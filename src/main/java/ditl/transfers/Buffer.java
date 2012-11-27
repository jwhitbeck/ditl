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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import ditl.ItemFactory;

public class Buffer implements Iterable<Integer> {

    public static String delim = ":";

    Integer _id;
    Set<Integer> msg_ids = new HashSet<Integer>();

    public Buffer(Integer id) {
        _id = id;
    }

    public Integer id() {
        return _id;
    }

    @Override
    public int hashCode() {
        return _id;
    }

    @Override
    public String toString() {
        final StringBuffer buffer = new StringBuffer();
        buffer.append(_id + " ");
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
        return _id.equals(b._id);
    }

    public static final class Factory implements ItemFactory<Buffer> {
        @Override
        public Buffer fromString(String s) {
            final String[] elems = s.trim().split(" ");
            try {
                final Integer id = Integer.parseInt(elems[0]);
                final Buffer b = new Buffer(id);
                if (!elems[1].equals("-"))
                    for (final String m : elems[1].split(delim))
                        b.msg_ids.add(Integer.parseInt(m));
                return b;
            } catch (final Exception e) {
                System.err.println("Error parsing '" + s + "': " + e.getMessage());
                return null;
            }
        }
    }

    @Override
    public Iterator<Integer> iterator() {
        return msg_ids.iterator();
    }
}
