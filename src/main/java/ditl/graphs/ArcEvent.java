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

public final class ArcEvent implements Item {

    public enum Type {
        UP, DOWN
    };

    final public Integer to;
    final public Integer from;
    final public Type type;

    public ArcEvent(Integer nodeFrom, Integer nodeTo, Type arcEventType) {
        from = nodeFrom;
        to = nodeTo;
        type = arcEventType;
    }

    public ArcEvent(Arc a, Type arcEventType) {
        from = a.from;
        to = a.to;
        type = arcEventType;
    }

    public boolean isUp() {
        return type == Type.UP;
    }

    public Arc arc() {
        return new Arc(from, to);
    }

    public static final class Factory implements Item.Factory<ArcEvent> {
        @Override
        public ArcEvent fromBinaryStream(CodedInputStream in) throws IOException {
            return new ArcEvent(in.readSInt(), in.readSInt(), Type.values()[in.readByte()]);
        }
    }

    @Override
    public String toString() {
        return from + " " + to + " " + type;
    }

    public static final class InternalGroupFilter implements Filter<ArcEvent> {
        private final Set<Integer> _group;

        public InternalGroupFilter(Set<Integer> group) {
            _group = group;
        }

        @Override
        public ArcEvent filter(ArcEvent item) {
            if (_group.contains(item.from) && _group.contains(item.to))
                return item;
            return null;
        }
    }

    @Override
    public void write(CodedBuffer out) {
        out.writeSInt(from);
        out.writeSInt(to);
        out.writeByte(type.ordinal());
    }

}
