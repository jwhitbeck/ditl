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
package ditl.plausible.forces;

import java.util.*;

import ditl.plausible.*;
import ditl.*;
import ditl.graphs.*;

public class AnticipatedForce implements Force, Interaction, 
	LinkHandler, WindowedLinkHandler {

	private double K = 50.0; // the hooke parameter
	private double alpha = 2.0; // coulomb exponent
	private double vmax = 10.0; // the maximal speed m/s
	private double range = 20; // the transmission range
	private double epsilon = 1.0; // guard to prevent denom from going to zero
	private double tau = 100.0; // the strength of future events
	private double cutoff = 30; // the distance beyond which we cease to push away
	private double lambda = 5; // spring equilibrium length
	private long tps = 1000; // tics per second
	private double G = defaultG(); // the coulomb parameter
	
	private Collection<Node> _nodes;
	
	private Map<Link,WindowedLink> window_map = new HashMap<Link,WindowedLink>();
	private Set<Link> active_links = new HashSet<Link>();
	
	public void setK(double k){
		K = k;
		G = defaultG();
	}
	
	public void setAlpha(double a){
		alpha = a;
		G = defaultG();
	}
	
	public void setVMax(double v){
		vmax = v;
	}
	
	public void setRange(double r){
		range = r;
		G = defaultG();
	}
	
	public void setEpsilon(double e){
		epsilon = e;
		G = defaultG();
	}
	
	public void setTau(double t){
		tau = t;
		G = defaultG();
	}
	
	public void setCutoff(double c){
		cutoff = c;
	}
	
	public void setLambda(double l){
		lambda = l;
		G = defaultG();
	}
	
	private double defaultG(){
		return K * Math.pow(epsilon+1, alpha)*range*(1-lambda/range); // balance at distance range
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
		if ( d2 < cutoff * cutoff ){
			double d = Math.sqrt(d2);
			Integer id = node.id();
			Integer oid = other_node.id();
			Link l = new Link(id, oid);
			double dt = 0;
			if ( active_links.contains(l) ){ // currently connected
				dt = window_map.get(l).minUpTime(time) / tps;
			}
			double F = G / Math.pow( epsilon + (d + vmax*dt)/range, alpha );
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
		Link l = new Link(id,oid);
		if ( active_links.contains(l) ){ // are connected
			double d = Math.sqrt( dx*dx + dy*dy );
			double F = K*(d-lambda);
			f.x = -F * dx/d;
			f.y = -F * dy/d;
		} else if ( window_map.containsKey(l) ){ // a window link exists between them
			double dt = window_map.get(new Link(id,oid)).minDownTime(time) / tps;
			double d = Math.sqrt( dx*dx + dy*dy );
			double F = K*(d-lambda)*Math.exp(-vmax*dt/tau);
			f.x += -F * dx/d;
			f.y += -F * dy/d;
		}
	}

	@Override
	public void setNodeCollection(Collection<Node> nodes) {
		_nodes = nodes;
	}

	@Override
	public Listener<LinkEvent> linkEventListener() {
		return new Listener<LinkEvent>(){
			@Override
			public void handle(long time, Collection<LinkEvent> events){
				for ( LinkEvent lev : events ){
					if ( lev.isUp() )
						active_links.add(lev.link());
					else
						active_links.remove(lev.link());
				}
			}
		};
	}

	@Override
	public Listener<Link> linkListener() {
		return new StatefulListener<Link>(){
			@Override
			public void reset() {
				active_links.clear();
			}

			@Override
			public void handle(long time, Collection<Link> events){
				for ( Link l : events )
					active_links.add(l);
			}
			
		};
	}

	@Override
	public Listener<WindowedLinkEvent> windowedLinkEventListener() {
		return new Listener<WindowedLinkEvent>(){
			@Override
			public void handle(long time, Collection<WindowedLinkEvent> events) {
				for ( WindowedLinkEvent wle : events ){
					Link l = wle.link();
					switch( wle.type() ){
					case WindowedLinkEvent.UP:
						window_map.put(l, new WindowedLink(l));
						break;
					case WindowedLinkEvent.DOWN:
						window_map.remove(l);
						break;
					default:
						window_map.get(l).handleEvent(wle);
					}
				}
			}
		};
	}

	@Override
	public Listener<WindowedLink> windowedLinkListener() {
		return new StatefulListener<WindowedLink>(){
			@Override
			public void reset() {
				window_map.clear();
			}

			@Override
			public void handle(long time, Collection<WindowedLink> events){
				Link l;
				for ( WindowedLink wl : events ){
					l = wl.link();
					window_map.put(l, wl);
				}
			}
			
		};
	}


}
