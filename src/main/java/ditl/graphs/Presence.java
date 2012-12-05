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

public final class Presence implements Item {

    public final Integer id;

    public Presence(Integer i) {
        id = i;
    }

    public static final class Factory implements Item.Factory<Presence> {
        @Override
        public Presence fromBinaryStream(CodedInputStream in) throws IOException {
            return new Presence(in.readSInt());
        }
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        final Presence p = (Presence) o;
        return p.id.equals(id);
    }

    @Override
    public String toString() {
        return "p " + id;
    }

    public static final class GroupFilter implements Filter<Presence> {
        private final Set<Integer> _group;

        public GroupFilter(Set<Integer> group) {
            _group = group;
        }

        @Override
        public Presence filter(Presence item) {
            if (_group.contains(item.id))
                return item;
            return null;
        }
    }

    @Override
    public void write(CodedBuffer out) {
        out.writeSInt(id);
    }
}
