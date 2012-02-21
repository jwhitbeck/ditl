/*******************************************************************************
 * This file is part of DITL.                                                  *
 *                                                                             *
 * Copyright (C) 2011-2012 John Whitbeck <john@whitbeck.fr>                    *
 *                                                                             *
 * DITL is free software: you can redistribute it and/or modify                *
 * it under the terms of the GNU General Public License as published by        *
 * the Free Software Foundation, either version 3 of the License, or           *
 * (at your option) any later version.                                         *
 *                                                                             *
 * DITL is distributed in the hope that it will be useful,                     *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the               *
 * GNU General Public License for more details.                                *
 *                                                                             *
 * You should have received a copy of the GNU General Public License           *
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.       *
 *******************************************************************************/
package ditl.plausible;

import java.io.IOException;
import java.util.*;

import ditl.*;
import ditl.graphs.*;

public final class PlausibleMobilityConverter implements Converter, 
	PresenceTrace.Handler, MovementTrace.Handler {
	
	private StatefulWriter<MovementEvent,Movement> writer;
	
	private MovementTrace known_movement;
	private LinkTrace _links;
	private WindowedLinkTrace windowed_links;
	private PresenceTrace _presence;
	private MovementTrace _movement;
	
	private Set<Integer> known_movement_ids = new HashSet<Integer>();
	private Map<Integer,KnownNode> known_nodes = new HashMap<Integer,KnownNode>();
	private Map<Integer,InferredNode> inferred_nodes = new HashMap<Integer,InferredNode>();
	private List<Node> all_nodes = new LinkedList<Node>();
	
	private List<Constraint> global_constraints = new LinkedList<Constraint>();
	private List<Force> global_forces = new LinkedList<Force>();
	private Map<Integer,List<Constraint>> node_constraints = new HashMap<Integer,List<Constraint>>();
	
	private double _height, _width;
	private boolean _overlap;
	
	private final static long rng_seed = 0;
	private Random rng = new Random(rng_seed);
	
	private long update_interval;
	private long tps;
	private long incr_interval;
	private int n_steps; 
	private long warm_time;
	
	public static final int defaultNSteps = 100; // by default, calculate 100 intermediate points between successive updates
	public static final long defaultWarmTime = 100; // by default warming period is equivalent to 100s of mobility
	public static final double defaultTubeWidth = 10; // width of "tube" for approximating straight lines
	public static final double defaultStaticThresh = 0.1; // distance threshold for deciding whether a node is static or not
	public static final double defaultBorder = 10;
	public static final long defaultUpdateInterval = 1; // by default calculate positions every second
	public static final boolean defaultOverlap = true;
	
	private double _e; 
	private double _s; 
	
	public PlausibleMobilityConverter(MovementTrace movement,
			PresenceTrace presence, LinkTrace links,
			WindowedLinkTrace windowedLinks, MovementTrace knownMovement,
			double width, double height, double e, double s, 
			int nSteps, long updateInterval, long warmTime, boolean overlap){
		
		_movement = movement;
		_presence = presence;
		_links = links;
		windowed_links = windowedLinks;
		known_movement = knownMovement;
		_height = height;
		_width = width;
		tps = _presence.ticsPerSecond();
		warm_time = warmTime;
		n_steps = nSteps;
		update_interval = updateInterval;
		incr_interval = update_interval / n_steps;
		_overlap = overlap;
		_e = e;
		_s = s;
	}
	
	public void markKnownMovement(Integer...ids){
		for ( Integer id : ids )
			known_movement_ids.add(id);
	}
	
	public void addGlobalConstraint(Constraint constraint){
		global_constraints.add(constraint);
		if ( constraint instanceof Interaction ){
			((Interaction)constraint).setNodeCollection(Collections.unmodifiableList(all_nodes));
		}
	}
	
	public void addNodeConstraint(Integer id, Constraint constraint){
		if ( ! node_constraints.containsKey(id) ){
			node_constraints.put(id, new LinkedList<Constraint>());
		}
		node_constraints.get(id).add(constraint);
		if ( constraint instanceof Interaction ){
			((Interaction)constraint).setNodeCollection(Collections.unmodifiableList(all_nodes));
		}
	}
	
	public void addGlobalForce(Force force){
		global_forces.add(force);
		if ( force instanceof Interaction ){
			((Interaction)force).setNodeCollection(Collections.unmodifiableList(all_nodes));
		}
	}

	@Override
	public void convert() throws IOException {
		long min_time = _presence.minTime();
		long max_time = _presence.maxTime();
		
		StatefulReader<MovementEvent,Movement> known_reader = null;
		writer = _movement.getWriter();
		
		// init event readers
		StatefulReader<PresenceEvent,Presence> presence_reader = _presence.getReader();		
		StatefulReader<LinkEvent,Link> link_reader = _links.getReader();
		StatefulReader<WindowedLinkEvent,WindowedLink> window_reader = windowed_links.getReader();
		
		if ( known_movement != null ){
			known_reader = known_movement.getReader();
		}
		
		// add bus listeners
		presence_reader.stateBus().addListener(this.presenceListener());
		presence_reader.bus().addListener(this.presenceEventListener());
		for ( Force force : global_forces ){
			if ( force instanceof PresenceTrace.Handler ){
				PresenceTrace.Handler phf = (PresenceTrace.Handler)force;
				presence_reader.stateBus().addListener(phf.presenceListener());
				presence_reader.bus().addListener(phf.presenceEventListener());
			}
		}
		
		for ( Force force : global_forces ){
			if ( force instanceof LinkTrace.Handler ){
				LinkTrace.Handler lhf = (LinkTrace.Handler)force;
				link_reader.stateBus().addListener(lhf.linkListener());
				link_reader.bus().addListener(lhf.linkEventListener());
			}
		}
		
		for ( Force force : global_forces ){
			if ( force instanceof WindowedLinkTrace.Handler ){
				WindowedLinkTrace.Handler wlhf = (WindowedLinkTrace.Handler)force;
				window_reader.stateBus().addListener(wlhf.windowedLinkListener());
				window_reader.bus().addListener(wlhf.windowedLinkEventListener());
			}
		}
		
		if ( known_reader != null ){
			known_reader.stateBus().addListener(this.movementListener());
			known_reader.bus().addListener(this.movementEventListener());
		}
		
		Runner runner = new Runner(incr_interval, min_time, max_time);
		runner.addGenerator(presence_reader);
		runner.addGenerator(link_reader);
		runner.addGenerator(window_reader);
		if ( known_reader != null )
			runner.addGenerator(known_reader);
		
		runner.seek(min_time);
		
		// get initial state
		Set<Movement> init_mv = new HashSet<Movement>();
		
		warm(min_time);
		
		for ( InferredNode node : inferred_nodes.values() ){
			init_mv.add(new Movement(node.id(), node.cur));
		}
		for ( KnownNode node : known_nodes.values() ){
			init_mv.add(node._movement);
		}
		
		writer.setInitState(min_time, init_mv);
		
		long cur_time = min_time;
		long prev_time;
		
		// infer node mobility
		while ( cur_time < max_time ){
			prev_time = cur_time;
			runner.incr();
			cur_time = runner.time();
			step(cur_time);
			if ( (cur_time-min_time) % update_interval == 0 ){
				long min_last_time = Trace.INFINITY;
				for ( Node node : inferred_nodes.values() ){
					node.writeMovement(cur_time, prev_time, _s, _e, writer);
					if ( node.lastRefTime() < min_last_time )
						min_last_time = node.lastRefTime();
				}
				writer.flush(min_last_time-update_interval);
			}
		}
		writer.flush();
		
		writer.setPropertiesFromTrace(_presence);
		writer.close();
		link_reader.close();
		presence_reader.close();
		window_reader.close();
		if ( known_reader != null )
			known_reader.close();
		
	}
	
	@Override
	public Listener<Movement> movementListener() {
		return new StatefulListener<Movement>(){
			@Override
			public void reset() {
				known_nodes.clear();
			}

			@Override
			public void handle(long time, Collection<Movement> events)
					throws IOException {
				Integer id;
				for ( Movement mv : events ){
					id = mv.id();
					if ( known_movement_ids.contains(id) ){
						KnownNode node = new KnownNode(id, mv);
						known_nodes.put(id,node);
						all_nodes.add(node);
					}
				}
			}
		};
	}

	@Override
	public Listener<MovementEvent> movementEventListener() {
		return new Listener<MovementEvent>(){
			@Override
			public void handle(long time, Collection<MovementEvent> events)
					throws IOException {
				KnownNode node;
				Integer id;
				for ( MovementEvent mev : events ){
					id = mev.id();
					if ( known_movement_ids.contains(id) ){
						switch ( mev.type() ){
						case MovementEvent.IN:
							node = new KnownNode(id, mev.origMovement());
							known_nodes.put(id,node);
							all_nodes.add(node);
							break;
						case MovementEvent.OUT:
							node = known_nodes.remove(id);
							all_nodes.remove(node);
							break;
						default:
							node = known_nodes.get(id);
							node.updateMovement(time, mev);
						}
						writer.queue(time, mev);
					}
				}
			}
		};
	}

	@Override
	public Listener<Presence> presenceListener() {
		return new StatefulListener<Presence>(){
			@Override
			public void reset() {
				inferred_nodes.clear();
			}

			@Override
			public void handle(long time, Collection<Presence> events)
					throws IOException {
				Integer id;
				for ( Presence p : events ){
					id = p.id();
					if ( ! known_movement_ids.contains(id) ){
						initInferredNode(id);
					}
				}
			}
		};
	}

	@Override
	public Listener<PresenceEvent> presenceEventListener() {
		return new Listener<PresenceEvent>(){
			@Override
			public void handle(long time, Collection<PresenceEvent> events)
					throws IOException {
				Integer id;
				for ( PresenceEvent pev : events ){
					id = pev.id();
					if ( ! known_movement_ids.contains(id) ){
						if ( pev.isIn() ){
							initInferredNode(id);
						} else {
							removeInferredNode(id);
						}
						// TODO: balance and add?
					}
				}
			}
		};
	}

	private void initInferredNode(Integer id){
		InferredNode node = new InferredNode(id);
		inferred_nodes.put(id,node);
		all_nodes.add(node);
		setForces(node);
		setConstraints(node);
		node.cur.x = rng.nextFloat()*_width;
		node.next.x = node.cur.x;
		node.cur.y = rng.nextFloat()*_height;
		node.next.y = node.cur.y;
	}
	
	private void removeInferredNode(Integer id){
		Node node = inferred_nodes.remove(id);
		all_nodes.remove(node);
	}
	
	private void setForces(InferredNode node){
		for ( Force f : global_forces )
			node.addForce(f);
	}
	
	private void setConstraints(InferredNode node){
		for ( Constraint cnstr : global_constraints )
			node.addConstraint(cnstr);
		if ( node_constraints.containsKey(node.id()) ){
			for ( Constraint cnstr : node_constraints.get(node.id()))
				node.addConstraint(cnstr);
		}
	}
	
	private void warm(long time){
		int i = 0;
		int ni = (int)(warm_time / incr_interval);
		do {
			step(time);
			++i;
		} while ( i < ni );
		
		for ( Node node : inferred_nodes.values() ){
			node.sampleCurrent();
			node.setReference(time, node.cur.copy());
		}
	}
	
	private void step(long time){
		Collections.shuffle(all_nodes, rng);
		double rdt = (double)incr_interval / (double)tps;
		for ( Node node : all_nodes ){
			node.step(time, rdt);
			if ( _overlap )
				node.commit();
		}
		if ( ! _overlap )
			for ( Node node : all_nodes )
				node.commit();
	}
}
