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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import ditl.Converter;
import ditl.Incrementable;
import ditl.Listener;
import ditl.Matcher;
import ditl.Runner;
import ditl.StatefulListener;
import ditl.StatefulReader;
import ditl.StatefulWriter;

public class MovementToEdgesConverter implements Incrementable, MovementTrace.Handler, Converter {

    private final double r2;
    private final long max_interval;
    private final Map<Integer, Movement> invalid_movements = new HashMap<Integer, Movement>();
    private final Map<Integer, Movement> valid_movements = new HashMap<Integer, Movement>();
    private StatefulWriter<EdgeEvent, Edge> edge_writer;
    private StatefulReader<MovementEvent, Movement> movement_reader;
    private final MovementTrace _movement;
    private final EdgeTrace _edges;
    private long cur_time;

    public MovementToEdgesConverter(EdgeTrace edges, MovementTrace movement,
            double range, long maxInterval) {
        _edges = edges;
        _movement = movement;
        r2 = range * range;
        max_interval = maxInterval;
    }

    @Override
    public Listener<Movement> movementListener() {
        return new StatefulListener<Movement>() {
            @Override
            public void handle(long time, Collection<Movement> events) throws IOException {
                for (final Movement m : events)
                    invalid_movements.put(m.id(), m);
                setInitialState(time);
            }

            @Override
            public void reset() {
                valid_movements.clear();
                invalid_movements.clear();
            }
        };
    }

    private void setInitialState(long time) throws IOException {
        final Set<Edge> initEdges = new AdjacencySet.Edges();
        final Iterator<Movement> i = invalid_movements.values().iterator();
        while (i.hasNext()) {
            final Movement im = i.next();
            for (final Movement vm : valid_movements.values()) {
                final long[] meetings = im.meetingTimes(vm, r2);
                if (meetings != null) {
                    final long begin = meetings[0], end = meetings[1];
                    final Edge e = new Edge(im.id(), vm.id());
                    if (begin < time) {
                        if (time <= end) {
                            initEdges.add(e); // edge is already up
                            if (end - time < max_interval) // edge goes down
                                                           // before
                                                           // max_interval
                                edge_writer.queue(end, new EdgeEvent(e, EdgeEvent.Type.DOWN));
                        }
                    } else if (begin - time < max_interval) {
                        edge_writer.queue(begin, new EdgeEvent(e, EdgeEvent.Type.UP));
                        if (end - time < max_interval)
                            edge_writer.queue(end, new EdgeEvent(e, EdgeEvent.Type.DOWN));
                    }
                }
            }
            i.remove();
            valid_movements.put(im.id(), im);
        }
        edge_writer.setInitState(time, initEdges);
    }

    @Override
    public Listener<MovementEvent> movementEventListener() {
        return new Listener<MovementEvent>() {
            @Override
            public void handle(long time, Collection<MovementEvent> events) throws IOException {
                for (final MovementEvent event : events) {
                    final Integer id = event.id();
                    switch (event.type) {
                        case IN:
                            invalid_movements.put(id, event.origMovement());
                            break;

                        case OUT:
                            valid_movements.remove(id);
                            invalidNodeMeetings(time, id);
                            break;

                        default:
                            Movement m;
                            if (invalid_movements.containsKey(id)) {
                                m = invalid_movements.get(id);
                                m.handleEvent(time, event);
                            } else {
                                m = valid_movements.remove(id);
                                invalidNodeMeetings(time, id);
                                m.handleEvent(time, event);
                                invalid_movements.put(id, m);
                            }
                    }
                }
                edge_writer.flush(time);
                updateNextMeetings(time);
            }
        };
    }

    private void updateNextMeetings(long time) {
        final Iterator<Movement> i = invalid_movements.values().iterator();
        while (i.hasNext()) {
            final Movement m = i.next();
            for (final Movement vm : valid_movements.values()) {
                final long[] meetings = m.meetingTimes(vm, r2);
                if (meetings != null) {
                    final long begin = meetings[0], end = meetings[1];
                    final Edge e = new Edge(m.id(), vm.id());
                    if (begin >= time && begin - time < max_interval)
                        edge_writer.queue(begin, new EdgeEvent(e, EdgeEvent.Type.UP));
                    if (end >= time && end - time < max_interval) // edge goes
                                                                  // down before
                                                                  // max_interval
                        edge_writer.queue(end, new EdgeEvent(e, EdgeEvent.Type.DOWN));
                }
            }
            valid_movements.put(m.id(), m);
            i.remove();
        }
    }

    private void invalidNodeMeetings(final long time, final Integer i) {
        edge_writer.removeFromQueueAfterTime(time, new Matcher<EdgeEvent>() {
            @Override
            public boolean matches(EdgeEvent item) {
                return item.edge().hasVertex(i);
            }
        });
    }

    @Override
    public void incr(long dt) throws IOException {
        edge_writer.flush(cur_time);
        cur_time += dt;
    }

    @Override
    public void seek(long time) throws IOException {
        cur_time = time;
    }

    @Override
    public void convert() throws IOException {
        edge_writer = _edges.getWriter();
        movement_reader = _movement.getReader();

        movement_reader.stateBus().addListener(movementListener());
        movement_reader.bus().addListener(movementEventListener());

        final Runner runner = new Runner(_movement.maxUpdateInterval(), _movement.minTime(), _movement.maxTime());
        runner.addGenerator(movement_reader);
        runner.add(this);
        runner.run();

        edge_writer.flush(_movement.maxTime());
        edge_writer.setPropertiesFromTrace(_movement);
        edge_writer.close();
        movement_reader.close();
    }
}
