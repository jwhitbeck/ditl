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



public final class AddingReachableConverter implements Converter, Generator, Listener<Object> {
	
	private AdjacencyMap<Edge,EdgeInfo> edge_infos = new AdjacencyMap.Edges<EdgeInfo>();
	private AdjacencySet<Edge> to_bring_down = new AdjacencySet.Edges();
	private AdjacencySet<Edge> to_bring_up = new AdjacencySet.Edges();
	
	private Bus<Object> update_bus = new Bus<Object>();
	private Composer[] composers;
	
	private StatefulWriter<EdgeEvent,Edge> writer;
	private ReachabilityFamily _family1, _family2;
	private ReachabilityTrace added_trace;
	private long _delay;
	private long tau;
	private long eta;
	private boolean init_state_set = false;
	private long min_time;

	
	public AddingReachableConverter(ReachabilityTrace addedTrace, ReachabilityFamily family1, ReachabilityFamily family2, long delay){
		added_trace = addedTrace;
		_family1 = family1;
		_family2 = family2;
		_delay = delay;
		eta = family1.eta();
		tau = family1.tau();
	}
	
	
	@Override
	public void convert() throws IOException {
		writer = added_trace.getWriter();
		writer.setProperty(ReachabilityTrace.etaKey, eta);
		writer.setProperty(ReachabilityTrace.tauKey, tau);
		writer.setProperty(ReachabilityTrace.delayKey, _delay);
		
		if ( tau == 0 ){
			composers = new Composer[1];
			if ( _delay == eta ) // first step in the tau==0 case, we are "adding" R0 and R0
				composers[0] = new Composer(_family1.getMember(0), _family2.getMember(0), eta);
			else
				composers[0] = new Composer(_family1.getMember(0), _family2.getMember(0));
		} else {
			int n = (int)(tau/eta);
			composers = new Composer[n];
			int i = (int)((_delay - _family1.delay() - _family2.delay())/eta);
			if ( i < 0 ){
				for ( int k=0; k<n; ++k ){
					composers[k] = new Composer( _family1.getMemberByOffset(i+k),
												_family2.getMemberByOffset(-k));
				}
			} else {
				for ( int k=0; k<n; ++k ){
					composers[k] = new Composer( _family1.getMemberByOffset(k),
												_family2.getMemberByOffset(i-k));
				}
			}
		}
		
		update_bus.addListener(this);
		Trace<?> trace = composers[0].delta_trace;
		min_time = trace.minTime();
		
		Runner runner = new Runner(eta, trace.minTime(), trace.maxTime());
		for ( Composer composer : composers )
			runner.addGenerator(composer);
		runner.addGenerator(this);
		runner.run();
		
		update_bus.flush(trace.maxTime());
		writer.setPropertiesFromTrace(trace);
		writer.close();
		for ( Composer composer : composers )
			composer.close();
	}
	
	
	private void scheduleUpdate(long time){
		update_bus.queue(time, Collections.<Object>emptyList());
	}

	
	private final class Waypoints {
		AdjacencySet.Edges delta_edges = new AdjacencySet.Edges();
		AdjacencySet.Edges mu_edges = new AdjacencySet.Edges();
		
		void addDeltaEdge(Edge de){
			delta_edges.add(de.reverse());
			for ( Integer to : mu_edges.getNext(de.to()) ){
				if ( ! to.equals(de.from()) ) //prevent loops on self
					increment( new Edge( de.from(), to) );
			}
		}
		
		void addShiftedMuEdge(Edge me){
			mu_edges.add(me);
			for ( Integer from : delta_edges.getNext(me.from()) ){
				if ( ! from.equals(me.to()) ) //prevent loops on self
					increment( new Edge( from, me.to()) );
			}
		}
		
		void removeDeltaEdge(Edge de){
			delta_edges.remove(de.reverse());
			for ( Integer to : mu_edges.getNext(de.to()) ){
				if ( ! to.equals(de.from()) ) //prevent loops on self
					decrement( new Edge( de.from(), to) );
			}
		}
		
