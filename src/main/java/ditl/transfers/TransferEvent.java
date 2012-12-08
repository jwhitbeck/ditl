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
import ditl.graphs.Arc;

public class TransferEvent implements Item {

    public enum Type {
        START, COMPLETE, ABORT
    }

    public final Integer msgId;
    public final Type type;
    public final long bytesTransferred;
    public final Integer from;
    public final Integer to;

    public TransferEvent(Integer messageId, Integer f, Integer t, Type transferEventType) {
        msgId = messageId;
        type = transferEventType;
        from = f;
        to = t;
        bytesTransferred = 0;
    }

    public TransferEvent(Integer messageId, Integer f, Integer t, Type transferEventType, long totalBytesTransferred) {
        msgId = messageId;
        type = transferEventType;
        from = f;
        to = t;
        bytesTransferred = totalBytesTransferred;
    }

    public long bytesTransferred() {
        return bytesTransferred;
    }

    public Type type() {
        return type;
    }

    public Integer msgId() {
        return msgId;
    }

    public Arc arc() {
        return new Arc(from, to);
    }

    @Override
    public String toString() {
        switch (type) {
            case START:
                return "START " + msgId + " " + from + " " + to;
            case COMPLETE:
                return "COMPLETE " + msgId + " " + from + " " + to + " " + bytesTransferred;
            default:
                return "ABORT " + msgId + " " + from + " " + to + " " + bytesTransferred;
        }
    }

    public static final class Factory implements Item.Factory<TransferEvent> {
        @Override
        public TransferEvent fromBinaryStream(CodedInputStream in) throws IOException {
            Type type = Type.values()[in.readByte()];
            int msgId = in.readSInt();
            int from = in.readSInt();
            int to = in.readSInt();
            if (type == Type.START) {
                return new TransferEvent(msgId, from, to, type);
            } else {
                return new TransferEvent(msgId, from, to, type, in.readLong());
            }
        }
    }

    @Override
    public void write(CodedBuffer out) {
        out.writeByte(type.ordinal());
        out.writeSInt(msgId);
        out.writeSInt(from);
        out.writeSInt(to);
        if (type != Type.START)
            out.writeLong(bytesTransferred);
    }
}
