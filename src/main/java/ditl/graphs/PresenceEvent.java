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

import java.util.Set;

import ditl.Filter;
import ditl.ItemFactory;

public final class PresenceEvent {

    public enum Type {
        IN, OUT
    }

    private final Integer id;
    private final Type _type;

    public PresenceEvent(Integer i, Type type) {
        id = i;
        _type = type;
    }

    public Integer id() {
        return id;
    }

    public boolean isIn() {
        return _type == Type.IN;
    }

    public Presence presence() {
        return new Presence(id);
    }

    public static final class Factory implements ItemFactory<PresenceEvent> {
        @Override
        public PresenceEvent fromString(String s) {
            final String[] elems = s.trim().split(" ");
            try {
                final int id = Integer.parseInt(elems[0]);
                final Type type = Type.valueOf(elems[1]);
                return new PresenceEvent(id, type);
            } catch (final Exception e) {
                System.err.println("Error parsing '" + s + "': " + e.getMessage());
                return null;
            }
        }
    }

    @Override
    public String toString() {
        return id + " " + _type;
    }

    public static final class GroupFilter implements Filter<PresenceEvent> {
        private final Set<Integer> _group;

        public GroupFilter(Set<Integer> group) {
            _group = group;
        }

        @Override
        public PresenceEvent filter(PresenceEvent item) {
            if (_group.contains(item.id))
                return item;
            return null;
        }
    }

}
