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

	private StatefulWriter<E,S> _to;
	private Collection<StatefulReader<E,S>> from_collection;
	
	public StatefulMergeConverter( StatefulWriter<E,S> to, Collection<StatefulReader<E,S>> fromCollection ){
		_to = to;
		from_collection = fromCollection;
	}
	
	@Override
	public void close() throws IOException {
		_to.close();
	}

	@Override
	public void run() throws IOException {
		long ticsPerSecond = 1L;
		long maxTime = Trace.INFINITY;
		long minTime = -Trace.INFINITY;
		Set<S> initState = new HashSet<S>();
		for ( StatefulReader<E,S> from : from_collection ){
			Trace trace = from.trace();
			ticsPerSecond = trace.ticsPerSecond();
			if ( trace.minTime() > minTime ) minTime = trace.minTime(); // stateful traces have a first init state. They are not defined prior to that state. 
			if ( trace.maxTime() > maxTime ) maxTime = trace.maxTime();
		}
		for ( StatefulReader<E,S> from : from_collection ){
			from.setBus(_to);
			from.seek(minTime);
			initState.addAll(from.referenceState());
			while ( from.hasNext() ){
				List<E> events = from.next();
				for ( E item : events )
					_to.queue(from.time(), item);
			}
		}
		_to.setInitState(minTime, initState);
		_to.flush();
		_to.setProperty(Trace.ticsPerSecondKey, ticsPerSecond);
		_to.setProperty(Trace.minTimeKey, minTime);
		_to.setProperty(Trace.maxTimeKey, maxTime);
	}
}
