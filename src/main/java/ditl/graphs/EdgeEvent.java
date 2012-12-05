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

import ditl.CodedBuffer;
import ditl.CodedInputStream;
import ditl.Filter;
import ditl.Item;

public final class EdgeEvent implements Item {

    public enum Type {
        UP, DOWN
    };

    final public Integer id1;
    final public Integer id2;
    final public Type type;

    public EdgeEvent(Integer i1, Integer i2, Type edgeEventType) {
        if (i1 < i2) {
            id1 = i1;
            id2 = i2;
        } else {
            id1 = i2;
            id2 = i1;
        }
        type = edgeEventType;
    }

    public EdgeEvent(Edge edge, Type edgeEventType) {
        id1 = edge.id1;
        id2 = edge.id2;
        type = edgeEventType;
    }

    public boolean isUp() {
        return type == Type.UP;
    }

    public Edge edge() {
        return new Edge(id1, id2);
    }

    public static final class Factory implements Item.Factory<EdgeEvent> {
        @Override
        public EdgeEvent fromBinaryStream(CodedInputStream in) throws IOException {
            return new EdgeEvent(in.readSInt(), in.readSInt(), Type.values()[in.readByte()]);
        }
    }

    @Override
    public String toString() {
        return id1 + " " + id2 + " " + type;
    }

    public static final class InternalGroupFilter implements Filter<EdgeEvent> {
        private final Set<Integer> _group;

        public InternalGroupFilter(Set<Integer> group) {
            _group = group;
        }

        @Override
        public EdgeEvent filter(EdgeEvent item) {
            if (_group.contains(item.id1) && _group.contains(item.id2))
                return item;
            return null;
        }
    }

    @Override
    public void write(CodedBuffer out) {
        out.writeSInt(id1);
        out.writeSInt(id2);
        out.writeByte(type.ordinal());
    }

}
