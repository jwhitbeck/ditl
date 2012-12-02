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
import java.util.HashSet;
import java.util.Set;

public class FilterConverter implements Converter {

    private final Trace<?> _to;
    private final Trace<?> _from;
    private final Set<Integer> _group;

    public FilterConverter(Trace<?> to, Trace<?> from, Set<Integer> group) {
        _to = to;
        _from = from;
        _group = group;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void convert() throws IOException {
        final Filter filter = ((Trace.Filterable) _from).eventFilter(_group);
        final Reader reader = _from.getReader();
        final Writer writer = _to.getWriter();
        reader.seek(_from.minTime());

        if (_from instanceof StatefulTrace) {
            final Filter state_filter = ((StatefulTrace.Filterable) _from).stateFilter(_group);
            final Set initState = new HashSet();
            for (final Object state : ((StatefulReader) reader).referenceState()) {
                final Object f_state = state_filter.filter(state);
                if (f_state != null)
                    initState.add(state);
            }
            ((StatefulWriter) writer).setInitState(_from.minTime(), initState);
        }

        while (reader.hasNext()) {
            for (final Object item : reader.next()) {
                final Object f_item = filter.filter(item);
                if (f_item != null)
                    writer.append(reader.time(), (Item) item);
            }
        }
        final IdMap id_map = _from.idMap();
        if (id_map != null) {
            final IdMap.Writer id_map_writer = IdMap.Writer.filter(id_map, _group);
            writer.setProperty(Trace.idMapKey, id_map_writer.toString());
        }
        writer.setPropertiesFromTrace(_from);
        ((Trace.Filterable) _from).copyOverTraceInfo(writer);
        writer.close();
        reader.close();
    }
}
