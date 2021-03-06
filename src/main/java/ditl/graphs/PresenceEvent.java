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

public final class PresenceEvent implements Item {

    public enum Type {
        IN, OUT
    }

    public final Integer id;
    public final Type type;

    public PresenceEvent(Integer i, Type presenceEventType) {
        id = i;
        type = presenceEventType;
    }

    public boolean isIn() {
        return type == Type.IN;
    }

    public Presence presence() {
        return new Presence(id);
    }

    public static final class Factory implements Item.Factory<PresenceEvent> {
        @Override
        public PresenceEvent fromBinaryStream(CodedInputStream in) throws IOException {
            return new PresenceEvent(in.readSInt(), Type.values()[in.readByte()]);
        }
    }

    @Override
    public String toString() {
        return id + " " + type;
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

    @Override
    public void write(CodedBuffer out) {
        out.writeSInt(id);
        out.writeByte(type.ordinal());
    }

}
