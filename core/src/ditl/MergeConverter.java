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

public class MergeConverter<I> implements Converter {

	private Trace<I> _to;
	private Collection<Trace<I>> from_collection;
	
	public MergeConverter( Trace<I> to, Collection<Trace<I>> fromCollection ){
		_to = to;
		from_collection = fromCollection;
	}

	@Override
	public void convert() throws IOException {
		long ticsPerSecond = 1L;
		long maxTime = -Trace.INFINITY;
		long minTime =  Trace.INFINITY;
		for ( Trace<I> from : from_collection ){
			ticsPerSecond = from.ticsPerSecond();
			if ( from.minTime() < minTime ) minTime = from.minTime();
			if ( from.maxTime() > maxTime ) maxTime = from.maxTime();
		}
		Writer<I> writer = _to.getWriter();
		for ( Trace<I> from : from_collection ){
			Reader<I> reader = from.getReader();
			reader.setBus(writer);
			reader.seek(minTime);
			while ( reader.hasNext() ){
				List<I> events = reader.next();
				for ( I item : events )
					writer.queue(reader.time(), item);
			}
			reader.close();
		}
		writer.flush();
		writer.setProperty(Trace.ticsPerSecondKey, ticsPerSecond);
		writer.setProperty(Trace.minTimeKey, minTime);
		writer.setProperty(Trace.maxTimeKey, maxTime);
		writer.close();
	}
}
