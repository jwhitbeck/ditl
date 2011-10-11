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

import java.io.*;
import java.util.*;

import ditl.*;



public final class ClusteringCoefficientReport extends StateTimeReport 
	implements PresenceTrace.Handler, LinkTrace.Handler {
	
	private AdjacencyMatrix adjacency = new AdjacencyMatrix();
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
	
	private void updateSurroundingCoeffs(Link link){
		Integer i1 = link.id1;
		Integer i2 = link.id2;
		Set<Integer> n1 = adjacency.getNext(i1);
		Set<Integer> n2 = adjacency.getNext(i2);
		if ( n1 != null && n2 != null ){
			for ( Integer k : n1 )
				if ( n2.contains(k) )
					updateCoeff(k);
		}
		updateCoeff(i1);
		updateCoeff(i2);
	}
	
	private void updateCoeff(Integer i){
		Set<Integer> neighbs = adjacency.getNext(i);
		if ( neighbs == null ){
			coeffs.put(i,0.0);
		} else {
			int k = neighbs.size();
			if ( k < 2 ){
				coeffs.put(i,0.0);
			} else {
				Set<Integer> buff = new HashSet<Integer>();
				int n_links = 0;
				for ( Integer j : neighbs ){
					Set<Integer> j_neighbs = adjacency.getNext(j);
					for ( Integer l : buff ){
						if ( j_neighbs.contains(l) )
							n_links ++;
					}
					buff.add(j);
				}
				double coeff = 2*(double)n_links/(double)(k*(k-1));
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
	public Listener<LinkEvent> linkEventListener() {
		return new Listener<LinkEvent>(){
			Listener<LinkEvent> adj_listener = adjacency.linkEventListener();
			@Override
			public void handle(long time, Collection<LinkEvent> events)
					throws IOException {
				adj_listener.handle(time, events);
				for ( LinkEvent lev : events )
					updateSurroundingCoeffs(lev.link());
				update(time);
			}
		};
	}

	@Override
	public Listener<Link> linkListener() {
		return new StatefulListener<Link>(){
			StatefulListener<Link> adj_listener = (StatefulListener<Link>) (adjacency.linkListener());
			@Override
			public void reset() {
				adj_listener.reset();
			}

			@Override
			public void handle(long time, Collection<Link> events)
					throws IOException {
				adj_listener.handle(time, events);
				for ( Link l : events )
					updateSurroundingCoeffs(l);
				update(time);
			}
			
		};
	}
}
