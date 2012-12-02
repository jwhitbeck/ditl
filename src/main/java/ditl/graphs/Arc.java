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

public final class Arc implements Item, Couple {

    final Integer _from;
    final Integer _to;

    public Arc(Integer from, Integer to) {
        _from = from;
        _to = to;
    }

    @Override
    public Integer id1() {
        return _from;
    }

    @Override
    public Integer id2() {
        return _to;
    }

    public Integer from() {
        return _from;
    }

    public Integer to() {
        return _to;
    }

    public final static class Factory implements Item.Factory<Arc> {
        @Override
        public Arc fromBinaryStream(CodedInputStream in) throws IOException {
            return new Arc(in.readSInt(), in.readSInt());
        }
    }

    public Arc reverse() {
        return new Arc(_to, _from);
    }

    public Edge edge() {
        return new Edge(_from, _to);
    }

    @Override
    public boolean equals(Object o) {
        final Arc l = (Arc) o;
        return (l._from.equals(_from)) && (l._to.equals(_to));
    }

    @Override
    public String toString() {
        return _from + " " + _to;
    }

    public static final class InternalGroupFilter implements Filter<Arc> {
        private final Set<Integer> _group;

        public InternalGroupFilter(Set<Integer> group) {
            _group = group;
        }

        @Override
        public Arc filter(Arc item) {
            if (_group.contains(item._from) && _group.contains(item._to))
                return item;
            return null;
        }
    }

    @Override
    public void write(CodedBuffer out) {
        out.writeSInt(_from);
        out.writeSInt(_to);
    }

}
