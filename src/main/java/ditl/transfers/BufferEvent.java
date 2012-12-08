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

public class BufferEvent implements Item {
    public enum Type {
        IN, ADD, REMOVE, OUT
    }

    public final Integer id;
    public final Integer msgId;
    public final Type type;

    public BufferEvent(Integer i, Type bufferEventType) {
        id = i;
        type = bufferEventType;
        msgId = null;
    }

    public BufferEvent(Integer i, Integer messageId, Type buffereEventType) {
        id = i;
        msgId = messageId;
        type = buffereEventType;
    }

    public static final class Factory implements Item.Factory<BufferEvent> {
        @Override
        public BufferEvent fromBinaryStream(CodedInputStream in) throws IOException {
            int id = in.readSInt();
            Type type = Type.values()[in.readByte()];
            switch (type) {
                case IN:
                case OUT:
                    return new BufferEvent(id, type);
                default:
                    return new BufferEvent(id, in.readSInt(), type);
            }
        }
    }

    @Override
    public String toString() {
        switch (type) {
            case IN:
            case OUT:
                return id + " " + type;
            default:
                return id + " " + type + " " + msgId;
        }
    }

    @Override
    public void write(CodedBuffer out) {
        out.writeSInt(id);
        out.writeByte(type.ordinal());
        if (type != Type.IN && type != Type.OUT)
            out.writeSInt(msgId);
    }
}
