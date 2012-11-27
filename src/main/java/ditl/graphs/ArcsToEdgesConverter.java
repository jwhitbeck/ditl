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
import java.util.Set;

import ditl.Converter;
import ditl.StatefulReader;
import ditl.StatefulWriter;

public final class ArcsToEdgesConverter implements Converter {

    public final static boolean UNION = true;
    public final static boolean INTERSECTION = false;

    private final boolean _union;
    private final Set<Arc> arcs = new AdjacencySet.Arcs();
    private StatefulWriter<EdgeEvent, Edge> edge_writer;
    private StatefulReader<ArcEvent, Arc> arc_reader;
    private final EdgeTrace _edges;
    private final ArcTrace _arcs;

    public ArcsToEdgesConverter(EdgeTrace edges, ArcTrace arcs, boolean union) {
        _arcs = arcs;
        _edges = edges;
        _union = union;
    }

    private void setInitStateFromArcs(long time, Set<Arc> states) throws IOException {
        final Set<Edge> contacts = new AdjacencySet.Edges();
        for (final Arc arc : states) {
            arcs.add(arc);
            if (_union)
                contacts.add(arc.edge());
            else if (arcs.contains(arc.reverse()))
                contacts.add(arc.edge());
        }
        edge_writer.setInitState(time, contacts);
    }

    private void handleArcEvent(long time, ArcEvent event) throws IOException {
        final Arc a = event.arc();
        final Edge e = a.edge();
        if (_union && !arcs.contains(a.reverse())) {
            if (event.isUp())
                edge_writer.append(time, new EdgeEvent(e, EdgeEvent.Type.UP));
            else
                edge_writer.append(time, new EdgeEvent(e, EdgeEvent.Type.DOWN));
        } else if (!_union && arcs.contains(a.reverse()))
            if (event.isUp())
                edge_writer.append(time, new EdgeEvent(e, EdgeEvent.Type.UP));
            else
                edge_writer.append(time, new EdgeEvent(e, EdgeEvent.Type.DOWN));
        if (event.isUp())
            arcs.add(a);
        else
            arcs.remove(a);
    }

    @Override
    public void convert() throws IOException {
        arc_reader = _arcs.getReader();
        edge_writer = _edges.getWriter();
        final long minTime = _arcs.minTime();
        arc_reader.seek(minTime);
        setInitStateFromArcs(minTime, arc_reader.referenceState());
        while (arc_reader.hasNext())
            for (final ArcEvent event : arc_reader.next())
                handleArcEvent(arc_reader.time(), event);
        edge_writer.setPropertiesFromTrace(_arcs);
        arc_reader.close();
        edge_writer.close();
    }
}
