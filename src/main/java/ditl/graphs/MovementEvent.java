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

public final class MovementEvent implements Item {

    public enum Type {
        IN, OUT, NEW_DEST
    }

    public final Integer id;
    public final Point dest;
    final double speed;
    public final Type type;

    public MovementEvent(Integer i) {
        id = i;
        type = Type.OUT;
        speed = 0.0;
        dest = null;
    }

    public MovementEvent(Integer i, Point d) {
        id = i;
        type = Type.IN;
        dest = d;
        speed = 0.0;
    }

    public MovementEvent(Integer i, double sp, Point d) {
        id = i;
        dest = d;
        speed = sp;
        type = Type.NEW_DEST;
    }

    public Movement origMovement() {
        return new Movement(id, dest);
    }

    public static final class Factory implements Item.Factory<MovementEvent> {
        @Override
        public MovementEvent fromBinaryStream(CodedInputStream in) throws IOException {
            int id = in.readSInt();
            Type type = Type.values()[in.readByte()];
            switch (type) {
                case OUT:
                    return new MovementEvent(id);
                case IN:
                    return new MovementEvent(id, new Point(in.readDouble(), in.readDouble()));
                default: // NEW_DEST
                    return new MovementEvent(id, in.readDouble(), new Point(in.readDouble(), in.readDouble()));
            }
        }
    }

    @Override
    public String toString() {
        switch (type) {
            case IN:
                return id + " IN " + dest;
            case OUT:
                return id + " OUT";
            default:
                return id + " " + speed + " " + dest;
        }
    }

    public String ns2String(double time, double mul) {
        return "$ns_ at " + time * mul + " \"$node_(" + id + ") setdest " + dest + " " + speed / mul + "\"\n";
    }

    public static final class GroupFilter implements Filter<MovementEvent> {
        private final Set<Integer> _group;

        public GroupFilter(Set<Integer> group) {
            _group = group;
        }

        @Override
        public MovementEvent filter(MovementEvent item) {
            if (_group.contains(item.id))
                return item;
            return null;
        }
    }

    @Override
    public void write(CodedBuffer out) {
        out.writeSInt(id);
        out.writeByte(type.ordinal());
        switch (type) {
            case IN:
                out.writeDouble(dest.x);
                out.writeDouble(dest.y);
                break;
            case NEW_DEST:
                out.writeDouble(speed);
                out.writeDouble(dest.x);
                out.writeDouble(dest.y);
                break;
        }
    }
}
