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

    final Integer _to;
    final Integer _from;
    final Type _type;

    public ArcEvent(Integer from, Integer to, Type type) {
        _from = from;
        _to = to;
        _type = type;
    }

    public ArcEvent(Arc a, Type type) {
        _from = a._from;
        _to = a._to;
        _type = type;
    }

    public Integer from() {
        return _from;
    }

    public Integer to() {
        return _to;
    }

    public boolean isUp() {
        return _type == Type.UP;
    }

    public Arc arc() {
        return new Arc(_from, _to);
    }

    public static final class Factory implements Item.Factory<ArcEvent> {
        @Override
        public ArcEvent fromBinaryStream(CodedInputStream in) throws IOException {
            return new ArcEvent(in.readSInt(), in.readSInt(), Type.values()[in.readByte()]);
        }
    }

    @Override
    public String toString() {
        return _from + " " + _to + " " + _type;
    }

    public static final class InternalGroupFilter implements Filter<ArcEvent> {
        private final Set<Integer> _group;

        public InternalGroupFilter(Set<Integer> group) {
            _group = group;
        }

        @Override
        public ArcEvent filter(ArcEvent item) {
            if (_group.contains(item._from) && _group.contains(item._to))
                return item;
            return null;
        }
    }

    @Override
    public void write(CodedBuffer out) {
        out.writeSInt(_from);
        out.writeSInt(_to);
        out.writeByte(_type.ordinal());
    }

}