		void removeShiftedMuEdge(Edge me){
			mu_edges.remove(me);
			for ( Integer from : delta_edges.getNext(me.from()) ){
				if ( ! from.equals(me.to()) ) //prevent loops on self
					decrement( new Edge( from, me.to()) );
			}
		}
		
	};
	
	private final class EdgeInfo {
		Edge _edge;
		int score=0;
		
		EdgeInfo(Edge edge){ 
			_edge = edge; 
			score = 1; // increment at creation
			to_bring_up.add(_edge); // bring up at next incr
			edge_infos.put(_edge, this);
		}
		
		void decrement(){
			score--;
			if ( score == 0 )
				to_bring_down.add(_edge);
		}
		
		void increment(){
			if ( score == 0 ){ // incrementing an edge that has been marked for deletion
				to_bring_down.remove(_edge);
			}
			score++;
		}
	}

	@Override
	public void incr(long dt) throws IOException {}



	@Override
	public void seek(long time) throws IOException {}



	@Override
	public Bus<?>[] busses() {
		return new Bus<?>[]{ update_bus };
	}


	@Override
	public int priority() {return Trace.lowestPriority;}

	void flushState(long time) throws IOException {
		if ( time >= min_time ){
			if ( ! init_state_set ){
				writer.setInitState(min_time, to_bring_up);
				init_state_set = true;
			} else {
				for ( Edge e : to_bring_up )
					writer.append(time, new EdgeEvent(e,EdgeEvent.UP));
			}
			to_bring_up.clear();
			for ( Edge e : to_bring_down ){
				edge_infos.remove(e);
				writer.append(time, new EdgeEvent(e,EdgeEvent.DOWN));
			}
			to_bring_down.clear();
		}
	}
	
	@Override
	public void handle(long time, Collection<Object> events) throws IOException {
		for ( Composer composer : composers )
			composer.processDeltaUpEvents();
		flushState(time-eta);
		for ( Composer composer : composers ){
			composer.processDeltaUpJourneyEvents();
			composer.processMuUpEvents();
		}
		for ( Composer composer : composers ){
			composer.processShiftedMuDownEvents();
			composer.processMuDownJourneyEvents();
			composer.processDeltaDownEvents();
		}
		scheduleUpdate(time+eta);
	}
	
	EdgeInfo increment(Edge e){
		EdgeInfo ei = edge_infos.get(e);
		if ( ei == null )
			ei = new EdgeInfo(e);
		else
			ei.increment();
		return ei;
	}
	
	EdgeInfo decrement(Edge e){
		EdgeInfo ei = edge_infos.get(e);
		ei.decrement();
		return ei;
	}
	
	
	private final class Composer implements Generator {
		ReachabilityTrace delta_trace, mu_trace;
		StatefulReader<EdgeEvent,Edge> delta_reader;
		StatefulReader<EdgeEvent,Edge> mu_reader;
		Deque<Edge> mu_down_events = new LinkedList<Edge>();
		Deque<Edge> shifted_mu_down_events = new LinkedList<Edge>();
		Deque<Edge> mu_up_events = new LinkedList<Edge>();
		Deque<Edge> delta_down_events = new LinkedList<Edge>();
		Deque<Edge> delta_up_events = new LinkedList<Edge>();
		Waypoints waypoints = new Waypoints();
		
		Composer (ReachabilityTrace deltaTrace, ReachabilityTrace muTrace) throws IOException{
			init(deltaTrace, muTrace, deltaTrace.delay());
		}
		
		Composer (ReachabilityTrace deltaTrace, ReachabilityTrace muTrace, long offset) throws IOException{
			init(deltaTrace, muTrace, offset);
		}
		
