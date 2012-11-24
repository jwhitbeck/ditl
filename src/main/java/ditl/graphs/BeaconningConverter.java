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
package ditl.graphs;

import java.io.IOException;
import java.util.*;

import ditl.*;



public final class BeaconningConverter implements PresenceTrace.Handler, Converter, Generator {

	protected boolean _randomize;
	private double _p;
	private AdjacencySet.Links _adjacency = new AdjacencySet.Links();
	private long _period;
	private Random rng = new Random();
	private Bus<Integer> next_scans = new Bus<Integer>();
	private Writer<Edge> beacon_writer;
	
	private BeaconTrace _beacons;
	private PresenceTrace _presence;
	private LinkTrace _links;
	
	
	public BeaconningConverter( BeaconTrace beacons, PresenceTrace presence, 
			LinkTrace links, long period, double p, boolean randomize) {
		_randomize = randomize;
		_p = p;
		_period = period;
		_beacons = beacons;
		_links = links;
		_presence = presence;
		next_scans.addListener(nextScansListener());
	}

	public Listener<Integer> nextScansListener() {
		return new Listener<Integer>(){
			@Override
			public void handle(long time, Collection<Integer> events) throws IOException {
				for ( Integer i : events ){
					for ( Integer n : _adjacency.getNext(i) ){
						double q = rng.nextDouble();
						if ( q < 1.0-_p )
							beacon_writer.append(time, new Edge(i, n));
					}
					next_scans.queue(time+_period, i);
				}
			}
		};
	}
	
	@Override
	public Listener<Presence> presenceListener() {
		return new StatefulListener<Presence>(){
			@Override
			public void handle(long time, Collection<Presence> events) {
				for ( Presence p : events )
					initBeaconning(time, p.id());
			}

			@Override
			public void reset() {
				next_scans.reset();
			}
		};
	}
	
	private void initBeaconning(long time, Integer id){
		long nextScan = (_randomize)? time + Math.abs(rng.nextLong() % _period) : time;
		next_scans.queue(nextScan, id );
	}
	
	private void stopBeaconning(long time, final Integer id){
		next_scans.removeFromQueueAfterTime(time, new Matcher<Integer>(){
			@Override
			public boolean matches(Integer item) {
				return item.equals(id);
			}
		});
	}
	
	
	@Override
	public Listener<PresenceEvent> presenceEventListener() {
		return new Listener<PresenceEvent>(){
			@Override
			public void handle(long time, Collection<PresenceEvent> events) {
				for ( PresenceEvent pev : events ){
					if ( pev.isIn() )
						initBeaconning(time, pev.id());
					else
						stopBeaconning(time, pev.id());
				}
			}
		};
	}

	@Override
	public void convert() throws IOException {
		StatefulReader<PresenceEvent,Presence> presence_reader = _presence.getReader();
		StatefulReader<LinkEvent,Link> link_reader = _links.getReader();
		beacon_writer = _beacons.getWriter();
		
		presence_reader.bus().addListener(presenceEventListener());
		presence_reader.stateBus().addListener(presenceListener());
		
		link_reader.stateBus().addListener(_adjacency.linkListener());
		link_reader.bus().addListener(_adjacency.linkEventListener());
		
		Runner runner = new Runner(_links.maxUpdateInterval(), _presence.minTime(), _presence.maxTime());
		runner.addGenerator(presence_reader);
		runner.addGenerator(link_reader);
		runner.addGenerator(this);
		runner.run();
		
		beacon_writer.setProperty(BeaconTrace.beaconningPeriodKey, _period);
		beacon_writer.setPropertiesFromTrace(_links);
		beacon_writer.close();
		presence_reader.close();
		link_reader.close();
	}

	@Override
	public Bus<?>[] busses() {
		return new Bus<?>[]{next_scans};
	}

	@Override
	public int priority() {
		return Trace.defaultPriority;
	}

	@Override
	public void incr(long dt) throws IOException {}

	@Override
	public void seek(long time) throws IOException {}

}
