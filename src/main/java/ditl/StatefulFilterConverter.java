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
import java.util.List;
import java.util.Set;

public class StatefulFilterConverter<E, S> implements Converter {

    private final StatefulTrace<E, S> _to;
    private final StatefulTrace<E, S> _from;
    private final Set<Integer> _group;

    public StatefulFilterConverter(StatefulTrace<E, S> to, StatefulTrace<E, S> from, Set<Integer> group) {
        _to = to;
        _from = from;
        _group = group;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void convert() throws IOException {
        final Filter<E> event_filter = ((StatefulTrace.Filterable<E, S>) _from).eventFilter(_group);
        final Filter<S> state_filter = ((StatefulTrace.Filterable<E, S>) _from).stateFilter(_group);

        final StatefulReader<E, S> reader = _from.getReader();
        final StatefulWriter<E, S> writer = _to.getWriter();

        reader.seek(_from.minTime());
        final Set<S> initState = new HashSet<S>();
        for (final S state : reader.referenceState()) {
            final S f_state = state_filter.filter(state);
            if (f_state != null)
                initState.add(state);
        }
        writer.setInitState(_from.minTime(), initState);

        while (reader.hasNext()) {
            final List<E> events = reader.next();
            for (final E item : events) {
                final E f_item = event_filter.filter(item);
                if (f_item != null)
                    writer.append(reader.time(), item);
            }
        }
        final IdMap id_map = _from.idMap();
        if (id_map != null) {
            final IdMap.Writer id_map_writer = IdMap.Writer.filter(id_map, _group);
            writer.setProperty(Trace.idMapKey, id_map_writer.toString());
        }

        writer.setPropertiesFromTrace(_from);
        ((StatefulTrace.Filterable<E, S>) _from).copyOverTraceInfo(writer);
        writer.close();
        reader.close();
    }
}
