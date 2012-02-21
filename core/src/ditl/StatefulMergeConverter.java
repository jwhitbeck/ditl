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
package ditl;

import java.io.IOException;
import java.util.*;

public class StatefulMergeConverter<E,S> implements Converter {

	private StatefulTrace<E,S> _to;
	private Collection<StatefulTrace<E,S>> from_collection;
	
	public StatefulMergeConverter( StatefulTrace<E,S> to, Collection<StatefulTrace<E,S>> fromCollection ){
		_to = to;
		from_collection = fromCollection;
	}

	@Override
	public void convert() throws IOException {
		String time_unit = "s";
		long maxTime = Trace.INFINITY;
		long minTime = -Trace.INFINITY;
		long snap_interval = 1L;
		Set<S> initState = new HashSet<S>();
		for ( StatefulTrace<E,S> from : from_collection ){
			time_unit = from.timeUnit();
			snap_interval = from.snapshotInterval();
			if ( from.minTime() > minTime ) minTime = from.minTime(); // stateful traces have a first init state. They are not defined prior to that state. 
			if ( from.maxTime() < maxTime ) maxTime = from.maxTime();
		}
		IdMap.Writer id_map_writer = null;
		StatefulWriter<E,S> writer = _to.getWriter(snap_interval);
		for ( StatefulTrace<E,S> from : from_collection ){
			IdMap id_map = from.idMap();
			if ( id_map != null ){
				if ( id_map_writer == null )
					id_map_writer = new IdMap.Writer(0);
				id_map_writer.merge(id_map);
			}
			StatefulReader<E,S> reader = from.getReader();
			reader.seek(minTime);
			initState.addAll(reader.referenceState());
			while ( reader.hasNext() ){
				List<E> events = reader.next();
				for ( E item : events )
					writer.queue(reader.time(), item);
			}
			reader.close();
		}
		writer.setInitState(minTime, initState);
		writer.flush();
		writer.setProperty(Trace.timeUnitKey, time_unit);
		writer.setProperty(Trace.minTimeKey, minTime);
		writer.setProperty(Trace.maxTimeKey, maxTime);
		if ( id_map_writer != null )
			id_map_writer.writeTraceInfo(writer);
		writer.close();
	}
}
