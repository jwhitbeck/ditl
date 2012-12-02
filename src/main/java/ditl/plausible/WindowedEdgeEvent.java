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
import ditl.graphs.Edge;

public final class WindowedEdgeEvent implements Item {

    public enum Type {
        UP, DOWN, PREVUP, PREVDOWN, NEXTUP, NEXTDOWN
    }

    final Type _type;
    final Edge _edge;
    final long _value;

    public WindowedEdgeEvent(Edge edge, Type type) {
        _type = type;
        _edge = edge;
        _value = 0;
    }

    public WindowedEdgeEvent(Edge edge, Type type, long value) {
        _type = type;
        _edge = edge;
        _value = value;
    }

    public Type type() {
        return _type;
    }

    public Edge edge() {
        return _edge;
    }

    @Override
    public String toString() {
        switch (_type) {
            case UP:
            case DOWN:
                return _edge + " " + _type;
            default:
                return _edge + " " + _type + " " + _value;
        }
    }

    public static final class Factory implements Item.Factory<WindowedEdgeEvent> {
        @Override
        public WindowedEdgeEvent fromBinaryStream(CodedInputStream in) throws IOException {
            Edge e = new Edge(in.readSInt(), in.readSInt());
            Type type = Type.values()[in.readByte()];
            switch (type) {
                case UP:
                case DOWN:
                    return new WindowedEdgeEvent(e, type);
                default:
                    return new WindowedEdgeEvent(e, type, in.readSLong());
            }
        }
    }

    @Override
    public void write(CodedBuffer out) {
        out.writeSInt(_edge.id1());
        out.writeSInt(_edge.id2());
        out.writeByte(_type.ordinal());
        if (_type != Type.UP && _type != Type.DOWN)
            out.writeSLong(_value);
    }
}
