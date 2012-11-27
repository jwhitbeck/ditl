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
package ditl.graphs.viz;

import java.awt.*;
import java.util.*;

import ditl.*;
import ditl.graphs.*;
import ditl.viz.Scene;



@SuppressWarnings("serial")
public class GraphScene extends Scene implements 
	MovementTrace.Handler, EdgeTrace.Handler, ArcTrace.Handler, GroupTrace.Handler {

	protected Map<Integer,NodeElement> nodes = new HashMap<Integer,NodeElement>();
	private Map<Edge,EdgeElement> edges = new AdjacencyMap.Edges<EdgeElement>();
	private Map<Edge,ArcElement> arcs = new AdjacencyMap.Edges<ArcElement>();
	private boolean showIds = false;
	private Map<Integer,Color> group_color_map = null;
	private IdMap id_map = null;
	
	public void setGroupColorMap(Map<Integer,Color> groupColorMap){
		group_color_map = groupColorMap;
	}
	
	public void setIdMap(IdMap idMap){
		id_map = idMap;
	}
	
	@Override
	public Listener<Movement> movementListener(){
		return new StatefulListener<Movement>(){
			@Override
			public void handle(long time, Collection<Movement> events) {
				for ( Movement m : events ){
					NodeElement node;;
					if ( id_map == null )
						node = new NodeElement(m);
					else
						node = new NodeElement(m, id_map.getExternalId(m.id()));
					node.setShowId(showIds);
					nodes.put(m.id(), node);
					addScaleListener(node);
				}
			}

			@Override
			public void reset() {
				nodes.clear();
			}
		};
	}
	
	@Override
	public Listener<MovementEvent> movementEventListener(){
		return new Listener<MovementEvent>(){
			@Override
			public void handle(long time, Collection<MovementEvent> events) {
				for ( MovementEvent mev : events ){
					NodeElement node;
					Integer id = mev.id(); 
					switch (mev.type() ){
					case IN:
						node = new NodeElement(mev.origMovement());
						nodes.put(id, node);
						addScaleListener(node);
						break;
						
					case OUT:
						removeScaleListener(nodes.get(id));
						nodes.remove(id);
						break;
						
					case NEW_DEST:
						node = nodes.get(id);
						node.updateMovement(time, mev); break;
					}
				}
			}
		};
	}

	@Override
	public Listener<EdgeEvent> edgeEventListener(){
		return new Listener<EdgeEvent>() {
			@Override
			public void handle(long time, Collection<EdgeEvent> events) {
				for ( EdgeEvent eev : events ){
					Edge e = eev.edge();
					if ( eev.isUp() ){
						NodeElement n1 = nodes.get(eev.id1());
						NodeElement n2 = nodes.get(eev.id2());
						EdgeElement edge = new EdgeElement(n1,n2);
						edges.put(e, edge);
					} else {
						edges.remove(e);
					}
				}
			}
		};
	}
	
	@Override
	public Listener<Edge> edgeListener(){
		return new StatefulListener<Edge>(){
			@Override
			public void handle(long time, Collection<Edge> events){
				for ( Edge e : events ){
					NodeElement n1 = nodes.get(e.id1());
					NodeElement n2 = nodes.get(e.id2());
					EdgeElement edge = new EdgeElement(n1,n2);
					edges.put(e, edge);
				}
			}

			@Override
			public void reset() {
				edges.clear();
			}
		};
	}
	
	@Override
	public Listener<ArcEvent> arcEventListener(){
		return new Listener<ArcEvent>() {
			@Override
			public void handle(long time, Collection<ArcEvent> events) {
				for ( ArcEvent aev : events ){
					Arc a = aev.arc();
					Edge e = a.edge();
					ArcElement ae;
					if ( aev.isUp() ){
						if ( ! arcs.containsKey(e) ){
							NodeElement n1 = nodes.get(aev.from());
							NodeElement n2 = nodes.get(aev.to());
							ae = new ArcElement(n1,n2);
							arcs.put(e, ae);
						}
						ae = arcs.get(e);
						ae.bringArcUp(a);
					} else {
						ae = arcs.get(e);
						ae.bringArcDown(a);
						if ( ae.state == ArcElement.State.DOWN ){
							arcs.remove(e);
						}
					}
				}
			}
		};
	}
	
	@Override
	public Listener<Arc> arcListener(){
		return new StatefulListener<Arc>(){
			@Override
			public void handle(long time, Collection<Arc> events){
				for ( Arc a : events ){
					Edge e = a.edge();
					ArcElement ae;
					if ( ! arcs.containsKey(e) ){
						NodeElement n1 = nodes.get(a.from());
						NodeElement n2 = nodes.get(a.to());
						ae = new ArcElement(n1,n2);
						arcs.put(e, ae);
					}
					arcs.get(e).bringArcUp(a);
				}
			}

			@Override
			public void reset() {
				arcs.clear();
			}
		};
	}
	
	public void setShowIds(boolean show){
		showIds = show;
		for ( NodeElement node : nodes.values() )
			node.setShowId(show);
		repaint();
	}
	
	public boolean getShowIds(){
		return showIds; 
	}
	
	@Override
	public void paint2D(Graphics2D g2){
		g2.setStroke(new BasicStroke(2));
		for ( ArcElement arc : arcs.values() )
			arc.paint(g2);
		g2.setStroke(new BasicStroke());
		g2.setColor(Color.BLACK);
		for ( EdgeElement edge : edges.values() )
			edge.paint(g2);
		for ( NodeElement node : nodes.values() )
			node.paint(g2);
	}

	
	@Override
	public void changeTime(long time){
		for ( NodeElement node : nodes.values() ){
			node.changeTime(time);
			node.rescale(this);
		}
		super.changeTime(time);
	}

	@Override
	public Listener<GroupEvent> groupEventListener() {
		return new Listener<GroupEvent>(){
			@Override
			public void handle(long time, Collection<GroupEvent> events) {
				for ( GroupEvent gev : events ){
					switch (gev.type()){
					case LEAVE:
						for ( Integer i : gev.members() ){
							NodeElement node = nodes.get(i);
							if ( node != null ) // node is null if it has also left the simulation
								node.setFillColor(GroupsPanel.noGroupColor);
						}
						break;
					case JOIN:
						for ( Integer i : gev.members() )
							nodes.get(i).setFillColor(group_color_map.get(gev.gid()));
						break;
					}
				}
			}
		};
	}

	@Override
	public Listener<Group> groupListener() {
		return new StatefulListener<Group>(){

			@Override
			public void reset() {
				for ( NodeElement node : nodes.values() )
					node.setFillColor(NodeElement.defaultFillColor);
			}

			@Override
			public void handle(long time, Collection<Group> events) {
				for ( NodeElement node : nodes.values() )
					node.setFillColor(GroupsPanel.noGroupColor);
				for ( Group group : events ){
					Color c =  group_color_map.get(group.gid());
					for ( Integer i : group.members() ){
						NodeElement node = nodes.get(i);
						if ( node != null )
							node.setFillColor(c);
					}
				}
			}
			
		};
	}
}
