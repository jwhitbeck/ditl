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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import ditl.Bus;
import ditl.Converter;
import ditl.Generator;
import ditl.Listener;
import ditl.Runner;
import ditl.StatefulReader;
import ditl.StatefulWriter;
import ditl.Trace;

public class ConnectedComponentsToReachableConverter
        implements GroupTrace.Handler, Generator, Listener<Object>, Converter {

    private StatefulWriter<ArcEvent, Arc> arc_writer;
    private final Map<Integer, Group> group_map = new HashMap<Integer, Group>();
    private final GroupTrace _ccs;
    private final ReachabilityTrace _reachability;
    private final Deque<Arc> to_bring_up = new LinkedList<Arc>();
    private final Deque<Arc> to_bring_down = new LinkedList<Arc>();
    private final Bus<Object> update_bus = new Bus<Object>();
    private final long _eta;

    public ConnectedComponentsToReachableConverter(ReachabilityTrace reachability, GroupTrace connectedComponents, long eta) {
        _ccs = connectedComponents;
        _reachability = reachability;
        _eta = eta;
    }

    @Override
    public void convert() throws IOException {
        final StatefulReader<GroupEvent, Group> ccs_reader = _ccs.getReader();
        arc_writer = _reachability.getWriter();

        arc_writer.setProperty(ReachabilityTrace.delayKey, 0);
        arc_writer.setProperty(ReachabilityTrace.tauKey, 0);
        arc_writer.setProperty(ReachabilityTrace.etaKey, _eta);
        arc_writer.setPropertiesFromTrace(_ccs);

        ccs_reader.stateBus().addListener(groupListener());
        ccs_reader.bus().addListener(groupEventListener());
        update_bus.addListener(this);

        final Runner runner = new Runner(_ccs.maxUpdateInterval(), _ccs.minTime(), _ccs.maxTime() + 1);
        runner.addGenerator(ccs_reader);
        runner.addGenerator(this);
        runner.run();

        update_bus.flush(_ccs.maxTime());
        arc_writer.flush();
        arc_writer.close();
        ccs_reader.close();
    }

    @Override
    public Listener<Group> groupListener() {
        return new Listener<Group>() {
            @Override
            public void handle(long time, Collection<Group> events) throws IOException {
                final AdjacencySet.Arcs init_state = new AdjacencySet.Arcs();
                for (final Group g : events) {
                    for (final Integer i : g.members())
                        for (final Integer j : g.members())
                            if (!i.equals(j))
                                init_state.add(new Arc(i, j));
                    group_map.put(g.gid(), g);
                }
                arc_writer.setInitState(time, init_state);
            }
        };
    }

    @Override
    public Listener<GroupEvent> groupEventListener() {
        return new Listener<GroupEvent>() {
            @Override
            public void handle(long time, Collection<GroupEvent> events) throws IOException {
                for (final GroupEvent gev : events) {
                    Group g;
                    final Integer gid = gev.gid();
                    switch (gev.type()) {
                        case NEW:
                            g = new Group(gid);
                            group_map.put(gid, g);
                            break;
                        case JOIN:
                            g = group_map.get(gid);
                            join(g, gev);
                            scheduleUpdate(time);
                            g.handleEvent(gev);
                            break;
                        case LEAVE:
                            g = group_map.get(gid);
                            g.handleEvent(gev);
                            leave(g, gev);
                            scheduleUpdate(time);
                            break;
                        case DELETE:
                            g = group_map.get(gid);
                            group_map.remove(gid);
                    }
                }
            }
        };
    }

    private void scheduleUpdate(long time) {
        update_bus.queue(time, Collections.<Object> emptyList());
    }

    private void join(Group g, GroupEvent gev) {
        final Set<Arc> cur_state = arc_writer.states();
        // if g is empty, members of the group event may not be in contact
        if (g.members().isEmpty())
            for (final Integer i : gev.members())
                for (final Integer j : gev.members())
                    if (!i.equals(j)) {
                        final Arc a = new Arc(i, j);
                        if (!cur_state.contains(a))
                            to_bring_up.add(a);
                    }
        // merge both groups
        for (final Integer i : g.members())
            for (final Integer j : gev.members()) {
                to_bring_up.add(new Arc(i, j));
                to_bring_up.add(new Arc(j, i));
            }
    }

    private void leave(Group g, GroupEvent gev) {
        for (final Integer i : g.members())
            for (final Integer j : gev.members()) {
                to_bring_down.add(new Arc(i, j));
                to_bring_down.add(new Arc(j, i));
            }
    }

    @Override
    public void incr(long dt) throws IOException {
    }

    @Override
    public void seek(long time) throws IOException {
    }

    @Override
    public void handle(long time, Collection<Object> events) throws IOException {
        while (!to_bring_up.isEmpty())
            arc_writer.append(time, new ArcEvent(to_bring_up.poll(), ArcEvent.Type.UP));
        while (!to_bring_down.isEmpty())
            arc_writer.append(time, new ArcEvent(to_bring_down.poll(), ArcEvent.Type.DOWN));
    }

    @Override
    public Bus<?>[] busses() {
        return new Bus<?>[] { update_bus };
    }

    @Override
    public int priority() {
        return Trace.defaultPriority;
    }

}
