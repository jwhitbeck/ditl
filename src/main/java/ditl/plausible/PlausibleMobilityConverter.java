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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import ditl.Converter;
import ditl.Listener;
import ditl.Runner;
import ditl.StatefulListener;
import ditl.StatefulReader;
import ditl.StatefulWriter;
import ditl.Trace;
import ditl.graphs.Edge;
import ditl.graphs.EdgeEvent;
import ditl.graphs.EdgeTrace;
import ditl.graphs.Movement;
import ditl.graphs.MovementEvent;
import ditl.graphs.MovementTrace;
import ditl.graphs.Presence;
import ditl.graphs.PresenceEvent;
import ditl.graphs.PresenceTrace;

public final class PlausibleMobilityConverter implements Converter,
        PresenceTrace.Handler, MovementTrace.Handler {

    private StatefulWriter<MovementEvent, Movement> writer;

    private final MovementTrace known_movement;
    private final EdgeTrace _edges;
    private final WindowedEdgeTrace windowed_edges;
    private final PresenceTrace _presence;
    private final MovementTrace _movement;

    private final Set<Integer> known_movement_ids = new HashSet<Integer>();
    private final Map<Integer, KnownNode> known_nodes = new HashMap<Integer, KnownNode>();
    private final Map<Integer, InferredNode> inferred_nodes = new HashMap<Integer, InferredNode>();
    private final List<Node> all_nodes = new LinkedList<Node>();

    private final List<Constraint> global_constraints = new LinkedList<Constraint>();
    private final List<Force> global_forces = new LinkedList<Force>();
    private final Map<Integer, List<Constraint>> node_constraints = new HashMap<Integer, List<Constraint>>();

    private final double _height, _width;
    private final boolean _overlap;

    private final static long rng_seed = 0;
    private final Random rng = new Random(rng_seed);

    private final long update_interval;
    private final long tps;
    private final long incr_interval;
    private final int n_steps;
    private final long warm_time;

    public static final int defaultNSteps = 100; // by default, calculate 100
                                                 // intermediate points between
                                                 // successive updates
    public static final long defaultWarmTime = 100; // by default warming period
                                                    // is equivalent to 100s of
                                                    // mobility
    public static final double defaultTubeWidth = 10; // width of "tube" for
                                                      // approximating straight
                                                      // lines
    public static final double defaultStaticThresh = 0.1; // distance threshold
                                                          // for deciding
                                                          // whether a node is
                                                          // static or not
    public static final double defaultBorder = 10;
    public static final long defaultUpdateInterval = 1; // by default calculate
                                                        // positions every
                                                        // second
    public static final boolean defaultOverlap = true;

    private final double _e;
    private final double _s;

    public PlausibleMobilityConverter(MovementTrace movement,
            PresenceTrace presence, EdgeTrace edges,
            WindowedEdgeTrace windowedEdges, MovementTrace knownMovement,
            double width, double height, double e, double s,
            int nSteps, long updateInterval, long warmTime, boolean overlap) {

        _movement = movement;
        _presence = presence;
        _edges = edges;
        windowed_edges = windowedEdges;
        known_movement = knownMovement;
        _height = height;
        _width = width;
        tps = _presence.ticsPerSecond();
        warm_time = warmTime;
        n_steps = nSteps;
        update_interval = updateInterval;
        incr_interval = update_interval / n_steps;
        _overlap = overlap;
        _e = e;
        _s = s;
    }

    public void markKnownMovement(Integer... ids) {
        for (final Integer id : ids)
            known_movement_ids.add(id);
    }

    public void addGlobalConstraint(Constraint constraint) {
        global_constraints.add(constraint);
        if (constraint instanceof Interaction)
            ((Interaction) constraint).setNodeCollection(Collections.unmodifiableList(all_nodes));
    }

    public void addNodeConstraint(Integer id, Constraint constraint) {
        if (!node_constraints.containsKey(id))
            node_constraints.put(id, new LinkedList<Constraint>());
        node_constraints.get(id).add(constraint);
        if (constraint instanceof Interaction)
            ((Interaction) constraint).setNodeCollection(Collections.unmodifiableList(all_nodes));
    }

    public void addGlobalForce(Force force) {
        global_forces.add(force);
        if (force instanceof Interaction)
            ((Interaction) force).setNodeCollection(Collections.unmodifiableList(all_nodes));
    }

    @Override
    public void convert() throws IOException {
        final long min_time = _presence.minTime();
        final long max_time = _presence.maxTime();

        StatefulReader<MovementEvent, Movement> known_reader = null;
        writer = _movement.getWriter();

        // init event readers
        final StatefulReader<PresenceEvent, Presence> presence_reader = _presence.getReader();
        final StatefulReader<EdgeEvent, Edge> edge_reader = _edges.getReader();
        final StatefulReader<WindowedEdgeEvent, WindowedEdge> window_reader = windowed_edges.getReader();

        if (known_movement != null)
            known_reader = known_movement.getReader();

        // add bus listeners
        presence_reader.stateBus().addListener(this.presenceListener());
        presence_reader.bus().addListener(this.presenceEventListener());
        for (final Force force : global_forces)
            if (force instanceof PresenceTrace.Handler) {
                final PresenceTrace.Handler phf = (PresenceTrace.Handler) force;
                presence_reader.stateBus().addListener(phf.presenceListener());
                presence_reader.bus().addListener(phf.presenceEventListener());
            }

        for (final Force force : global_forces)
            if (force instanceof EdgeTrace.Handler) {
                final EdgeTrace.Handler lhf = (EdgeTrace.Handler) force;
                edge_reader.stateBus().addListener(lhf.edgeListener());
                edge_reader.bus().addListener(lhf.edgeEventListener());
            }

        for (final Force force : global_forces)
            if (force instanceof WindowedEdgeTrace.Handler) {
                final WindowedEdgeTrace.Handler wlhf = (WindowedEdgeTrace.Handler) force;
                window_reader.stateBus().addListener(wlhf.windowedEdgesListener());
                window_reader.bus().addListener(wlhf.windowedEdgesEventListener());
            }

        if (known_reader != null) {
            known_reader.stateBus().addListener(this.movementListener());
            known_reader.bus().addListener(this.movementEventListener());
        }

        final Runner runner = new Runner(incr_interval, min_time, max_time);
        runner.addGenerator(presence_reader);
        runner.addGenerator(edge_reader);
        runner.addGenerator(window_reader);
        if (known_reader != null)
            runner.addGenerator(known_reader);

        runner.seek(min_time);

        // get initial state
        final Set<Movement> init_mv = new HashSet<Movement>();

        warm(min_time);

        for (final InferredNode node : inferred_nodes.values())
            init_mv.add(new Movement(node.id(), node.cur));
        for (final KnownNode node : known_nodes.values())
            init_mv.add(node._movement);

        writer.setInitState(min_time, init_mv);

        long cur_time = min_time;
        long prev_time;

        // infer node mobility
        while (cur_time < max_time) {
            prev_time = cur_time;
            runner.incr();
            cur_time = runner.time();
            step(cur_time);
            if ((cur_time - min_time) % update_interval == 0) {
                long min_last_time = Trace.INFINITY;
                for (final Node node : inferred_nodes.values()) {
                    node.writeMovement(cur_time, prev_time, _s, _e, writer);
                    if (node.lastRefTime() < min_last_time)
                        min_last_time = node.lastRefTime();
                }
                writer.flush(min_last_time - update_interval);
            }
        }
        writer.flush();

        writer.setPropertiesFromTrace(_presence);
        writer.close();
        edge_reader.close();
        presence_reader.close();
        window_reader.close();
        if (known_reader != null)
            known_reader.close();

    }

    @Override
    public Listener<Movement> movementListener() {
        return new StatefulListener<Movement>() {
            @Override
            public void reset() {
                known_nodes.clear();
            }

            @Override
            public void handle(long time, Collection<Movement> events)
                    throws IOException {
                Integer id;
                for (final Movement mv : events) {
                    id = mv.id();
                    if (known_movement_ids.contains(id)) {
                        final KnownNode node = new KnownNode(id, mv);
                        known_nodes.put(id, node);
                        all_nodes.add(node);
                    }
                }
            }
        };
    }

    @Override
    public Listener<MovementEvent> movementEventListener() {
        return new Listener<MovementEvent>() {
            @Override
            public void handle(long time, Collection<MovementEvent> events)
                    throws IOException {
                KnownNode node;
                Integer id;
                for (final MovementEvent mev : events) {
                    id = mev.id();
                    if (known_movement_ids.contains(id)) {
                        switch (mev.type()) {
                            case IN:
                                node = new KnownNode(id, mev.origMovement());
                                known_nodes.put(id, node);
                                all_nodes.add(node);
                                break;
                            case OUT:
                                node = known_nodes.remove(id);
                                all_nodes.remove(node);
                                break;
                            default:
                                node = known_nodes.get(id);
                                node.updateMovement(time, mev);
                        }
                        writer.queue(time, mev);
                    }
                }
            }
        };
    }

    @Override
    public Listener<Presence> presenceListener() {
        return new StatefulListener<Presence>() {
            @Override
            public void reset() {
                inferred_nodes.clear();
            }

            @Override
            public void handle(long time, Collection<Presence> events)
                    throws IOException {
                Integer id;
                for (final Presence p : events) {
                    id = p.id();
                    if (!known_movement_ids.contains(id))
                        initInferredNode(id);
                }
            }
        };
    }

    @Override
    public Listener<PresenceEvent> presenceEventListener() {
        return new Listener<PresenceEvent>() {
            @Override
            public void handle(long time, Collection<PresenceEvent> events)
                    throws IOException {
                Integer id;
                for (final PresenceEvent pev : events) {
                    id = pev.id();
                    if (!known_movement_ids.contains(id))
                        if (pev.isIn())
                            initInferredNode(id);
                        else
                            removeInferredNode(id);
                }
            }
        };
    }

    private void initInferredNode(Integer id) {
        final InferredNode node = new InferredNode(id);
        inferred_nodes.put(id, node);
        all_nodes.add(node);
        setForces(node);
        setConstraints(node);
        node.cur.x = rng.nextFloat() * _width;
        node.next.x = node.cur.x;
        node.cur.y = rng.nextFloat() * _height;
        node.next.y = node.cur.y;
    }

    private void removeInferredNode(Integer id) {
        final Node node = inferred_nodes.remove(id);
        all_nodes.remove(node);
    }

    private void setForces(InferredNode node) {
        for (final Force f : global_forces)
            node.addForce(f);
    }

    private void setConstraints(InferredNode node) {
        for (final Constraint cnstr : global_constraints)
            node.addConstraint(cnstr);
        if (node_constraints.containsKey(node.id()))
            for (final Constraint cnstr : node_constraints.get(node.id()))
                node.addConstraint(cnstr);
    }

    private void warm(long time) {
        int i = 0;
        final int ni = (int) (warm_time / incr_interval);
        do {
            step(time);
            ++i;
        } while (i < ni);

        for (final Node node : inferred_nodes.values()) {
            node.sampleCurrent();
            node.setReference(time, node.cur.copy());
        }
    }

    private void step(long time) {
        Collections.shuffle(all_nodes, rng);
        final double rdt = (double) incr_interval / (double) tps;
        for (final Node node : all_nodes) {
            node.step(time, rdt);
            if (_overlap)
                node.commit();
        }
        if (!_overlap)
            for (final Node node : all_nodes)
                node.commit();
    }
}
