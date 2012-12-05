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
import ditl.ReportFactory;
import ditl.StateTimeReport;
import ditl.StatefulListener;

public final class ClusteringCoefficientReport extends StateTimeReport
        implements PresenceTrace.Handler, EdgeTrace.Handler {

    private final AdjacencySet.Edges adjacency = new AdjacencySet.Edges();
    private final Map<Integer, Double> coeffs = new HashMap<Integer, Double>();
    private final boolean remove_leaves;

    public ClusteringCoefficientReport(OutputStream out, boolean removeLeaves) throws IOException {
        super(out);
        remove_leaves = removeLeaves;
        appendComment("time | duration | clustering coefficient distribution");
    }

    public static final class Factory implements ReportFactory<ClusteringCoefficientReport> {
        private final boolean remove_leaves;

        public Factory(boolean removeLeaves) {
            remove_leaves = removeLeaves;
        }

        @Override
        public ClusteringCoefficientReport getNew(OutputStream out) throws IOException {
            return new ClusteringCoefficientReport(out, remove_leaves);
        }
    }

    private void updateSurroundingCoeffs(Edge edge) {
        final Integer i1 = edge.id1;
        final Integer i2 = edge.id2;
        final Set<Integer> n1 = adjacency.getNext(i1);
        final Set<Integer> n2 = adjacency.getNext(i2);
        if (!n1.isEmpty() && !n2.isEmpty())
            for (final Integer k : n1)
                if (n2.contains(k))
                    updateCoeff(k);
        updateCoeff(i1);
        updateCoeff(i2);
    }

    private void updateCoeff(Integer i) {
        final Set<Integer> neighbs = adjacency.getNext(i);
        if (neighbs.isEmpty())
            coeffs.put(i, 0.0);
        else {
            final int k = neighbs.size();
            if (k < 2)
                coeffs.put(i, 0.0);
            else {
                final Set<Integer> buff = new HashSet<Integer>();
                int n_edges = 0;
                for (final Integer j : neighbs) {
                    final Set<Integer> j_neighbs = adjacency.getNext(j);
                    for (final Integer l : buff)
                        if (j_neighbs.contains(l))
                            n_edges++;
                    buff.add(j);
                }
                final double coeff = 2 * (double) n_edges / (k * (k - 1));
                coeffs.put(i, coeff);
            }
        }
    }

    private void update(long time) throws IOException {
        final StringBuffer buffer = new StringBuffer();
        // buffer.append(time);
        for (final Double c : coeffs.values())
            if (c > 0 || !remove_leaves)
                buffer.append(c + " ");
        append(time, buffer.toString());
    }

    @Override
    public Listener<PresenceEvent> presenceEventListener() {
        return new Listener<PresenceEvent>() {
            @Override
            public void handle(long time, Collection<PresenceEvent> events) {
                for (final PresenceEvent pev : events)
                    if (pev.isIn())
                        coeffs.put(pev.id, 0.0);
                    else
                        coeffs.remove(pev.id);
            }
        };
    }

    @Override
    public Listener<Presence> presenceListener() {
        return new StatefulListener<Presence>() {
            @Override
            public void reset() {
                coeffs.clear();
            }

            @Override
            public void handle(long time, Collection<Presence> events) {
                for (final Presence p : events)
                    coeffs.put(p.id, 0.0);
            }
        };
    }

    @Override
    public Listener<EdgeEvent> edgeEventListener() {
        return new Listener<EdgeEvent>() {
            Listener<EdgeEvent> adj_listener = adjacency.edgeEventListener();

            @Override
            public void handle(long time, Collection<EdgeEvent> events)
                    throws IOException {
                adj_listener.handle(time, events);
                for (final EdgeEvent eev : events)
                    updateSurroundingCoeffs(eev.edge());
                update(time);
            }
        };
    }

    @Override
    public Listener<Edge> edgeListener() {
        return new StatefulListener<Edge>() {
            StatefulListener<Edge> adj_listener = (StatefulListener<Edge>) (adjacency.edgeListener());

            @Override
            public void reset() {
                adj_listener.reset();
            }

            @Override
            public void handle(long time, Collection<Edge> events)
                    throws IOException {
                adj_listener.handle(time, events);
                for (final Edge e : events)
                    updateSurroundingCoeffs(e);
                update(time);
            }

        };
    }
}
