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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import ditl.CodedBuffer;
import ditl.CodedInputStream;
import ditl.Filter;
import ditl.Groups;
import ditl.Item;

public class GroupEvent implements Item {

    public enum Type {
        NEW,
        JOIN,
        LEAVE,
        DELETE;
    }

    public final Type type;
    public final Integer gid;
    final Set<Integer> _members;

    public GroupEvent(Integer groupId, Type groupEventType) { // NEW and DELETE
                                                              // events
        gid = groupId;
        type = groupEventType;
        _members = null;
    }

    public GroupEvent(Integer groupId, Type groupEventType, Integer[] members) {
        gid = groupId;
        type = groupEventType;
        _members = new HashSet<Integer>();
        for (final Integer i : members)
            _members.add(i);
    }

    public GroupEvent(Integer groupId, Type groupEventType, Set<Integer> members) {
        gid = groupId;
        type = groupEventType;
        _members = members;
    }

    public Set<Integer> members() {
        return Collections.unmodifiableSet(_members);
    }

    public final static class Factory implements Item.Factory<GroupEvent> {
        @Override
        public GroupEvent fromBinaryStream(CodedInputStream in) throws IOException {
            int gid = in.readSInt();
            Type type = Type.values()[in.readByte()];
            if (type == Type.JOIN || type == Type.LEAVE) {
                return new GroupEvent(gid, type, in.readSIntSet());
            }
            return new GroupEvent(gid, type);
        }
    }

    @Override
    public String toString() {
        switch (type) {
            case NEW:
            case DELETE:
                return type + " " + gid;
            default:
                return type + " " + gid + " " + Groups.toJSON(_members);
        }
    }

    public final static class GroupFilter implements Filter<GroupEvent> {
        private final Set<Integer> _group;

        public GroupFilter(Set<Integer> group) {
            _group = group;
        }

        @Override
        public GroupEvent filter(GroupEvent item) {
            final Set<Integer> f_members = new HashSet<Integer>();
            if (item.type == Type.JOIN || item.type == Type.LEAVE) {
                for (final Integer i : item._members)
                    if (_group.contains(i))
                        f_members.add(i);
                if (f_members.isEmpty())
                    return null;
                return new GroupEvent(item.gid, item.type, f_members);
            }
            return item;
        }
    }

    @Override
    public void write(CodedBuffer out) {
        out.writeSInt(gid);
        out.writeByte(type.ordinal());
        if (type == Type.JOIN || type == Type.LEAVE) {
            out.writeSIntSet(_members);
        }
    }
}
