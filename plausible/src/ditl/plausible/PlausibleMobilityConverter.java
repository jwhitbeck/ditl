/*******************************************************************************
 * This file is part of DITL.                                                  *
 *                                                                             *
 * Copyright (C) 2011 John Whitbeck <john@whitbeck.fr>                         *
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

import ditl.plausible.constraints.BoxConstraint;
import ditl.*;
import ditl.graphs.*;

public final class PlausibleMobilityConverter implements Converter, 
	PresenceHandler, MovementHandler {
	
	private StatefulReader<MovementEvent,Movement> known_reader = null;
	private StatefulReader<LinkEvent,Link> link_reader;
	private StatefulReader<WindowedLinkEvent,WindowedLink> window_reader;
	private StatefulReader<PresenceEvent,Presence> presence_reader;
	private StatefulWriter<MovementEvent,Movement> writer;
	
	private Set<Integer> known_movement_ids = new HashSet<Integer>();
	private Map<Integer,KnownNode> known_nodes = new HashMap<Integer,KnownNode>();
	private Map<Integer,InferredNode> inferred_nodes = new HashMap<Integer,InferredNode>();
	private List<Node> all_nodes = new LinkedList<Node>();
	
	private List<Constraint> global_constraints = new LinkedList<Constraint>();
	private List<Force> global_forces = new LinkedList<Force>();
	private Map<Integer,List<Constraint>> node_constraints = new HashMap<Integer,List<Constraint>>();
	
	private double _height, _width;
	private boolean overlap = true;
	
	private final static long rng_seed = 0;
	private Random rng = new Random(rng_seed);
	
	private long update_interval;
	private long tps;
	private long incr_interval;
	private int n_steps = 100; // by default, calculate 100 intermediate points between successive updates
	private long warm_time = 100; // by default warming period is equivalent to 100s of mobility
	
	private double _e; // width of "tube" for approximating straight lines
	private double _s; // distance threshold for deciding whether a node is static or not 
	
	public PlausibleMobilityConverter(
			StatefulWriter<MovementEvent,Movement> movementWriter,
			StatefulReader<PresenceEvent,Presence> presenceReader,
			StatefulReader<LinkEvent,Link> linkReader,
			StatefulReader<WindowedLinkEvent,WindowedLink> windowedLinkReader,
			StatefulReader<MovementEvent,Movement> knownMovement, 
			double width, double height, double border, double e, double s){
		
		writer = movementWriter;
		presence_reader = presenceReader;
		link_reader = linkReader;
		window_reader = windowedLinkReader;
		known_reader = knownMovement;
		_height = height;
		_width = width;
		addGlobalConstraint(new BoxConstraint(_width,_height, border));
		tps = presence_reader.trace().ticsPerSecond();
		setUpdateInterval(1); // by default 1 position per second
		_e = e;
		_s = s;
	}
	
	public void markKnownMovement(Integer[] ids){
		for ( Integer id : ids )
			known_movement_ids.add(id);
	}
	
	public void setUpdateInterval(long interval){
		update_interval = interval*tps;
		incr_interval = update_interval / n_steps;
	}
	
	public void setWarmTime(long warmTime){
		warm_time = warmTime;
	}
	
	public void setNSteps(int nSteps){
		n_steps = nSteps;
		incr_interval = update_interval / n_steps;
	}
	
	public void setOverlap(boolean b){
		overlap = b;
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
	public void close() throws IOException {
		writer.close();
	}

	@Override
	public void run() throws IOException {
		
		Trace presence = presence_reader.trace();
		long min_time = presence.minTime();
		long max_time = presence.maxTime();
		
		// init event busses
		Bus<Presence> presenceBus = new Bus<Presence>();
		Bus<PresenceEvent> presenceEventBus = new Bus<PresenceEvent>();
		presence_reader.setBus(presenceEventBus);
		presence_reader.setStateBus(presenceBus);
		
		Bus<Link> linkBus = new Bus<Link>();
		Bus<LinkEvent> linkEventBus = new Bus<LinkEvent>();
		link_reader.setBus(linkEventBus);
		link_reader.setStateBus(linkBus);
		
		Bus<WindowedLink> windowBus = new Bus<WindowedLink>();
		Bus<WindowedLinkEvent> windowEventBus = new Bus<WindowedLinkEvent>();
		window_reader.setBus(windowEventBus);
		window_reader.setStateBus(windowBus);
		
		Bus<Movement> knownBus = new Bus<Movement>();
		Bus<MovementEvent> knownEventBus = new Bus<MovementEvent>();
		if ( known_reader != null ){
			known_reader.setBus(knownEventBus);
			known_reader.setStateBus(knownBus);
		}
		
		// add bus listeners
		presenceBus.addListener(this.presenceListener());
		presenceEventBus.addListener(this.presenceEventListener());
		for ( Force force : global_forces ){
			if ( force instanceof PresenceHandler ){
				PresenceHandler phf = (PresenceHandler)force;
				presenceBus.addListener(phf.presenceListener());
				presenceEventBus.addListener(phf.presenceEventListener());
			}
		}
		
		for ( Force force : global_forces ){
			if ( force instanceof LinkHandler ){
				LinkHandler lhf = (LinkHandler)force;
				linkBus.addListener(lhf.linkListener());
				linkEventBus.addListener(lhf.linkEventListener());
			}
		}
		
		for ( Force force : global_forces ){
			if ( force instanceof WindowedLinkHandler ){
				WindowedLinkHandler wlhf = (WindowedLinkHandler)force;
				windowBus.addListener(wlhf.windowedLinkListener());
				windowEventBus.addListener(wlhf.windowedLinkEventListener());
			}
		}
		
		knownBus.addListener(this.movementListener());
		knownEventBus.addListener(this.movementEventListener());
		
		Runner runner = new Runner(incr_interval, min_time, max_time);
		runner.addGenerator(presence_reader);
		runner.addGenerator(link_reader);
		runner.addGenerator(window_reader);
		if ( known_reader != null )
			runner.addGenerator(known_reader);
		
		runner.seek(min_time);
		
		// get initial state
		Bounds bounds = new Bounds();
		bounds.update(new Point(0,0));
		bounds.update(new Point(_width,_height));
		bounds.writeToTrace(writer);
		
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
		while ( cur_time < max_time + incr_interval ){
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
		
		// a few extras
		writer.setProperty(Trace.maxTimeKey, max_time);	
		writer.setProperty(Trace.ticsPerSecondKey, tps);
		
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
		int ni = (int)(warm_time * tps / incr_interval);
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
		for ( Node node : all_nodes ){
			node.step(time, incr_interval, tps);
			if ( overlap )
				node.commit();
		}
		if ( ! overlap )
			for ( Node node : all_nodes )
				node.commit();
	}
}
