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
package ditl.transfers.viz;

import java.awt.*;
import java.util.*;

import ditl.*;
import ditl.graphs.*;
import ditl.graphs.viz.*;
import ditl.transfers.*;

@SuppressWarnings("serial")
public class TransferScene extends GraphScene implements TransferTrace.Handler {

	private Map<Edge,EdgeElement> active_transfers = new AdjacencyMap.Edges<EdgeElement>();
	private Map<Edge,Integer> transfer_count = new AdjacencyMap.Edges<Integer>();
	private boolean show_transfers = true;

	@Override
	public Listener<TransferEvent> transferEventListener() {
		return new Listener<TransferEvent>(){
			@Override
			public void handle(long time, Collection<TransferEvent> events) {
				for ( TransferEvent tev : events ){
					Edge e = tev.arc().edge();
					if ( tev.type() == TransferEvent.Type.START ){
						incrTransfer(e);
					} else {
						decrTransfer(e);
					}
				}
			}
		};
	}

	@Override
	public Listener<Transfer> transferListener() {
		return new StatefulListener<Transfer>(){
			@Override
			public void handle(long time, Collection<Transfer> events) {
				for ( Transfer transfer : events ){
					Edge e = transfer.arc().edge();
					incrTransfer(e);
				}
			}

			@Override
			public void reset() {
				active_transfers.clear();
				transfer_count.clear();
			}
		};
	}
	
	@Override
	public void paint2D(Graphics2D g2){
		g2.setColor(Color.RED);
		g2.setStroke(new BasicStroke(2));
		for ( EdgeElement edge : active_transfers.values() )
			edge.paint(g2);
		super.paint2D(g2);
	}
	
	public void setShowTransfers(boolean show){
		show_transfers = show;
	}
	
	public boolean getShowTransfers(){
		return show_transfers;
	}
	
	public void setInfected(Set<Integer> infected){
		for ( Integer id : nodes.keySet() ){
			if ( infected.contains(id) )
				addInfected(id);
			else
				removeInfected(id);
		}
	}
	
	public void addInfected(Integer id){
		NodeElement node = nodes.get(id);
		node.setFillColor(Color.RED);
	}
	
	public void removeInfected(Integer id){
		NodeElement node = nodes.get(id);
		node.setFillColor(Color.BLUE);
	}
	
	private void incrTransfer(Edge e){
		if ( ! transfer_count.containsKey(e) ){
			transfer_count.put(e, 1);
			NodeElement n1 = nodes.get(e.id1());
			NodeElement n2 = nodes.get(e.id2());
			active_transfers.put(e, new EdgeElement(n1,n2));
		} else {
			int c = transfer_count.get(e);
			transfer_count.put(e, c+1);
		}
	}
	
	private void decrTransfer(Edge e){
		int c = transfer_count.get(e);
		if ( c > 1 ){
			transfer_count.put(e, c-1);
		} else {
			active_transfers.remove(e);
			transfer_count.remove(e);
		}
	}
}
