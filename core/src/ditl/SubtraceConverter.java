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
import java.util.List;

public class SubtraceConverter<I> implements Converter {

	private Writer<I> _to;
	private Reader<I> _from;
	private long _minTime, _maxTime;
	
	public SubtraceConverter( Writer<I> to, Reader<I> from, long minTime, long maxTime){
		_to = to;
		_from = from;
		_minTime = minTime;
		_maxTime = maxTime;
	}
	
	@Override
	public void close() throws IOException {
		_to.close();
	}

	@Override
	public void run() throws IOException {
		_from.seek(_minTime);
		while ( _from.hasNext() && _from.nextTime() <= _maxTime){
			List<I> events = _from.next();
			for ( I item : events )
				_to.append(_from.time(), item);
		}
		_to.setProperty(Trace.ticsPerSecondKey, _from.trace().ticsPerSecond());
		_to.setProperty(Trace.minTimeKey, _minTime);
		_to.setProperty(Trace.maxTimeKey, _maxTime);
	}
}