		void init(ReachabilityTrace deltaTrace, ReachabilityTrace muTrace, long offset) throws IOException{
			delta_trace = deltaTrace;
			mu_trace = muTrace;
			delta_reader = delta_trace.getReader();
			delta_reader.stateBus().addListener(new DeltaListener());
			delta_reader.bus().addListener(new DeltaEventListener());
			mu_reader = mu_trace.getReader(Trace.defaultPriority, offset);
			mu_reader.stateBus().addListener(new MuListener());
			mu_reader.bus().addListener(new MuEventListener());
		}
		
		void processDeltaUpEvents(){
			for ( Edge e : delta_up_events ){
				EdgeInfo ei = edge_infos.get(e);
				if ( ei == null ){
					ei = new EdgeInfo(e);
				} else {
					ei.increment();
				}
			}
		}
		
		void processDeltaUpJourneyEvents(){
			while ( ! delta_up_events.isEmpty() ){
				Edge de = delta_up_events.poll();
				waypoints.addDeltaEdge(de);
			}
		}
		
		void processDeltaDownEvents(){
			while ( ! delta_down_events.isEmpty() ){
				Edge de = delta_down_events.poll();
				decrement(de);
				waypoints.removeDeltaEdge(de);
			}
		}
		
		
		void processMuUpEvents(){
			while ( ! mu_up_events.isEmpty() ){
				Edge me = mu_up_events.poll();
				increment(me);
				waypoints.addShiftedMuEdge(me);
			}
		}
		
		void processMuDownJourneyEvents(){
			while ( ! mu_down_events.isEmpty() ){
				Edge me = mu_down_events.poll();
				shifted_mu_down_events.addLast(me);
				waypoints.removeShiftedMuEdge(me); 
			}
		}
		
		void processShiftedMuDownEvents(){
			while ( ! shifted_mu_down_events.isEmpty() ){
				Edge e = shifted_mu_down_events.poll();
				decrement(e);
			}
		}

		void close() throws IOException{
			delta_reader.close();
			mu_reader.close();
		}
		
		
		
		final class DeltaListener implements Listener<Edge>{
			@Override
			public void handle(long time, Collection<Edge> events){
				for ( Edge e : events ){
					increment(e);
					waypoints.addDeltaEdge(e);
				}
				scheduleUpdate(time+eta);
			}
		}
		
		final class DeltaEventListener implements Listener<EdgeEvent>  {
			@Override
			public void handle(long time, Collection<EdgeEvent> events){
				for ( EdgeEvent eev : events ){
					if ( eev.isUp() )
						delta_up_events.addLast(eev.edge());
					else
						delta_down_events.addLast(eev.edge());
				}
				scheduleUpdate(time);
			}
		}
		
		final class MuListener implements Listener<Edge> {
			@Override
			public void handle(long time, Collection<Edge> events) {
				for ( Edge e : events ){
					increment(e);
					waypoints.addShiftedMuEdge(e);
				}
				scheduleUpdate(time+eta);
			}
		}
		
		final class MuEventListener implements Listener<EdgeEvent> {
			@Override
			public void handle(long time, Collection<EdgeEvent> events) {
				for ( EdgeEvent eev : events ){
					if ( eev.isUp() )
						mu_up_events.addLast(eev.edge());
					else
						mu_down_events.addLast(eev.edge());
				}
				scheduleUpdate(time);
			}
		}
		
		@Override
		public void incr(long dt) throws IOException {
			delta_reader.incr(dt);
			mu_reader.incr(dt);
		}

		@Override
		public void seek(long time) throws IOException {
			mu_down_events.clear();
			shifted_mu_down_events.clear();
			mu_up_events.clear();
			delta_down_events.clear();
			delta_up_events.clear();
			delta_reader.seek(time);
			mu_reader.seek(time);
		}

		@Override
		public Bus<?>[] busses() {
			return new Bus<?>[]{ 
						delta_reader.stateBus(), delta_reader.bus(),
						mu_reader.stateBus(), mu_reader.bus() };
		}

		@Override
		public int priority() { return Trace.defaultPriority; }
	}
}
