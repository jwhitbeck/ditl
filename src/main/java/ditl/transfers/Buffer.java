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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import ditl.CodedBuffer;
import ditl.CodedInputStream;
import ditl.Item;

public class Buffer implements Iterable<Integer>, Item {

    public static String delim = ":";

    public final Integer id;
    Set<Integer> msg_ids = new HashSet<Integer>();

    public Buffer(Integer i) {
        id = i;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        final StringBuffer buffer = new StringBuffer();
        buffer.append(id + " ");
        if (!msg_ids.isEmpty())
            for (final Iterator<Integer> i = msg_ids.iterator(); i.hasNext();) {
                final Integer msgId = i.next();
                buffer.append(msgId);
                if (i.hasNext())
                    buffer.append(delim);
            }
        else
            buffer.append("-");
        return buffer.toString();
    }

    @Override
    public boolean equals(Object o) {
        final Buffer b = (Buffer) o;
        return id.equals(b.id);
    }

    public static final class Factory implements Item.Factory<Buffer> {
        @Override
        public Buffer fromBinaryStream(CodedInputStream in) throws IOException {
            Buffer b = new Buffer(in.readSInt());
            int n = in.readInt();
            for (int i = 0; i < n; ++i)
                b.msg_ids.add(in.readSInt());
            return b;
        }
    }

    @Override
    public Iterator<Integer> iterator() {
        return msg_ids.iterator();
    }

    @Override
    public void write(CodedBuffer out) {
        out.writeSInt(id);
        out.writeInt(msg_ids.size());
        for (Integer msgId : msg_ids) {
            out.writeSInt(msgId);
        }
    }
}
