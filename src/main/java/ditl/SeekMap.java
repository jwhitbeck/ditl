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
package ditl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.TreeMap;

public final class SeekMap {

    private final TreeMap<Long, Long> byteOffsets = new TreeMap<Long, Long>();

    private SeekMap() {
    }

    public static SeekMap open(InputStream is) throws IOException {
        SeekMap sm = new SeekMap();
        CodedInputStream in = new CodedInputStream(new BufferedInputStream(is));
        while (!in.isAtEnd()) {
            sm.byteOffsets.put(in.readSLong(), in.readLong());
        }
        in.close();
        return sm;
    }

    public long getOffset(long timestamp) {
        Map.Entry<Long, Long> e = byteOffsets.floorEntry(timestamp);
        if (e == null)
            return Long.MIN_VALUE;
        return e.getValue();
    }

    public static final class Writer {
        private final OutputStream _os;
        private final CodedBuffer _buffer;

        public Writer(OutputStream out) {
            _os = new BufferedOutputStream(out);
            _buffer = new CodedBuffer(20);
        }

        public void append(long timestamp, long byteOffset) throws IOException {
            _buffer.writeSLong(timestamp);
            _buffer.writeLong(byteOffset);
            _buffer.flush(_os);
        }

        public void close() throws IOException {
            _os.close();
        }

    }
}
