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

public class MessageEvent {

    public enum Type {
        NEW, EXPIRE
    }

    Integer msg_id;
    Type _type;

    public MessageEvent(Integer msgId, Type type) {
        msg_id = msgId;
        _type = type;
    }

    public Integer msgId() {
        return msg_id;
    }

    public boolean isNew() {
        return _type == Type.NEW;
    }

    public Message message() {
        return new Message(msg_id);
    }

    public static final class Factory implements ItemFactory<MessageEvent> {
        @Override
        public MessageEvent fromString(String s) {
            final String[] elems = s.trim().split(" ");
            try {
                final Integer msgId = Integer.parseInt(elems[0]);
                final Type type = Type.valueOf(elems[1]);
                return new MessageEvent(msgId, type);
            } catch (final Exception e) {
                System.err.println("Error parsing '" + s + "': " + e.getMessage());
                return null;
            }
        }
    }

    @Override
    public String toString() {
        return msg_id + " " + _type;
    }
}
