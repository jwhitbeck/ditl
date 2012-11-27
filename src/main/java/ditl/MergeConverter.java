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
import java.util.List;

public class MergeConverter<I> implements Converter {

    private final Trace<I> _to;
    private final Collection<Trace<I>> from_collection;

    public MergeConverter(Trace<I> to, Collection<Trace<I>> fromCollection) {
        _to = to;
        from_collection = fromCollection;
    }

    @Override
    public void convert() throws IOException {
        String time_unit = "s";
        long maxTime = -Trace.INFINITY;
        long minTime = Trace.INFINITY;
        for (final Trace<I> from : from_collection) {
            time_unit = from.timeUnit();
            if (from.minTime() < minTime)
                minTime = from.minTime();
            if (from.maxTime() > maxTime)
                maxTime = from.maxTime();
        }
        IdMap.Writer id_map_writer = null;
        final Writer<I> writer = _to.getWriter();
        for (final Trace<I> from : from_collection) {
            final IdMap id_map = from.idMap();
            if (id_map != null) {
                if (id_map_writer == null)
                    id_map_writer = new IdMap.Writer(0);
                id_map_writer.merge(id_map);
            }
            final Reader<I> reader = from.getReader();
            reader.seek(minTime);
            while (reader.hasNext()) {
                final List<I> events = reader.next();
                for (final I item : events)
                    writer.queue(reader.time(), item);
            }
            reader.close();
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
