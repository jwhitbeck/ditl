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

import java.io.*;
import java.util.*;

import ditl.*;



public final class ClusteringCoefficientReport extends StateTimeReport 
	implements PresenceTrace.Handler, EdgeTrace.Handler {
	
	private AdjacencySet.Edges adjacency = new AdjacencySet.Edges();
	private Map<Integer,Double> coeffs = new HashMap<Integer,Double>();
	private boolean remove_leaves;
	
	public ClusteringCoefficientReport(OutputStream out, boolean removeLeaves) throws IOException {
		super(out);
		remove_leaves = removeLeaves;
		appendComment("time | duration | clustering coefficient distribution");
	}
	
	public static final class Factory implements ReportFactory<ClusteringCoefficientReport> {
		private boolean remove_leaves;
		public Factory(boolean removeLeaves){ remove_leaves = removeLeaves;}
		@Override
		public ClusteringCoefficientReport getNew(OutputStream out) throws IOException {
			return new ClusteringCoefficientReport(out, remove_leaves);
		}
	}
	
	private void updateSurroundingCoeffs(Edge edge){
		Integer i1 = edge.id1;
		Integer i2 = edge.id2;
		Set<Integer> n1 = adjacency.getNext(i1);
		Set<Integer> n2 = adjacency.getNext(i2);
		if ( ! n1.isEmpty() && ! n2.isEmpty() ){
			for ( Integer k : n1 )
				if ( n2.contains(k) )
					updateCoeff(k);
		}
		updateCoeff(i1);
		updateCoeff(i2);
	}
	
	private void updateCoeff(Integer i){
		Set<Integer> neighbs = adjacency.getNext(i);
		if ( neighbs.isEmpty() ){
			coeffs.put(i,0.0);
		} else {
			int k = neighbs.size();
			if ( k < 2 ){
				coeffs.put(i,0.0);
			} else {
				Set<Integer> buff = new HashSet<Integer>();
				int n_edges = 0;
				for ( Integer j : neighbs ){
					Set<Integer> j_neighbs = adjacency.getNext(j);
					for ( Integer l : buff ){
						if ( j_neighbs.contains(l) )
							n_edges ++;
					}
					buff.add(j);
				}
				double coeff = 2*(double)n_edges/(double)(k*(k-1));
				coeffs.put(i, coeff);
			}
		}
	}

	private void update(long time) throws IOException {
		StringBuffer buffer = new StringBuffer();
		//buffer.append(time);
		for ( Double c : coeffs.values() ){
			if ( c > 0 || ! remove_leaves )
				buffer.append(c+" ");
		}
		append( time, buffer.toString() );
	}
	

	@Override
	public Listener<PresenceEvent> presenceEventListener() {
		return new Listener<PresenceEvent>(){
			@Override
			public void handle(long time, Collection<PresenceEvent> events){
				for ( PresenceEvent pev : events ){
					if ( pev.isIn() ){
						coeffs.put(pev.id(), 0.0);
					} else {
						coeffs.remove(pev.id());
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
				coeffs.clear();
			}

			@Override
			public void handle(long time, Collection<Presence> events) {
				for ( Presence p : events )
					coeffs.put(p.id(), 0.0);
			}
		};
	}

	@Override
	public Listener<EdgeEvent> edgeEventListener() {
		return new Listener<EdgeEvent>(){
			Listener<EdgeEvent> adj_listener = adjacency.edgeEventListener();
			@Override
			public void handle(long time, Collection<EdgeEvent> events)
					throws IOException {
				adj_listener.handle(time, events);
				for ( EdgeEvent eev : events )
					updateSurroundingCoeffs(eev.edge());
				update(time);
			}
		};
	}

	@Override
	public Listener<Edge> edgeListener() {
		return new StatefulListener<Edge>(){
			StatefulListener<Edge> adj_listener = (StatefulListener<Edge>) (adjacency.edgeListener());
			@Override
			public void reset() {
				adj_listener.reset();
			}

			@Override
			public void handle(long time, Collection<Edge> events)
					throws IOException {
				adj_listener.handle(time, events);
				for ( Edge e : events )
					updateSurroundingCoeffs(e);
				update(time);
			}
			
		};
	}
}
