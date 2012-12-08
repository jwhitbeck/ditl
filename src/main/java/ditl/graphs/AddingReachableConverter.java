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
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;

import ditl.Bus;
import ditl.Converter;
import ditl.Generator;
import ditl.Listener;
import ditl.Runner;
import ditl.StatefulReader;
import ditl.StatefulWriter;
import ditl.Trace;

public final class AddingReachableConverter implements Converter, Generator, Listener<Object> {

    private final AdjacencyMap<Arc, ArcInfo> arc_infos = new AdjacencyMap.Arcs<ArcInfo>();
    private final AdjacencySet<Arc> to_bring_down = new AdjacencySet.Arcs();
    private final AdjacencySet<Arc> to_bring_up = new AdjacencySet.Arcs();

    private final Bus<Object> update_bus = new Bus<Object>();
    private Composer[] composers;

    private StatefulWriter<ArcEvent, Arc> writer;
    private final ReachabilityFamily _family1, _family2;
    private final ReachabilityTrace added_trace;
    private final long _delay;
    private final long tau;
    private final long eta;
    private boolean init_state_set = false;
    private long min_time;

    public AddingReachableConverter(ReachabilityTrace addedTrace, ReachabilityFamily family1, ReachabilityFamily family2, long delay) {
        added_trace = addedTrace;
        _family1 = family1;
        _family2 = family2;
        _delay = delay;
        eta = family1.eta();
        tau = family1.tau();
    }

    @Override
    public void convert() throws IOException {
        writer = added_trace.getWriter();
        writer.setProperty(ReachabilityTrace.etaKey, eta);
        writer.setProperty(ReachabilityTrace.tauKey, tau);
        writer.setProperty(ReachabilityTrace.delayKey, _delay);

        if (tau == 0) {
            composers = new Composer[1];
            if (_delay == eta) // first step in the tau==0 case, we are "adding"
                               // R0 and R0
                composers[0] = new Composer(_family1.getMember(0), _family2.getMember(0), eta);
            else
                composers[0] = new Composer(_family1.getMember(0), _family2.getMember(0));
        } else {
            final int n = (int) (tau / eta);
            composers = new Composer[n];
            final int i = (int) ((_delay - _family1.delay() - _family2.delay()) / eta);
            if (i < 0)
                for (int k = 0; k < n; ++k)
                    composers[k] = new Composer(_family1.getMemberByOffset(i + k),
                            _family2.getMemberByOffset(-k));
            else
                for (int k = 0; k < n; ++k)
                    composers[k] = new Composer(_family1.getMemberByOffset(k),
                            _family2.getMemberByOffset(i - k));
        }

        update_bus.addListener(this);
        final Trace<?> trace = composers[0].delta_trace;
        min_time = trace.minTime();

        final Runner runner = new Runner(eta, trace.minTime(), trace.maxTime());
        for (final Composer composer : composers)
            runner.addGenerator(composer);
        runner.addGenerator(this);
        runner.run();

        update_bus.flush(trace.maxTime());
        writer.setPropertiesFromTrace(trace);
        writer.close();
        for (final Composer composer : composers)
            composer.close();
    }

    private void scheduleUpdate(long time) {
        update_bus.queue(time, Collections.<Object> emptyList());
    }

    private final class Waypoints {
        AdjacencySet.Arcs delta_arcs = new AdjacencySet.Arcs();
        AdjacencySet.Arcs mu_arcs = new AdjacencySet.Arcs();

        void addDeltaArc(Arc de) {
            delta_arcs.add(de.reverse());
            for (final Integer to : mu_arcs.getNext(de.to))
                if (!to.equals(de.from)) // prevent loops on self
                    increment(new Arc(de.from, to));
        }

        void addShiftedMuArc(Arc me) {
            mu_arcs.add(me);
            for (final Integer from : delta_arcs.getNext(me.from))
                if (!from.equals(me.to)) // prevent loops on self
                    increment(new Arc(from, me.to));
        }

