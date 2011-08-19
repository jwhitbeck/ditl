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
package ditl.transfers.viz;

import java.awt.*;
import java.util.*;

import ditl.*;
import ditl.graphs.Link;
import ditl.graphs.viz.*;
import ditl.transfers.*;

@SuppressWarnings("serial")
public class TransferScene extends GraphScene implements TransferHandler {

	private Map<Link,LinkElement> active_transfers = new HashMap<Link,LinkElement>();
	private boolean show_transfers = true;

	@Override
	public Listener<TransferEvent> transferEventListener() {
		return new Listener<TransferEvent>(){
			@Override
			public void handle(long time, Collection<TransferEvent> events) {
				for ( TransferEvent tev : events ){
					Link l = tev.edge().link();
					if ( tev.type() == TransferEvent.START ){
						NodeElement n1 = nodes.get(l.id1());
						NodeElement n2 = nodes.get(l.id2());
						active_transfers.put(l, new LinkElement(n1,n2));
					} else {
						active_transfers.remove(l);
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
					Link l = transfer.edge().link();
					NodeElement n1 = nodes.get(l.id1());
					NodeElement n2 = nodes.get(l.id2());
					active_transfers.put(l, new LinkElement(n1,n2));
				}
			}

			@Override
			public void reset() {
				active_transfers.clear();
			}
		};
	}
	
	@Override
	public void paint2D(Graphics2D g2){
		g2.setColor(Color.RED);
		g2.setStroke(new BasicStroke(2));
		for ( LinkElement link : active_transfers.values() )
			link.paint(g2);
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
}
