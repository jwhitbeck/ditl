/*******************************************************************************
 * This file is part of DITL.                                                  *
 *                                                                             *
 * Copyright (C) 2011 John Whitbeck <john@whitbeck.fr>                         *
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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import ditl.Bus;
import ditl.Converter;
import ditl.Generator;
import ditl.Listener;
import ditl.Matcher;
import ditl.Runner;
import ditl.StatefulReader;
import ditl.StatefulWriter;
import ditl.Trace;

public class FloodingReachableConverter implements
        EdgeTrace.Handler, PresenceTrace.Handler, Generator, Converter {

    private final long _tau;
    private final long _delay;
    private final long min_time;

    private StatefulWriter<ArcEvent, Arc> arc_writer;

    private final Set<Arc> state = new TreeSet<Arc>();
    private final Set<Integer> present = new HashSet<Integer>();
    private final AdjacencySet.Arcs rev_matrix = new AdjacencySet.Arcs();
    private final AdjacencySet.Edges matrix = new AdjacencySet.Edges();
    private boolean started = false;

    private final EdgeTrace _edges;
    private final PresenceTrace _presence;
    private final ReachabilityTrace _reachability;

    private final Bus<Object> update_bus = new Bus<Object>();
    private final Bus<Infection> infection_bus = new Bus<Infection>();

    public FloodingReachableConverter(ReachabilityTrace reachability, PresenceTrace presence,
            EdgeTrace edges, long tau, long period, long minTime) {
        min_time = minTime;
        _edges = edges;
        _presence = presence;
        _reachability = reachability;
        _tau = tau;
        _delay = period;
        update_bus.addListener(new UpdateListener());
        infection_bus.addListener(new InfectionListener());
    }

    @Override
    public Listener<EdgeEvent> edgeEventListener() {
        return new Listener<EdgeEvent>() {
            @Override
            public void handle(long time, Collection<EdgeEvent> events) throws IOException {
                for (final EdgeEvent eev : events) {
                    final Edge e = eev.edge();
                    if (eev.isUp()) {
                        matrix.add(e);
                        final Set<Integer> already_inf_1 = rev_matrix.getNext(e.id1());
                        final Set<Integer> already_inf_2 = rev_matrix.getNext(e.id2());
                        if (already_inf_1 != null) {
                            final Arc a = new Arc(e.id1(), e.id2());
                            for (final Integer orig : already_inf_1)
                                if (already_inf_2 == null || !already_inf_2.contains(orig))
                                    infection_bus.queue(time + _tau, new Infection(orig, a));
                        }
                        if (already_inf_2 != null) {
                            final Arc a = new Arc(e.id2(), e.id1());
                            for (final Integer orig : already_inf_2)
                                if (already_inf_1 == null || !already_inf_1.contains(orig))
                                    infection_bus.queue(time + _tau, new Infection(orig, a));
                        }
                    } else {
                        matrix.remove(e);
                        infection_bus.removeFromQueueAfterTime(time, new EdgeMatcher(e));
                    }
                }
            }
        };
    }

    @Override
    public Listener<Edge> edgeListener() {
        return new Listener<Edge>() {
            @Override
            public void handle(long time, Collection<Edge> events) {
                for (final Edge e : events)
                    matrix.add(e);
            }
        };
    }

    @Override
    public void incr(long time) throws IOException {
    }

    @Override
    public void seek(long time) throws IOException {
        update_bus.queue(min_time, Collections.emptySet());
    }

    @Override
    public void convert() throws IOException {
        final StatefulReader<EdgeEvent, Edge> edge_reader = _edges.getReader();
        final StatefulReader<PresenceEvent, Presence> presence_reader = _presence.getReader();

        arc_writer = _reachability.getWriter();

        arc_writer.setProperty(ReachabilityTrace.delayKey, _delay);
        arc_writer.setProperty(Trace.timeUnitKey, _edges.timeUnit());
        arc_writer.setProperty(ReachabilityTrace.tauKey, _tau);
        arc_writer.setProperty(Trace.minTimeKey, _edges.minTime());
        arc_writer.setProperty(Trace.maxTimeKey, _edges.maxTime());

        edge_reader.stateBus().addListener(edgeListener());
        edge_reader.bus().addListener(edgeEventListener());

        presence_reader.stateBus().addListener(presenceListener());
        presence_reader.bus().addListener(presenceEventListener());

        final Runner runner = new Runner(_tau, _edges.minTime(), _edges.maxTime());
        runner.addGenerator(presence_reader);
        runner.addGenerator(edge_reader);
        runner.addGenerator(this);
        runner.run();

        arc_writer.flush();
        arc_writer.close();
        edge_reader.close();
        presence_reader.close();
    }

    @Override
    public Bus<?>[] busses() {
        return new Bus<?>[] { infection_bus, update_bus };
    }

    @Override
    public int priority() {
        return Integer.MAX_VALUE; // this should come after all other events
    }

    private final static class Infection {
        Integer _orig;
        Arc _arc;

        Infection(Integer orig, Arc arc) {
            _orig = orig;
            _arc = arc;
        }

        Arc arc() {
            return new Arc(_orig, _arc.to());
        }

        Integer rcpt() {
            return _arc.to();
        }

        @Override
        public String toString() {
            return arc().toString();
        }
    }

    private final class UpdateListener implements Listener<Object> {
        @Override
        public void handle(long time, Collection<Object> events) throws IOException {
            if (started) {
                // first handle previous time period
                final long t = time - _delay;
                if (t == _edges.minTime())
                    arc_writer.setInitState(min_time, state);
                else {
                    final Set<Arc> cur_state = arc_writer.states();
                    for (final Arc a : state)
                        if (!cur_state.contains(a))
                            arc_writer.queue(t, new ArcEvent(a, ArcEvent.Type.UP));
                    for (final Arc a : cur_state)
                        if (!state.contains(a))
                            arc_writer.queue(t, new ArcEvent(a, ArcEvent.Type.DOWN));
                    arc_writer.flush();
                }

                // then clear state and start new epidemic
                state.clear();
                rev_matrix.clear();
                infection_bus.reset();
            } else {
                started = true;
                if (min_time > _edges.minTime()) // starting after min_time =>
                                                 // empty initial state
                    arc_writer.setInitState(_edges.minTime(), Collections.<Arc> emptySet());
            }

            for (final Integer i : present) {
                rev_matrix.add(new Arc(i, i));
                broadcast(time, i, i);
            }

            update_bus.queue(time + _delay, Collections.emptySet());
        }
    }

    private final class InfectionListener implements Listener<Infection> {
        @Override
        public void handle(long time, Collection<Infection> events) throws IOException {
            for (final Infection infection : events) {
                final Arc a = infection.arc();
                state.add(a);
                rev_matrix.add(a.reverse());
                infection_bus.removeFromQueueAfterTime(time, new TransferMatcher(infection.rcpt(), infection._orig));
                broadcast(time, infection.rcpt(), infection._orig);
            }
        }
    }

    private void broadcast(long time, Integer id, Integer orig) {
        final Set<Integer> neighbs = matrix.getNext(id);
        if (neighbs != null)
            for (final Integer i : neighbs) {
                final Arc a = new Arc(orig, i);
                if (!state.contains(a)) {
                    final Infection inf = new Infection(orig, new Arc(id, i));
                    infection_bus.queue(time + _tau, inf);
                }
            }
    }

    private final class EdgeMatcher implements Matcher<Infection> {
        Edge _edge;

        EdgeMatcher(Edge edge) {
            _edge = edge;
        }

        @Override
        public boolean matches(Infection item) {
            return item._arc.edge().equals(_edge);
        }
    }

    private final class NodeMatcher implements Matcher<Infection> {
        Integer _id;

        NodeMatcher(Integer id) {
            _id = id;
        }

        @Override
        public boolean matches(Infection item) {
            return item._arc.from().equals(_id) || item._arc.to().equals(_id);
        }
    }

    private final class TransferMatcher implements Matcher<Infection> {
        Integer _id;
        Integer _orig;

        TransferMatcher(Integer id, Integer orig) {
            _id = id;
            _orig = orig;
        }

        @Override
        public boolean matches(Infection item) {
            return _orig.equals(item._orig) && item._arc.to().equals(_id);
        }
    }

    @Override
    public Listener<Presence> presenceListener() {
        return new Listener<Presence>() {
            @Override
            public void handle(long time, Collection<Presence> events) {
                for (final Presence p : events)
                    present.add(p.id());
            }
        };
    }

    @Override
    public Listener<PresenceEvent> presenceEventListener() {
        return new Listener<PresenceEvent>() {
            @Override
            public void handle(long time, Collection<PresenceEvent> events) {
                for (final PresenceEvent pev : events) {
                    final Integer id = pev.id();
                    if (pev.isIn()) {
                        present.add(id);
                        rev_matrix.add(new Arc(id, id));
                        broadcast(time, id, id);
                    } else {
                        present.remove(id);
                        infection_bus.removeFromQueueAfterTime(time, new NodeMatcher(id));
                    }
                }
            }
        };
    }
}