        void removeDeltaArc(Arc de) {
            delta_arcs.remove(de.reverse());
            for (final Integer to : mu_arcs.getNext(de.to))
                if (!to.equals(de.from)) // prevent loops on self
                    decrement(new Arc(de.from, to));
        }

        void removeShiftedMuArc(Arc me) {
            mu_arcs.remove(me);
            for (final Integer from : delta_arcs.getNext(me.from))
                if (!from.equals(me.to)) // prevent loops on self
                    decrement(new Arc(from, me.to));
        }

    };

    private final class ArcInfo {
        Arc _arc;
        int score = 0;

        ArcInfo(Arc arc) {
            _arc = arc;
            score = 1; // increment at creation
            to_bring_up.add(_arc); // bring up at next incr
            arc_infos.put(_arc, this);
        }

        void decrement() {
            score--;
            if (score == 0)
                to_bring_down.add(_arc);
        }

        void increment() {
            if (score == 0)
                to_bring_down.remove(_arc);
            score++;
        }
    }

    @Override
    public void incr(long dt) throws IOException {
    }

    @Override
    public void seek(long time) throws IOException {
    }

    @Override
    public Bus<?>[] busses() {
        return new Bus<?>[] { update_bus };
    }

    @Override
    public int priority() {
        return Trace.lowestPriority;
    }

    void flushState(long time) throws IOException {
        if (time >= min_time) {
            if (!init_state_set) {
                writer.setInitState(min_time, to_bring_up);
                init_state_set = true;
            } else
                for (final Arc a : to_bring_up)
                    writer.append(time, new ArcEvent(a, ArcEvent.Type.UP));
            to_bring_up.clear();
            for (final Arc a : to_bring_down) {
                arc_infos.remove(a);
                writer.append(time, new ArcEvent(a, ArcEvent.Type.DOWN));
            }
            to_bring_down.clear();
        }
    }

    @Override
    public void handle(long time, Collection<Object> events) throws IOException {
        for (final Composer composer : composers)
            composer.processDeltaUpEvents();
        flushState(time - eta);
        for (final Composer composer : composers) {
            composer.processDeltaUpJourneyEvents();
            composer.processMuUpEvents();
        }
        for (final Composer composer : composers) {
            composer.processShiftedMuDownEvents();
            composer.processMuDownJourneyEvents();
            composer.processDeltaDownEvents();
        }
        scheduleUpdate(time + eta);
    }

    ArcInfo increment(Arc a) {
        ArcInfo ai = arc_infos.get(a);
        if (ai == null)
            ai = new ArcInfo(a);
        else
            ai.increment();
        return ai;
    }

    ArcInfo decrement(Arc a) {
        final ArcInfo ai = arc_infos.get(a);
        ai.decrement();
        return ai;
    }

    private final class Composer implements Generator {
        ReachabilityTrace delta_trace, mu_trace;
        StatefulReader<ArcEvent, Arc> delta_reader;
        StatefulReader<ArcEvent, Arc> mu_reader;
        Deque<Arc> mu_down_events = new LinkedList<Arc>();
        Deque<Arc> shifted_mu_down_events = new LinkedList<Arc>();
        Deque<Arc> mu_up_events = new LinkedList<Arc>();
        Deque<Arc> delta_down_events = new LinkedList<Arc>();
        Deque<Arc> delta_up_events = new LinkedList<Arc>();
        Waypoints waypoints = new Waypoints();

        Composer(ReachabilityTrace deltaTrace, ReachabilityTrace muTrace) throws IOException {
            init(deltaTrace, muTrace, deltaTrace.delay());
        }

        Composer(ReachabilityTrace deltaTrace, ReachabilityTrace muTrace, long offset) throws IOException {
            init(deltaTrace, muTrace, offset);
        }

