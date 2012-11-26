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
package ditl.graphs;

import java.io.IOException;
import java.util.*;

import ditl.*;


public class LinksToReachableConverter implements LinkTrace.Handler, Generator, Converter {

	private long _tau;
	private long _delay;
	private long _eta;
	private long min_time;
	private long cur_time;
	private StatefulWriter<ArcEvent,Arc> arc_writer;
	private Set<Arc> init_state = new AdjacencySet.Arcs();
	private LinkTrace _links;
	private ReachabilityTrace _reachability;
	private Bus<Link> link_bus = new Bus<Link>();
	private Bus<ArcEvent> outbus = new Bus<ArcEvent>();
	private Set<Link> cur_links = new AdjacencySet.Links();
	
	public LinksToReachableConverter(ReachabilityTrace reachability, LinkTrace links, long eta, long tau, long delay) {
		min_time = links.minTime();
		_links = links;
		_reachability = reachability;
		_tau = tau;
		_delay = delay;
		_eta = eta;
		link_bus.addListener(new LinkListener());
		outbus.addListener(new Outputer());
	}

	@Override
	public Listener<LinkEvent> linkEventListener() {
		return new Listener<LinkEvent>() {
			@Override
			public void handle(long time, Collection<LinkEvent> events) throws IOException {
				for ( LinkEvent lev : events ){
					final Link l = lev.link();
					if ( lev.isUp() ){
						link_bus.queue(time+_tau,l);
					} else {
						if ( cur_links.contains(l) ){
							fire(time-_tau, l, false);
							cur_links.remove(l);
						} else {
							link_bus.removeFromQueueAfterTime(time-_tau, new Matcher<Link>(){
								@Override
								public boolean matches(Link item) {
									return item.equals(l);
								}
							});
						}
					}
				}
			}
		};
	}
	
	private void fire(long time, Link l, boolean up) {
		outbus.queue(time, new ArcEvent(l.id1(),l.id2(),up));
		outbus.queue(time, new ArcEvent(l.id2(),l.id1(),up));
	}

	@Override
	public Listener<Link> linkListener() {
		return new Listener<Link>(){
			@Override
			public void handle(long time, Collection<Link> events) {
				for ( Link l : events ){
					link_bus.queue(time+_tau, l);
				}
			}
		};
	}
	
	@Override
	public void incr(long dt) throws IOException {
		outbus.flush(cur_time - _delay - _tau);
		cur_time += dt;
	}

	@Override
	public void seek(long time) throws IOException {
		cur_time = time;
	}
	
	@Override
	public void convert() throws IOException {
		StatefulReader<LinkEvent,Link> link_reader = _links.getReader();
		arc_writer = _reachability.getWriter();
	
		arc_writer.setProperty(ReachabilityTrace.delayKey, _delay);
		arc_writer.setProperty(ReachabilityTrace.tauKey, _tau);
		arc_writer.setProperty(ReachabilityTrace.etaKey, _eta);
		arc_writer.setPropertiesFromTrace(_links);
		
		min_time = _links.minTime();
		
		if ( _delay >= _tau ){
			link_reader.stateBus().addListener(linkListener());
			link_reader.bus().addListener(linkEventListener());
		
			Runner runner = new Runner(_tau, _links.minTime(), _links.maxTime()+1);
			runner.addGenerator(link_reader);
			runner.addGenerator(this);
			runner.run();
			link_bus.flush(_links.maxTime());
			outbus.flush(_links.maxTime());
		} else {
			arc_writer.setInitState(min_time, Collections.<Arc>emptySet());
		}
		arc_writer.close();
		link_reader.close();
	}

	@Override
	public Bus<?>[] busses() {
		return new Bus<?>[]{ link_bus };
	}

	@Override
	public int priority() {
		return Trace.highestPriority;
	}

	private final class LinkListener implements Listener<Link> {
		@Override
		public void handle(long time, Collection<Link> events) {
			for ( Link l : events ){
				cur_links.add(l);
				fire(time-_delay, l, true);
			}
		}
	}
	
	private final class Outputer implements Listener<ArcEvent> {
		@Override
		public void handle(long time, Collection<ArcEvent> events)
				throws IOException {
			if ( init_state != null && time >= min_time ){
				arc_writer.setInitState(min_time, init_state);
				init_state = null;
			}
			Deque<ArcEvent> down_events = new LinkedList<ArcEvent>();
			for ( ArcEvent aev : events ){
				if ( init_state != null ){
					if ( aev.isUp() )
						init_state.add(aev.arc());
				} else {
					if ( aev.isUp() ){
						arc_writer.append(time, aev);
					} else {
						down_events.addLast(aev);
					}
				}
			}
			while ( ! down_events.isEmpty() )
				arc_writer.append(time, down_events.poll());
		}
	}
}
