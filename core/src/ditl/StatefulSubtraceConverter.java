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
package ditl;

import java.io.IOException;
import java.util.List;

public class StatefulSubtraceConverter<E,S> implements Converter {

	private StatefulTrace<E,S> _to;
	private StatefulTrace<E,S> _from;
	private long _minTime, _maxTime;
	
	public StatefulSubtraceConverter( StatefulTrace<E,S> to, StatefulTrace<E,S> from, long minTime, long maxTime){
		_to = to;
		_from = from;
		_minTime = minTime;
		_maxTime = maxTime;
	}

	@Override
	public void convert() throws IOException {
		StatefulReader<E,S> reader = _from.getReader();
		StatefulWriter<E,S> writer = _to.getWriter();
		reader.seek(_minTime);
		writer.setInitState(_minTime, reader.referenceState());
		while ( reader.hasNext() && reader.nextTime() <= _maxTime){
			List<E> events = reader.next();
			for ( E item : events )
				writer.append(reader.time(), item);
		}
		writer.setProperty(Trace.minTimeKey, _minTime);
		writer.setProperty(Trace.maxTimeKey, _maxTime);
		writer.setPropertiesFromTrace(_from);
		reader.close();
		writer.close();
	}
}
