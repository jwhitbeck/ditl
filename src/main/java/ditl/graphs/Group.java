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
import ditl.GroupSpecification;
import ditl.Item;

public class Group implements Item {

    Integer _gid;
    Set<Integer> _members;

    public Group(Integer gid) {
        _gid = gid;
        _members = new HashSet<Integer>();
    }

    public Group(Integer gid, Set<Integer> members) {
        _gid = gid;
        _members = members;
    }

    public Integer gid() {
        return _gid;
    }

    public int size() {
        return _members.size();
    }

    public final static class Factory implements Item.Factory<Group> {
        @Override
        public Group fromBinaryStream(CodedInputStream in) throws IOException {
            int gid = in.readSInt();
            int n = in.readInt();
            Set<Integer> members = new HashSet<Integer>();
            for (int i = 0; i < n; ++i)
                members.add(in.readSInt());
            return new Group(gid, members);
        }
    }

    public void handleEvent(GroupEvent event) {
        switch (event._type) {
            case JOIN:
                for (final Integer m : event._members)
                    _members.add(m);
                break;
            case LEAVE:
                for (final Integer m : event._members)
                    _members.remove(m);
                break;
        }
    }

    @Override
    public String toString() {
        return _gid + " " + GroupSpecification.toString(_members);
    }

    @Override
    public int hashCode() {
        return _gid;
    }

    public Set<Integer> members() {
        return Collections.unmodifiableSet(_members);
    }

    public final static class GroupFilter implements Filter<Group> {
        private final Set<Integer> _group;

        public GroupFilter(Set<Integer> group) {
            _group = group;
        }

        @Override
        public Group filter(Group item) {
            final Group f_group = new Group(item._gid);
            for (final Integer i : item._members)
                if (_group.contains(i))
                    f_group._members.add(i);
            if (f_group._members.isEmpty())
                return null;
            return f_group;
        }

    }

    @Override
    public void write(CodedBuffer out) {
        out.writeSInt(_gid);
        out.writeInt(_members.size());
        for (Integer m : _members) {
            out.writeSInt(m);
        }
    }
}
