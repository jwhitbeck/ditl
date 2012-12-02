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

import java.io.IOException;

import ditl.CodedBuffer;
import ditl.CodedInputStream;
import ditl.Item;

public class Message implements Item {

    private final Integer msg_id;

    public Message(Integer msgId) {
        msg_id = msgId;
    }

    public Integer msgId() {
        return msg_id;
    }

    public static final class Factory implements Item.Factory<Message> {
        @Override
        public Message fromBinaryStream(CodedInputStream in) throws IOException {
            return new Message(in.readSInt());
        }
    }

    @Override
    public int hashCode() {
        return msg_id;
    }

    @Override
    public boolean equals(Object o) {
        final Message m = (Message) o;
        return m.msg_id.equals(msg_id);
    }

    @Override
    public String toString() {
        return "m " + msg_id;
    }

    @Override
    public void write(CodedBuffer out) {
        out.writeSInt(msg_id);
    }
}
