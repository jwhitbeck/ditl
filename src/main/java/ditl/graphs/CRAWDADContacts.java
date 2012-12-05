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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;

import ditl.IdGenerator;
import ditl.StatefulReader;
import ditl.StatefulWriter;
import ditl.Trace;
import ditl.Units;

public class CRAWDADContacts {

    public static void fromCRAWDAD(EdgeTrace edges,
            InputStream in, double timeMul, long ticsPerSecond,
            long offset, IdGenerator idGen) throws IOException {

        final StatefulWriter<EdgeEvent, Edge> edgeWriter = edges.getWriter();
        final BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line;

        while ((line = br.readLine()) != null) {
            final String[] elems = line.split("[ \t]+");
            final Integer id1 = idGen.getInternalId(elems[0]);
            final Integer id2 = idGen.getInternalId(elems[1]);
            final long begin = (long) (Long.parseLong(elems[2]) * timeMul) + offset;
            final long end = (long) (Long.parseLong(elems[3]) * timeMul) + offset;
            edgeWriter.queue(begin, new EdgeEvent(id1, id2, EdgeEvent.Type.UP));
            edgeWriter.queue(end, new EdgeEvent(id1, id2, EdgeEvent.Type.DOWN));
        }
        edgeWriter.flush();
        edgeWriter.setProperty(Trace.timeUnitKey, Units.toTimeUnit(ticsPerSecond));
        idGen.writeTraceInfo(edgeWriter);
        edgeWriter.close();
        br.close();
    }

    public static void toCRAWDAD(EdgeTrace edges,
            OutputStream out, double timeMul) throws IOException {

        final StatefulReader<EdgeEvent, Edge> edgeReader = edges.getReader();
        final BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
        final Map<Edge, Long> activeContacts = new AdjacencyMap.Edges<Long>();

        edgeReader.seek(edges.minTime());
        for (final Edge e : edgeReader.referenceState())
            activeContacts.put(e, edges.minTime());
        while (edgeReader.hasNext())
            for (final EdgeEvent eev : edgeReader.next()) {
                final Edge e = eev.edge();
                if (eev.isUp())
                    activeContacts.put(e, edgeReader.time());
                else {
                    final double beg = activeContacts.get(e) * timeMul;
                    final double end = edgeReader.time() * timeMul;
                    activeContacts.remove(e);
                    bw.write(e.id1 + "\t" + e.id2 + "\t" + beg + "\t" + end + "\n");
                }
            }
        edgeReader.close();
        bw.close();
    }

}
