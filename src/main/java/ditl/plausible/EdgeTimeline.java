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
package ditl.plausible;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ditl.StatefulWriter;
import ditl.graphs.Edge;
import ditl.graphs.EdgeEvent;

class EdgeTimeline {

    long prev_up = Long.MIN_VALUE;
    long prev_down = Long.MIN_VALUE;
    long next_up = Long.MAX_VALUE;
    long next_down = Long.MAX_VALUE;

    private long prev_up_tmp, prev_down_tmp, next_up_tmp, next_down_tmp;

    private final Edge _edge;

    private final TreeMap<Long, List<EdgeEvent>> buffer = new TreeMap<Long, List<EdgeEvent>>();
    private final StatefulWriter<WindowedEdgeEvent, WindowedEdge> window_writer;

    public EdgeTimeline(Edge edge, StatefulWriter<WindowedEdgeEvent, WindowedEdge> windowWriter) {
        _edge = edge;
        window_writer = windowWriter;
    }

    private void write_events_if_changed(long time) throws IOException {
        if (prev_up_tmp != prev_up) {
            prev_up = prev_up_tmp;
            window_writer.queue(time, new WindowedEdgeEvent(_edge, WindowedEdgeEvent.Type.PREVUP, prev_up));
        }
        if (prev_down_tmp != prev_down) {
            prev_down = prev_down_tmp;
            window_writer.queue(time, new WindowedEdgeEvent(_edge, WindowedEdgeEvent.Type.PREVDOWN, prev_down));
        }
        if (next_up_tmp != next_up) {
            next_up = next_up_tmp;
            window_writer.queue(time, new WindowedEdgeEvent(_edge, WindowedEdgeEvent.Type.NEXTUP, next_up));
        }
        if (next_down_tmp != next_down) {
            next_down = next_down_tmp;
            window_writer.queue(time, new WindowedEdgeEvent(_edge, WindowedEdgeEvent.Type.NEXTDOWN, next_down));
        }
    }

    private void update_tmp_values(long time) {

        prev_up_tmp = Long.MIN_VALUE;
        prev_down_tmp = Long.MIN_VALUE;
        next_up_tmp = Long.MAX_VALUE;
        next_down_tmp = Long.MAX_VALUE;

        for (final Map.Entry<Long, List<EdgeEvent>> e : buffer.entrySet()) {
            final long t = e.getKey();
            for (final EdgeEvent eev : e.getValue())
                if (eev.isUp()) {
                    if (t <= time && t > prev_up_tmp)
                        prev_up_tmp = t;
                    else if (t > time && t < next_up_tmp)
                        next_up_tmp = t;
                } else if (t <= time && t > prev_down_tmp)
                    prev_down_tmp = t;
                else if (t > time && t < next_down_tmp)
                    next_down_tmp = t;
        }
    }

    public void pop(long time, long window) throws IOException {
        buffer.pollFirstEntry();
        if (prev_up == time - window || prev_down == time - window)
            update(time);
    }

    public void queue(long time, EdgeEvent edgeEvent) {
        List<EdgeEvent> eventsAtTime = buffer.get(time);
        if (eventsAtTime == null) {
            eventsAtTime = new LinkedList<EdgeEvent>();
            buffer.put(time, eventsAtTime);
        }
        eventsAtTime.add(edgeEvent);
    }

    public void append(long time, long window, EdgeEvent edgeEvent) throws IOException {
        queue(time, edgeEvent);
        if (edgeEvent.isUp()) {
            if (next_up == Long.MAX_VALUE) {
                next_up = time;
                window_writer.queue(time - window, new WindowedEdgeEvent(_edge, WindowedEdgeEvent.Type.NEXTUP, next_up));
            }
        } else if (next_down == Long.MAX_VALUE) {
            next_down = time;
            window_writer.queue(time - window, new WindowedEdgeEvent(_edge, WindowedEdgeEvent.Type.NEXTDOWN, next_down));
        }
    }

    public void update(long time) throws IOException {
        update_tmp_values(time);
        write_events_if_changed(time);
    }

    public void expire(long time) throws IOException {
        window_writer.queue(time, new WindowedEdgeEvent(_edge, WindowedEdgeEvent.Type.DOWN));
    }

    public WindowedEdge windowedEdges() {
        final WindowedEdge wl = new WindowedEdge(_edge);
        wl.prev_up = prev_up;
        wl.prev_down = prev_down;
        wl.next_up = next_up;
        wl.next_down = next_down;
        return wl;
    }

}
