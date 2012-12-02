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

    Integer msg_id;
    Type _type;
    long bytes_transferred;
    Integer _from;
    Integer _to;

    public TransferEvent(Integer msgId, Integer from, Integer to, Type type) {
        msg_id = msgId;
        _type = type;
        _from = from;
        _to = to;
    }

    public TransferEvent(Integer msgId, Integer from, Integer to, Type type, long bytesTransferred) {
        msg_id = msgId;
        _type = type;
        _from = from;
        _to = to;
        bytes_transferred = bytesTransferred;
    }

    public long bytesTransferred() {
        return bytes_transferred;
    }

    public Type type() {
        return _type;
    }

    public Integer msgId() {
        return msg_id;
    }

    public Arc arc() {
        return new Arc(_from, _to);
    }

    @Override
    public String toString() {
        switch (_type) {
            case START:
                return "START " + msg_id + " " + _from + " " + _to;
            case COMPLETE:
                return "COMPLETE " + msg_id + " " + _from + " " + _to + " " + bytes_transferred;
            default:
                return "ABORT " + msg_id + " " + _from + " " + _to + " " + bytes_transferred;
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
        out.writeByte(_type.ordinal());
        out.writeSInt(msg_id);
        out.writeSInt(_from);
        out.writeSInt(_to);
        if (_type != Type.START)
            out.writeLong(bytes_transferred);
    }
}
