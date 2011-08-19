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



public final class BeaconningConverter implements PresenceHandler, Converter, Generator {

	protected boolean _randomize;
	private double _p;
	private AdjacencyMatrix _adjacency = new AdjacencyMatrix();
	private long _period;
	private Random rng = new Random();
	private Bus<Integer> next_scans = new Bus<Integer>();
	
	private StatefulReader<PresenceEvent,Presence> presence_reader;
	private StatefulReader<LinkEvent,Link> link_reader;
	private Writer<Edge> beacon_writer;
	
	
	public BeaconningConverter( Writer<Edge> beaconWriter,
			StatefulReader<PresenceEvent,Presence> presenceReader,
			StatefulReader<LinkEvent,Link> linkReader, long period, double p, boolean randomize) {
		_randomize = randomize;
		_p = p;
		_period = period;
		beacon_writer = beaconWriter;
		presence_reader = presenceReader;
		link_reader = linkReader;
		next_scans.addListener(nextScansListener());
	}

	public Listener<Integer> nextScansListener() {
		return new Listener<Integer>(){
			@Override
			public void handle(long time, Collection<Integer> events) throws IOException {
				for ( Integer i : events ){
					Set<Integer> neighbs = _adjacency.getNext(i);
					if ( neighbs != null ){
						for ( Integer n : _adjacency.getNext(i) ){
							double q = rng.nextDouble();
							if ( q < 1.0-_p )
								beacon_writer.append(time, new Edge(i, n));
						}
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
	public void close() throws IOException {
		beacon_writer.close();
	}

	@Override
	public void run() throws IOException {
		Trace links = link_reader.trace();
		Trace presence = presence_reader.trace();
		
		Bus<LinkEvent> linkEventBus = new Bus<LinkEvent>();
		Bus<PresenceEvent> presenceEventBus = new Bus<PresenceEvent>();
		Bus<Presence> presenceBus = new Bus<Presence>();
		Bus<Link> linkBus = new Bus<Link>();
		
		presence_reader.setBus(presenceEventBus);
		presence_reader.setStateBus(presenceBus);
		
		presenceEventBus.addListener(presenceEventListener());
		presenceBus.addListener(presenceListener());
		
		link_reader.setBus(linkEventBus);
		link_reader.setStateBus(linkBus);
		
		linkBus.addListener(_adjacency.linkListener());
		linkEventBus.addListener(_adjacency.linkEventListener());
		
		Runner runner = new Runner(links.maxUpdateInterval(), presence.minTime(), presence.maxTime());
		runner.addGenerator(presence_reader);
		runner.addGenerator(link_reader);
		runner.addGenerator(this);
		runner.run();
		
		beacon_writer.setProperty(Trace.ticsPerSecondKey, links.ticsPerSecond());
	}

	@Override
	public Bus<?>[] busses() {
		return new Bus<?>[]{next_scans};
	}

	@Override
	public int priority() {
		return Runner.defaultPriority;
	}

	@Override
	public void incr(long time) throws IOException {}

	@Override
	public void seek(long time) throws IOException {}

}
