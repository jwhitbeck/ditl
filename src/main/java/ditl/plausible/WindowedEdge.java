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

import ditl.CodedBuffer;
import ditl.CodedInputStream;
import ditl.Item;
import ditl.graphs.Couple;
import ditl.graphs.Edge;

public final class WindowedEdge implements Couple, Item {

    long prev_up = Long.MIN_VALUE;
    long prev_down = Long.MIN_VALUE;
    long next_up = Long.MAX_VALUE;
    long next_down = Long.MAX_VALUE;

    public final Edge edge;

    public WindowedEdge(Edge e) {
        edge = e;
    }

    @Override
    public Integer id1() {
        return edge.id1;
    }

    @Override
    public Integer id2() {
        return edge.id2;
    }

    public long minUpTime(long t) {
        return Math.min(next_down - t, t - prev_up);
    }

    public long minDownTime(long t) {
        return Math.min(next_up - t, t - prev_down);
    }

    @Override
    public boolean equals(Object o) {
        final WindowedEdge we = (WindowedEdge) o;
        return we.edge.equals(edge);
    }

    @Override
    public String toString() {
        return edge + " " + prev_up + " " + prev_down + " " + next_up + " " + next_down;
    }

    public static final class Factory implements Item.Factory<WindowedEdge> {
        @Override
        public WindowedEdge fromBinaryStream(CodedInputStream in) throws IOException {
            WindowedEdge we = new WindowedEdge(new Edge(in.readSInt(), in.readSInt()));
            we.prev_up = in.readSLong();
            we.prev_down = in.readSLong();
            we.next_up = in.readSLong();
            we.next_down = in.readSLong();
            return we;
        }
    }

    public void handleEvent(WindowedEdgeEvent wee) {
        switch (wee.type) {
            case PREVUP:
                prev_up = wee._value;
                break;
            case PREVDOWN:
                prev_down = wee._value;
                break;
            case NEXTUP:
                next_up = wee._value;
                break;
            case NEXTDOWN:
                next_down = wee._value;
                break;
        }
    }

    @Override
    public void write(CodedBuffer out) {
        out.writeSInt(edge.id1);
        out.writeSInt(edge.id2);
        out.writeSLong(prev_up);
        out.writeSLong(prev_down);
        out.writeSLong(next_up);
        out.writeSLong(next_down);
    }
}
