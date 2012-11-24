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

import java.io.IOException;

import ditl.*;



public final class UpperReachableConverter implements Converter {
	
	private ReachabilityTrace _lower;
	private ReachabilityTrace _upper;
	
	
	public UpperReachableConverter(ReachabilityTrace upper, ReachabilityTrace lower){
		_lower = lower;
		_upper = upper;
	}

	@Override
	public void convert() throws IOException {
		AdjacencyMap.Edges<EdgeEvent> to_bring_down = new AdjacencyMap.Edges<EdgeEvent>();
		
		StatefulWriter<EdgeEvent,Edge> upper_writer = _upper.getWriter(); 
		StatefulReader<EdgeEvent,Edge> lower_reader = _lower.getReader();
		
		lower_reader.seek(_lower.minTime());
		upper_writer.setInitState(_lower.minTime(), lower_reader.referenceState());
		long last_time = _lower.minTime();
		long time = last_time;
		long eta = _lower.eta();
		
		while ( lower_reader.hasNext() ){
			time = lower_reader.nextTime();
			if ( time > last_time + eta ){
				for ( EdgeEvent eev : to_bring_down.values() )
					upper_writer.queue(last_time, eev);
				to_bring_down.clear();
			}
			boolean first_down = true;
			for ( EdgeEvent eev : lower_reader.next() ){ // assumes that all UP event come before all DOWN events
				Edge e = eev.edge();
				if ( eev.isUp() ){
					if ( to_bring_down.containsKey(e) ){ // edge present at time and time-eta, do not remove
						to_bring_down.remove(e);
					} else {
						upper_writer.queue(time, eev);
					}
				} else { // down event. Just queue for next time step
					if ( first_down ){ // first flush previous time's down events
						for ( EdgeEvent dev : to_bring_down.values() )
							upper_writer.queue(last_time, dev);
						to_bring_down.clear();
						first_down = false;
					}
					to_bring_down.put(e, eev);
				}
			}
			upper_writer.flush(last_time);
			last_time = time;
		}
		
		// flush final events
		for ( EdgeEvent dev : to_bring_down.values() )
			upper_writer.queue(last_time, dev);
		upper_writer.flush();
		
		upper_writer.setPropertiesFromTrace(_lower);
		_lower.copyOverTraceInfo(upper_writer);
		upper_writer.close();
		lower_reader.close();
	}

}
