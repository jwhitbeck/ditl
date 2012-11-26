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



public final class ReachabilityReport extends StateTimeReport implements ArcTrace.Handler, PresenceTrace.Handler {

	private Set<Arc> arcs = new AdjacencySet.Arcs();
	private int n_bidir;
	private int n_dir;
	private int samp_n_bidir;
	private int samp_n_dir;
	private int n_total;
	private int n_nodes;
	
	public ReachabilityReport(OutputStream out) throws IOException {
		super(out);
		appendComment("time | duration | N bidir | N dir | Sampled N bidir | Sampled N dir | N total ");
	}
	
	public static final class Factory implements ReportFactory<ReachabilityReport> {
		@Override
		public ReachabilityReport getNew(OutputStream out) throws IOException {
			return new ReachabilityReport(out);
		}
	}
	
	@Override
	public Listener<PresenceEvent> presenceEventListener(){
		return new Listener<PresenceEvent>(){
			@Override
			public void handle(long time, Collection<PresenceEvent> events) throws IOException {
				int old = n_nodes;
				for ( PresenceEvent p : events ){
					if ( p.isIn() ){
						n_nodes += 1;
					} else {
						n_nodes -= 1;
					}
				}
				update_total();
				if ( old != n_nodes )
					update(time);
			}
		};
	}
	
	private void update_total(){
		n_total = (n_nodes*(n_nodes-1))/2;
	}
	
	@Override
	public Listener<Presence> presenceListener(){
		return new StatefulListener<Presence>(){
			@Override
			public void handle(long time, Collection<Presence> events) throws IOException {
				n_nodes = events.size();
				update_total();
			}

			@Override
			public void reset() {
				n_nodes = 0;
				n_total = 0;
			}
		};
	}
	
	
	private void update(long time) throws IOException {
		append(time,n_bidir+" "+n_dir+" "+samp_n_bidir+" "+samp_n_dir+" "+n_total);
	}

	@Override
	public Listener<ArcEvent> arcEventListener() {
		return new Listener<ArcEvent>(){
			@Override
			public void handle(long time, Collection<ArcEvent> events)
					throws IOException {
				int old_n_dir = n_dir;
				int old_n_bidir = n_bidir;
				int old_samp_n_bidir = samp_n_bidir;
				int old_samp_n_dir = samp_n_dir;
				boolean first_down = true; // we assume that all reachability events are ordered UP first DOWN second
				for ( ArcEvent aev : events ){
					Arc a = aev.arc();
					if ( aev.isUp() ){
						if ( arcs.contains(a.reverse()) ){
							n_bidir += 1;
							n_dir -= 1;
						} else {
							n_dir += 1;
						}
						arcs.add(a);
					} else {
						if ( first_down ){
							samp_n_bidir = n_bidir;
							samp_n_dir = n_dir;
							first_down = false;
						}
						if ( arcs.contains(a.reverse()) ){
							n_bidir -= 1;
							n_dir += 1;
						} else {
							n_dir -= 1;
						}
						arcs.remove(a);
					}
				}
				if ( first_down ){
					samp_n_bidir = n_bidir;
					samp_n_dir = n_dir;
					first_down = false;
				}
				if ( old_n_bidir != n_bidir || old_n_dir != n_dir || 
						old_samp_n_dir != samp_n_dir || old_samp_n_bidir != samp_n_bidir)
					update(time);
			}
		};
	}

	@Override
	public Listener<Arc> arcListener() {
		return new StatefulListener<Arc>(){
			@Override
			public void handle(long time, Collection<Arc> events)
					throws IOException {
				for ( Arc a : events ){
					if ( arcs.contains(a.reverse()) ){
						n_bidir += 1;
						n_dir -= 1;
					} else {
						n_dir += 1;
					}
					arcs.add(a);
				}
				samp_n_dir = n_dir;
				samp_n_bidir = n_bidir;
				update(time);
			}

			@Override
			public void reset() {
				arcs.clear();
				n_bidir = 0;
				n_dir = 0;
				samp_n_bidir = 0;
				samp_n_dir = 0;
			}
		};
	}
}
