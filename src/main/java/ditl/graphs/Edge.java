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

public final class Edge implements Couple, Item {

    final public Integer id1;
    final public Integer id2;

    public Edge(Integer i1, Integer i2) {
        if (i1 < i2) {
            id1 = i1;
            id2 = i2;
        } else {
            id1 = i2;
            id2 = i1;
        }
    }

    @Override
    public Integer id1() {
        return id1;
    }

    @Override
    public Integer id2() {
        return id2;
    }

    public static final class Factory implements Item.Factory<Edge> {
        @Override
        public Edge fromBinaryStream(CodedInputStream in) throws IOException {
            return new Edge(in.readSInt(), in.readSInt());
        }
    }

    public boolean hasVertex(Integer id) {
        return (id.equals(id1) || id.equals(id2));
    }

    @Override
    public boolean equals(Object o) {
        final Edge ct = (Edge) o;
        return (ct.id1.equals(id1)) && (ct.id2.equals(id2));
    }

    @Override
    public String toString() {
        return id1 + " " + id2;
    }

    public static final class InternalGroupFilter implements Filter<Edge> {
        private final Set<Integer> _group;

        public InternalGroupFilter(Set<Integer> group) {
            _group = group;
        }

        @Override
        public Edge filter(Edge item) {
            if (_group.contains(item.id1) && _group.contains(item.id2))
                return item;
            return null;
        }
    }

    @Override
    public void write(CodedBuffer out) {
        out.writeSInt(id1);
        out.writeSInt(id2);
    }

}
