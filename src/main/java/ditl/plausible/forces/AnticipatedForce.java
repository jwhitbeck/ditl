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
package ditl.plausible.forces;

import java.util.*;

import ditl.*;
import ditl.graphs.*;
import ditl.plausible.*;

public class AnticipatedForce implements Force, Interaction, 
	EdgeTrace.Handler, WindowedEdgeTrace.Handler {
	
	final static public double defaultK = 50.0; // the hooke parameter
	final static public double defaultAlpha = 2.0; // coulomb exponent
	final static public double defaultVmax = 10.0; // the maximal speed m/s
	final static public double defaultRange = 20; // the transmission range
	final static public double defaultEpsilon = 1.0; // guard to prevent denom from going to zero
	final static public double defaultTau = 100.0; // the strength of future events
	final static public double defaultCutoff = 30; // the distance beyond which we cease to push away
	final static public double defaultLambda = 5; // spring equilibrium length

	private double _K;
	private double _alpha;
	private double _vmax;
	private double _range;
	private double _epsilon;
	private double _tau;
	private double _cutoff;
	private double _lambda;
	private long _tps;
	private double _G;
	
	private Collection<Node> _nodes;
	
	private Map<Edge,WindowedEdge> window_map = new AdjacencyMap.Edges<WindowedEdge>();
	private Set<Edge> active_edges = new AdjacencySet.Edges();
	
	public AnticipatedForce(double K, double alpha, double vmax, double range, 
			double epsilon, double tau, double cutoff, double lambda, long tps){
		_K = K;
		_alpha = alpha;
		_vmax = vmax;
		_range = range;
		_epsilon = epsilon;
		_tau = tau;
		_cutoff = cutoff;
		_lambda = lambda;
		_tps = tps;
		_G = defaultG();
	}
	
	private double defaultG(){
		return _K * Math.pow(_epsilon+1, _alpha)*_range*(1-_lambda/_range); // balance at distance range
	}
	
	@Override
	public Point apply(long time, InferredNode node) {
		Point f = new Point(0,0);
		for ( Node other_node : _nodes ){
			if ( node != other_node ){
				f_rep(time, node, other_node, f);
				f_att(time, node, other_node, f);
			}
		}
		return f;
	}
	
	private void f_rep(long time, InferredNode node, Node other_node, Point f){
		Point r = node.nextPosition();
		Point or = other_node.currentPosition();
		double dx = r.x-or.x;
		double dy = r.y-or.y;
		double d2 = dx*dx + dy*dy;
		if ( d2 < _cutoff * _cutoff ){
			double d = Math.sqrt(d2);
			Integer id = node.id();
			Integer oid = other_node.id();
			Edge e = new Edge(id, oid);
			double dt = 0;
			if ( active_edges.contains(e) ){ // currently connected
				dt = (double)window_map.get(e).minUpTime(time) / (double)_tps;
			}
			double F = _G / Math.pow( _epsilon + (d + _vmax*dt)/_range, _alpha );
			f.x += F * dx/d;
			f.y += F * dy/d;
		}
	}
	
	private void f_att(long time, InferredNode node, Node other_node, Point f){
		Point r = node.nextPosition();
		Point or = other_node.currentPosition();
		double dx = r.x-or.x;
		double dy = r.y-or.y;
		Integer id = node.id();
		Integer oid = other_node.id();
		Edge e = new Edge(id,oid);
		if ( active_edges.contains(e) ){ // are connected
			double d = Math.sqrt( dx*dx + dy*dy );
			double F = _K*(d-_lambda);
			f.x = -F * dx/d;
			f.y = -F * dy/d;
		} else if ( window_map.containsKey(e) ){ // a window edge exists between them
			double dt = (double)window_map.get(new Edge(id,oid)).minDownTime(time) / (double)_tps;
			double d = Math.sqrt( dx*dx + dy*dy );
			double F = _K*(d-_lambda)*Math.exp(-_vmax*dt/_tau);
			f.x += -F * dx/d;
			f.y += -F * dy/d;
		}
	}

	@Override
	public void setNodeCollection(Collection<Node> nodes) {
		_nodes = nodes;
	}

	@Override
	public Listener<EdgeEvent> edgeEventListener() {
		return new Listener<EdgeEvent>(){
			@Override
			public void handle(long time, Collection<EdgeEvent> events){
				for ( EdgeEvent eev : events ){
					if ( eev.isUp() )
						active_edges.add(eev.edge());
					else
						active_edges.remove(eev.edge());
				}
			}
		};
	}

	@Override
	public Listener<Edge> edgeListener() {
		return new StatefulListener<Edge>(){
			@Override
			public void reset() {
				active_edges.clear();
			}

			@Override
			public void handle(long time, Collection<Edge> events){
				for ( Edge e : events )
					active_edges.add(e);
			}
			
		};
	}

	@Override
	public Listener<WindowedEdgeEvent> windowedEdgesEventListener() {
		return new Listener<WindowedEdgeEvent>(){
			@Override
			public void handle(long time, Collection<WindowedEdgeEvent> events) {
				for ( WindowedEdgeEvent wle : events ){
					Edge e = wle.edge();
					switch( wle.type() ){
					case UP:
						window_map.put(e, new WindowedEdge(e));
						break;
					case DOWN:
						window_map.remove(e);
						break;
					default:
						window_map.get(e).handleEvent(wle);
					}
				}
			}
		};
	}

	@Override
	public Listener<WindowedEdge> windowedEdgesListener() {
		return new StatefulListener<WindowedEdge>(){
			@Override
			public void reset() {
				window_map.clear();
			}

			@Override
			public void handle(long time, Collection<WindowedEdge> events){
				Edge e;
				for ( WindowedEdge wl : events ){
					e = wl.edge();
					window_map.put(e, wl);
				}
			}
			
		};
	}


}