        void init(ReachabilityTrace deltaTrace, ReachabilityTrace muTrace, long offset) throws IOException {
            delta_trace = deltaTrace;
            mu_trace = muTrace;
            delta_reader = delta_trace.getReader();
            delta_reader.stateBus().addListener(new DeltaListener());
            delta_reader.bus().addListener(new DeltaEventListener());
            mu_reader = mu_trace.getReader(Trace.defaultPriority, offset);
            mu_reader.stateBus().addListener(new MuListener());
            mu_reader.bus().addListener(new MuEventListener());
        }

        void processDeltaUpEvents() {
            for (final Arc a : delta_up_events) {
                ArcInfo ai = arc_infos.get(a);
                if (ai == null)
                    ai = new ArcInfo(a);
                else
                    ai.increment();
            }
        }

        void processDeltaUpJourneyEvents() {
            while (!delta_up_events.isEmpty()) {
                final Arc de = delta_up_events.poll();
                waypoints.addDeltaArc(de);
            }
        }

        void processDeltaDownEvents() {
            while (!delta_down_events.isEmpty()) {
                final Arc de = delta_down_events.poll();
                decrement(de);
                waypoints.removeDeltaArc(de);
            }
        }

        void processMuUpEvents() {
            while (!mu_up_events.isEmpty()) {
                final Arc me = mu_up_events.poll();
                increment(me);
                waypoints.addShiftedMuArc(me);
            }
        }

        void processMuDownJourneyEvents() {
            while (!mu_down_events.isEmpty()) {
                final Arc me = mu_down_events.poll();
                shifted_mu_down_events.addLast(me);
                waypoints.removeShiftedMuArc(me);
            }
        }

        void processShiftedMuDownEvents() {
            while (!shifted_mu_down_events.isEmpty()) {
                final Arc a = shifted_mu_down_events.poll();
                decrement(a);
            }
        }

        void close() throws IOException {
            delta_reader.close();
            mu_reader.close();
        }

        final class DeltaListener implements Listener<Arc> {
            @Override
            public void handle(long time, Collection<Arc> events) {
                for (final Arc a : events) {
                    increment(a);
                    waypoints.addDeltaArc(a);
                }
                scheduleUpdate(time + eta);
            }
        }

        final class DeltaEventListener implements Listener<ArcEvent> {
            @Override
            public void handle(long time, Collection<ArcEvent> events) {
                for (final ArcEvent aev : events)
                    if (aev.isUp())
                        delta_up_events.addLast(aev.arc());
                    else
                        delta_down_events.addLast(aev.arc());
                scheduleUpdate(time);
            }
        }

        final class MuListener implements Listener<Arc> {
            @Override
            public void handle(long time, Collection<Arc> events) {
                for (final Arc a : events) {
                    increment(a);
                    waypoints.addShiftedMuArc(a);
                }
                scheduleUpdate(time + eta);
            }
        }

        final class MuEventListener implements Listener<ArcEvent> {
            @Override
            public void handle(long time, Collection<ArcEvent> events) {
                for (final ArcEvent aev : events)
                    if (aev.isUp())
                        mu_up_events.addLast(aev.arc());
                    else
                        mu_down_events.addLast(aev.arc());
                scheduleUpdate(time);
            }
        }

        @Override
        public void incr(long dt) throws IOException {
            delta_reader.incr(dt);
            mu_reader.incr(dt);
        }

        @Override
        public void seek(long time) throws IOException {
            mu_down_events.clear();
            shifted_mu_down_events.clear();
            mu_up_events.clear();
            delta_down_events.clear();
            delta_up_events.clear();
            delta_reader.seek(time);
            mu_reader.seek(time);
        }

        @Override
        public Bus<?>[] busses() {
            return new Bus<?>[] {
                    delta_reader.stateBus(), delta_reader.bus(),
                    mu_reader.stateBus(), mu_reader.bus() };
        }

        @Override
        public int priority() {
            return Trace.defaultPriority;
        }
    }
}
