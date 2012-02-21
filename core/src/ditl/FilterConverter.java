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

public class FilterConverter<I> implements Converter {

	private Trace<I> _to;
	private Trace<I> _from;
	private Set<Integer> _group;
	
	public FilterConverter( Trace<I> to, Trace<I> from, Set<Integer> group ){
		_to = to;
		_from = from;
		_group = group;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void convert() throws IOException {
		Filter<I> filter = ((Trace.Filterable<I>)_from).eventFilter(_group);
		Reader<I> reader = _from.getReader();
		Writer<I> writer = _to.getWriter();
		reader.seek(_from.minTime());
		while ( reader.hasNext() ){
			List<I> events = reader.next();
			for ( I item : events ){
				I f_item = filter.filter(item);
				if ( f_item != null )
					writer.append(reader.time(), item);
			}
		}
		IdMap id_map = _from.idMap();
		if ( id_map != null ){
			IdMap.Writer id_map_writer = IdMap.Writer.filter(id_map, _group);
			writer.setProperty(Trace.idMapKey, id_map_writer.toString());
		}
		writer.setPropertiesFromTrace(_from);
		((Trace.Filterable<I>)_from).fillFilteredTraceInfo(writer);
		writer.close();
		reader.close();
	}
}
