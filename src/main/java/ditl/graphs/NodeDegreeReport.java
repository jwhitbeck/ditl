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
import java.util.Map;

import ditl.Listener;
import ditl.ReportFactory;
import ditl.StateTimeReport;
import ditl.StatefulListener;

public final class NodeDegreeReport extends StateTimeReport implements EdgeTrace.Handler, PresenceTrace.Handler {

    private final Map<Integer, Integer> degrees = new HashMap<Integer, Integer>();

    public NodeDegreeReport(OutputStream out) throws IOException {
        super(out);
        appendComment("time | duration | node degree distribution");
    }

    public static final class Factory implements ReportFactory<NodeDegreeReport> {
        @Override
        public NodeDegreeReport getNew(OutputStream out) throws IOException {
            return new NodeDegreeReport(out);
        }
    }

    @Override
    public Listener<PresenceEvent> presenceEventListener() {
        return new Listener<PresenceEvent>() {
            @Override
            public void handle(long time, Collection<PresenceEvent> events) throws IOException {
                for (final PresenceEvent p : events)
                    if (p.isIn())
                        degrees.put(p.id, 0);
                    else
                        degrees.remove(p.id);
                update(time);
            }
        };
    }

    @Override
    public Listener<EdgeEvent> edgeEventListener() {
        return new Listener<EdgeEvent>() {
            @Override
            public void handle(long time, Collection<EdgeEvent> events) throws IOException {
                for (final EdgeEvent ce : events)
                    if (ce.isUp()) {
                        incrDegree(ce.id1, 1);
                        incrDegree(ce.id2, 1);
                    } else {
                        incrDegree(ce.id1, -1);
                        incrDegree(ce.id2, -1);
                    }
                update(time);
            }
        };
    }

    @Override
    public Listener<Presence> presenceListener() {
        return new StatefulListener<Presence>() {
            @Override
            public void handle(long time, Collection<Presence> events) throws IOException {
                for (final Presence p : events)
                    degrees.put(p.id, 0);
            }

            @Override
            public void reset() {
                degrees.clear();
            }
        };
    }

    @Override
    public Listener<Edge> edgeListener() {
        return new Listener<Edge>() {
            @Override
            public void handle(long time, Collection<Edge> events) throws IOException {
                for (final Edge c : events) {
                    incrDegree(c.id1, 1);
                    incrDegree(c.id2, 1);
                }
                update(time);
            }
        };
    }

    private void update(long time) throws IOException {
        final StringBuffer buffer = new StringBuffer();
        for (final Integer d : degrees.values())
            buffer.append(d + " ");
        append(time, buffer.toString());
    }

    private void incrDegree(Integer id, int incr) {
        final Integer d = degrees.get(id);
        degrees.put(id, d + incr);
    }
}
