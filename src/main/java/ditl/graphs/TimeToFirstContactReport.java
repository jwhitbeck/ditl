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
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ditl.Listener;
import ditl.Report;
import ditl.ReportFactory;
import ditl.StatefulListener;

public final class TimeToFirstContactReport extends Report
        implements EdgeTrace.Handler, PresenceTrace.Handler {

    private final Map<Integer, Long> entry_times = new HashMap<Integer, Long>();
    private final Set<Integer> done = new HashSet<Integer>();

    public TimeToFirstContactReport(OutputStream out) throws IOException {
        super(out);
    }

    public static final class Factory implements ReportFactory<TimeToFirstContactReport> {
        @Override
        public TimeToFirstContactReport getNew(OutputStream out) throws IOException {
            return new TimeToFirstContactReport(out);
        }
    }

    @Override
    public Listener<EdgeEvent> edgeEventListener() {
        return new Listener<EdgeEvent>() {
            @Override
            public void handle(long time, Collection<EdgeEvent> events) throws IOException {
                for (final EdgeEvent event : events)
                    if (event.isUp()) {
                        handleNode(time, event.id1);
                        handleNode(time, event.id2);
                    }
            }
        };
    }

    private void handleNode(long time, Integer i) throws IOException {
        if (!done.contains(i)) {
            final long t2fc = time - entry_times.get(i);
            append(t2fc);
            done.add(i);
        }
    }

    @Override
    public Listener<Edge> edgeListener() {
        return new StatefulListener<Edge>() {
            @Override
            public void handle(long time, Collection<Edge> events) throws IOException {
                for (final Edge e : events) {
                    done.add(e.id1);
                    done.add(e.id2);
                    append(0);
                    append(0);
                }
            }

            @Override
            public void reset() {
                done.clear();
            }
        };
    }

    @Override
    public Listener<PresenceEvent> presenceEventListener() {
        return new Listener<PresenceEvent>() {
            @Override
            public void handle(long time, Collection<PresenceEvent> events) {
                for (final PresenceEvent pev : events)
                    if (pev.isIn())
                        entry_times.put(pev.id, time);
                    else {
                        entry_times.remove(pev.id);
                        done.remove(pev.id);
                    }
            }
        };
    }

    @Override
    public Listener<Presence> presenceListener() {
        return new StatefulListener<Presence>() {
            @Override
            public void handle(long time, Collection<Presence> events) {
                for (final Presence p : events)
                    entry_times.put(p.id, time);
            }

            @Override
            public void reset() {
                entry_times.clear();
            }

        };
    }
}
