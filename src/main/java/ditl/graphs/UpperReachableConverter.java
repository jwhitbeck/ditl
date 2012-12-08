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
package ditl.graphs;

import java.io.IOException;

import ditl.Converter;
import ditl.StatefulReader;
import ditl.StatefulWriter;

public final class UpperReachableConverter implements Converter {

    private final ReachabilityTrace _lower;
    private final ReachabilityTrace _upper;

    public UpperReachableConverter(ReachabilityTrace upper, ReachabilityTrace lower) {
        _lower = lower;
        _upper = upper;
    }

    @Override
    public void convert() throws IOException {
        final AdjacencyMap.Arcs<ArcEvent> to_bring_down = new AdjacencyMap.Arcs<ArcEvent>();

        final StatefulWriter<ArcEvent, Arc> upper_writer = _upper.getWriter();
        final StatefulReader<ArcEvent, Arc> lower_reader = _lower.getReader();

        lower_reader.seek(_lower.minTime());
        upper_writer.setInitState(_lower.minTime(), lower_reader.referenceState());
        long last_time = _lower.minTime();
        long time = last_time;
        final long eta = _lower.eta();

        while (lower_reader.hasNext()) {
            time = lower_reader.nextTime();
            if (time > last_time + eta) {
                for (final ArcEvent aev : to_bring_down.values())
                    upper_writer.queue(last_time, aev);
                to_bring_down.clear();
            }
            boolean first_down = true;
            for (final ArcEvent aev : lower_reader.next()) { // assumes that all
                                                             // UP event come
                                                             // before all DOWN
                                                             // events
                final Arc a = aev.arc();
                if (aev.isUp()) {
                    if (to_bring_down.containsKey(a))
                        to_bring_down.remove(a);
                    else
                        upper_writer.queue(time, aev);
                } else { // down event. Just queue for next time step
                    if (first_down) { // first flush previous time's down events
                        for (final ArcEvent dev : to_bring_down.values())
                            upper_writer.queue(last_time, dev);
                        to_bring_down.clear();
                        first_down = false;
                    }
                    to_bring_down.put(a, aev);
                }
            }
            upper_writer.flush(last_time);
            last_time = time;
        }

        // flush final events
        for (final ArcEvent dev : to_bring_down.values())
            upper_writer.queue(last_time, dev);
        upper_writer.flush();

        upper_writer.setPropertiesFromTrace(_lower);
        _lower.copyOverTraceInfo(upper_writer);
        upper_writer.close();
        lower_reader.close();
    }

}
