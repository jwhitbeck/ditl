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
import ditl.graphs.Arc;

public class TransferEvent {

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

    public static final class Factory implements ItemFactory<TransferEvent> {
        @Override
        public TransferEvent fromString(String s) {
            final String[] elems = s.trim().split(" ");
            try {
                final Type type = Type.valueOf(elems[0]);
                final Integer msgId = Integer.parseInt(elems[1]);
                final Integer from = Integer.parseInt(elems[2]);
                final Integer to = Integer.parseInt(elems[3]);
                switch (type) {
                    case START:
                        return new TransferEvent(msgId, from, to, type);
                    default:
                        final long bytesTransferred = Long.parseLong(elems[4]);
                        return new TransferEvent(msgId, from, to, type, bytesTransferred);
                }
            } catch (final Exception e) {
                System.err.println("Error parsing '" + s + "': " + e.getMessage());
                return null;
            }
        }
    }
}
