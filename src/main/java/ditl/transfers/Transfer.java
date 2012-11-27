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

public class Transfer {

    Integer msg_id;
    Integer _from;
    Integer _to;

    public Transfer(Integer msgId, Integer from, Integer to) {
        msg_id = msgId;
        _from = from;
        _to = to;
    }

    public Transfer(TransferEvent event) {
        if (event.type() == TransferEvent.Type.START) {
            msg_id = event.msg_id;
            _to = event._to;
            _from = event._from;
        }
    }

    public Arc arc() {
        return new Arc(_from, _to);
    }

    public Integer from() {
        return _from;
    }

    public Integer to() {
        return _to;
    }

    public Integer msgId() {
        return msg_id;
    }

    @Override
    public String toString() {
        return msg_id + " " + _from + " " + _to;
    }

    public static final class Factory implements ItemFactory<Transfer> {
        @Override
        public Transfer fromString(String s) {
            final String[] elems = s.trim().split(" ");
            try {
                final Integer msgId = Integer.parseInt(elems[0]);
                final Integer from = Integer.parseInt(elems[1]);
                final Integer to = Integer.parseInt(elems[2]);
                return new Transfer(msgId, from, to);
            } catch (final Exception e) {
                System.err.println("Error parsing '" + s + "': " + e.getMessage());
                return null;
            }
        }
    }
}
