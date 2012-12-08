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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MergeConverter implements Converter {

    private final Trace<?> _to;
    private final Collection<Trace<?>> from_collection;

    public MergeConverter(Trace<?> to, Collection<Trace<?>> fromCollection) {
        _to = to;
        from_collection = fromCollection;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void convert() throws IOException {
        String time_unit = "s";
        long maxTime = Long.MIN_VALUE;
        long minTime = Long.MAX_VALUE;
        for (final Trace<?> from : from_collection) {
            time_unit = from.timeUnit();
            if (_to instanceof StatefulTrace) {
                // stateful traces have a first init state. They are not defined
                // prior to that state.
                if (minTime == Long.MAX_VALUE || from.minTime() > minTime) {
                    minTime = from.minTime();
                }
            } else if (from.minTime() < minTime) {
                minTime = from.minTime();
            }
            if (from.maxTime() > maxTime)
                maxTime = from.maxTime();

        }
        IdMap.Writer id_map_writer = null;
        final Writer writer = _to.getWriter();
        Set initState = new HashSet();
        for (final Trace<?> from : from_collection) {
            final IdMap id_map = from.idMap();
            if (id_map != null) {
                if (id_map_writer == null)
                    id_map_writer = new IdMap.Writer(0);
                id_map_writer.merge(id_map);
            }
            final Reader reader = from.getReader();
            reader.seek(minTime);
            if (_to instanceof StatefulTrace) {
                initState.addAll(((StatefulReader) reader).referenceState());
            }
            while (reader.hasNext()) {
                final List<? extends Item> events = reader.next();
                for (final Item item : events)
                    writer.queue(reader.time(), item);
            }
            reader.close();
        }
        if (_to instanceof StatefulTrace) {
            ((StatefulWriter) writer).setInitState(minTime, initState);
        }
        writer.flush();
        writer.setProperty(Trace.timeUnitKey, time_unit);
        writer.setProperty(Trace.minTimeKey, minTime);
        writer.setProperty(Trace.maxTimeKey, maxTime);
        if (id_map_writer != null)
            id_map_writer.writeTraceInfo(writer);
        writer.close();
    }
}
