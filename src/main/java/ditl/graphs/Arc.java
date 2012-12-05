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

    final public Integer from;
    final public Integer to;

    public Arc(Integer nodeFrom, Integer nodeTo) {
        from = nodeFrom;
        to = nodeTo;
    }

    @Override
    public Integer id1() {
        return from;
    }

    @Override
    public Integer id2() {
        return to;
    }

    public final static class Factory implements Item.Factory<Arc> {
        @Override
        public Arc fromBinaryStream(CodedInputStream in) throws IOException {
            return new Arc(in.readSInt(), in.readSInt());
        }
    }

    public Arc reverse() {
        return new Arc(to, from);
    }

    public Edge edge() {
        return new Edge(from, to);
    }

    @Override
    public boolean equals(Object o) {
        final Arc l = (Arc) o;
        return (l.from.equals(from)) && (l.to.equals(to));
    }

    @Override
    public String toString() {
        return from + " " + to;
    }

    public static final class InternalGroupFilter implements Filter<Arc> {
        private final Set<Integer> _group;

        public InternalGroupFilter(Set<Integer> group) {
            _group = group;
        }

        @Override
        public Arc filter(Arc item) {
            if (_group.contains(item.from) && _group.contains(item.to))
                return item;
            return null;
        }
    }

    @Override
    public void write(CodedBuffer out) {
        out.writeSInt(from);
        out.writeSInt(to);
    }

}
