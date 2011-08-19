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



public final class NumberContactsReport extends Report implements LinkHandler {

	private Map<Integer,Integer> contactsCount = new HashMap<Integer,Integer>();
	
	public NumberContactsReport(OutputStream out) throws IOException {
		super(out);
		appendComment("ID | Number of contacts");
	}
	
	
	public static ReportFactory<NumberContactsReport> factory(){
		return new ReportFactory<NumberContactsReport>() {
			@Override
			public NumberContactsReport getNew(OutputStream out) throws IOException {
				return new NumberContactsReport(out);
			}
		};
	}

	@Override
	public Listener<LinkEvent> linkEventListener() {
		return new Listener<LinkEvent>() {
			@Override
			public void handle(long time, Collection<LinkEvent> events) {
				for ( LinkEvent cev : events ){
					if ( cev.isUp() ){
						incr(cev.id1());
						incr(cev.id2());
					}
				}
			}
		};
	}
	
	private void incr(int id){
		Integer c = contactsCount.get(id);
		int cp = (c==null)? 1 : c+1; 
		contactsCount.put(id, cp);
	}

	@Override
	public Listener<Link> linkListener() {
		return new StatefulListener<Link>(){
			@Override
			public void handle(long time, Collection<Link> events) {
				for ( Link l : events ){
					incr(l.id1());
					incr(l.id2());
				}
			}

			@Override
			public void reset() {
				contactsCount.clear();
			}
		};
	}
	
	@Override
	public void finish() throws IOException {
		for ( Map.Entry<Integer, Integer> e : contactsCount.entrySet() )
			append(e.getKey()+" "+e.getValue());
		super.finish();
	}

}
