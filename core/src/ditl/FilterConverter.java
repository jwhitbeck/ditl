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

public class FilterConverter<I> implements Converter {

	private Trace<I> _to;
	private Trace<I> _from;
	private Matcher<I> _matcher;
	
	public FilterConverter( Trace<I> to, Trace<I> from, Matcher<I> matcher ){
		_to = to;
		_from = from;
		_matcher = matcher;
	}

	@Override
	public void convert() throws IOException {
		Reader<I> reader = _from.getReader();
		Writer<I> writer = _to.getWriter();
		reader.seek(_from.minTime());
		while ( reader.hasNext() ){
			List<I> events = reader.next();
			for ( I item : events )
				if ( _matcher.matches(item) )
					writer.append(reader.time(), item);
		}
		writer.setProperty(Trace.ticsPerSecondKey, _from.ticsPerSecond());
		writer.setProperty(Trace.minTimeKey, _from.minTime());
		writer.setProperty(Trace.maxTimeKey, _from.maxTime());
		writer.close();
		reader.close();
	}
}
