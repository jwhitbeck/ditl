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
package ditl.transfers;

import ditl.ItemFactory;

public class BufferEvent {
    public enum Type {
        IN, ADD, REMOVE, OUT
    }

    Integer _id;
    Integer msg_id;
    Type _type;

    public BufferEvent(Integer id, Type type) {
        _id = id;
        _type = type;
    }

    public BufferEvent(Integer id, Integer msgId, Type type) {
        _id = id;
        msg_id = msgId;
        _type = type;
    }

    public Type type() {
        return _type;
    }

    public Integer id() {
        return _id;
    }

    public Integer msgId() {
        return msg_id;
    }

    public static final class Factory implements ItemFactory<BufferEvent> {
        @Override
        public BufferEvent fromString(String s) {
            final String[] elems = s.trim().split(" ");
            try {
                final Integer id = Integer.parseInt(elems[0]);
                final Type type = Type.valueOf(elems[1]);
                switch (type) {
                    case IN:
                    case OUT:
                        return new BufferEvent(id, type);
                    default:
                        final Integer msgId = Integer.parseInt(elems[2]);
                        return new BufferEvent(id, msgId, type);
                }
            } catch (final Exception e) {
                System.err.println("Error parsing '" + s + "': " + e.getMessage());
                return null;
            }
        }
    }

    @Override
    public String toString() {
        switch (_type) {
            case IN:
            case OUT:
                return _id + " " + _type;
            default:
                return _id + " " + _type + " " + msg_id;
        }
    }
}
