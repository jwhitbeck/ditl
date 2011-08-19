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




public final class BeaconsToEdgesConverter implements Incrementable, Converter {

	private long _period;
	private int _tol = 0;
	private long cur_time;
	private TreeMap<Long,Set<Edge>> edgesBuffer = new TreeMap<Long,Set<Edge>>();
	private Map<Edge,Long> lastEdges = new HashMap<Edge,Long>();
	private Random rng = new Random();
	private double _expansion;
	private StatefulWriter<EdgeEvent, Edge> edge_writer;
	private Reader<Edge> beacon_reader;
	
	public BeaconsToEdgesConverter(StatefulWriter<EdgeEvent, Edge> edgeWriter,
			Reader<Edge> beaconReader, long period, int tol, double expansion) {
		edge_writer = edgeWriter;
		beacon_reader = beaconReader;
		_period = period;
		_tol = tol;
		_expansion = expansion;
	}
	
	public Listener<Edge> detectedListener(){
		return new Listener<Edge>() {
			@Override
			public void handle(long time, Collection<Edge> events) {
				for ( Edge e : events ){
					if ( ! lastEdges.containsKey(e) ){ // link comes up
						long start_time = time - (long)(_expansion*rand()); 
						edge_writer.queue(start_time, new EdgeEvent(e,EdgeEvent.UP));
					}
					appendLastEdge(time,e);
				}
			}
		};
	}
	
	private void appendLastEdge(long time, Edge edge){
		if ( lastEdges.containsKey(edge) ){ // remove from beaconBuffer
			Long prevTime = lastEdges.get(edge);
			Set<Edge> edges = edgesBuffer.get(prevTime);
			edges.remove(edge);
			if ( edges.isEmpty() )
				edgesBuffer.remove(prevTime);
		}
		if ( ! edgesBuffer.containsKey(time) )
			edgesBuffer.put(time, new HashSet<Edge>() );
		edgesBuffer.get(time).add(edge);
		lastEdges.put(edge, time);
	}
	
	
	private void expireBeacons(){
		while ( ! edgesBuffer.isEmpty() && 
				edgesBuffer.firstKey() <= cur_time - (_tol+1)*_period ){
			Map.Entry<Long, Set<Edge>> e = edgesBuffer.pollFirstEntry();
			long time = e.getKey();
			for ( Edge edge : e.getValue() ){
				long end_time = time + (long)(_expansion*rand());
				edge_writer.queue(end_time, new EdgeEvent(edge,EdgeEvent.DOWN));
				lastEdges.remove(edge);
			}
		}
	}
	
	private long rand(){
		return Math.abs(rng.nextLong()) % _period;
	}
	
	@Override
	public void incr(long time) throws IOException {
		expireBeacons();
		edge_writer.flush(cur_time-(_tol+1)*_period);
		cur_time += time;
	}

	@Override
	public void seek(long time) {
		cur_time = time;
	}
	
	@Override
	public void close() throws IOException {
		edge_writer.close();
	}
	
	@Override
	public void run() throws IOException {
		
		Trace detected = beacon_reader.trace();
		
		Bus<Edge> detectedBus = new Bus<Edge>();	
		beacon_reader.setBus(detectedBus);
		
		detectedBus.addListener(detectedListener());
		
		Runner runner = new Runner(_period,detected.minTime(),detected.maxTime());
		runner.addGenerator(beacon_reader);
		runner.add(this);
		
		runner.run();
				
		edge_writer.flush(cur_time+(_tol+1)*_period);
		
		edge_writer.setProperty(Trace.ticsPerSecondKey, detected.ticsPerSecond());
	}
}
