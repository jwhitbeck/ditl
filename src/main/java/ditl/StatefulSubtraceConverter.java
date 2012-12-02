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

public class StatefulSubtraceConverter<E extends Item, S extends Item> implements Converter {

    private final StatefulTrace<E, S> _to;
    private final StatefulTrace<E, S> _from;
    private final long _minTime, _maxTime;

    public StatefulSubtraceConverter(StatefulTrace<E, S> to, StatefulTrace<E, S> from, long minTime, long maxTime) {
        _to = to;
        _from = from;
        _minTime = minTime;
        _maxTime = maxTime;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void convert() throws IOException {
        final StatefulReader<E, S> reader = _from.getReader();
        final StatefulWriter<E, S> writer = _to.getWriter();
        reader.seek(_minTime);
        writer.setInitState(_minTime, reader.referenceState());
        while (reader.hasNext() && reader.nextTime() <= _maxTime) {
            final List<E> events = reader.next();
            for (final E item : events)
                writer.append(reader.time(), item);
        }
        writer.setProperty(Trace.minTimeKey, _minTime);
        writer.setProperty(Trace.maxTimeKey, _maxTime);
        writer.setPropertiesFromTrace(_from);
        if (_from instanceof Trace.Copyable<?>)
            ((Trace.Copyable<E>) _from).copyOverTraceInfo(writer);
        reader.close();
        writer.close();
    }
}
