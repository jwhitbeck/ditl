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

import java.io.IOException;
import java.io.InputStream;

public class CodedInputStream {

    private final static int EMPTY = -100;
    private final static int END = -1;

    private long position = 0;
    private final InputStream _is;

    private long mark = 0;
    private int stored_byte = EMPTY;

    public CodedInputStream(InputStream is) {
        _is = is;
    }

    public byte readByte() throws IOException {
        position++;
        if (stored_byte != EMPTY) {
            byte b = (byte) stored_byte;
            stored_byte = EMPTY;
            return b;
        }
        return (byte) _is.read();
    }

    public boolean isAtEnd() throws IOException {
        if (stored_byte == EMPTY) {
            stored_byte = _is.read();
        }
        return stored_byte == END;
    }

    public int readSInt() throws IOException {
        return decodeZigZag32(readInt());
    }

    public int readInt() throws IOException {
        int shift = 0;
        int result = 0;
        while (shift < 32) {
            final byte b = readByte();
            result |= (b & 0x7F) << shift;
            if ((b & 0x80) == 0) {
                return result;
            }
            shift += 7;
        }
        throw new IllegalStateException("Malformed varint 32");
    }

    public long readLong() throws IOException {
        int shift = 0;
        long result = 0;
        while (shift < 64) {
            final byte b = readByte();
            result |= (long) (b & 0x7F) << shift;
            if ((b & 0x80) == 0) {
                return result;
            }
            shift += 7;
        }
        throw new IllegalStateException("Malformed varint 64");
    }

    public long readSLong() throws IOException {
        return decodeZigZag64(readLong());
    }

    public double readDouble() throws IOException {
        return Double.longBitsToDouble(readRawLittleEndian64());
    }

    private long readRawLittleEndian64() throws IOException {
        final byte b1 = readByte();
        final byte b2 = readByte();
        final byte b3 = readByte();
        final byte b4 = readByte();
        final byte b5 = readByte();
        final byte b6 = readByte();
        final byte b7 = readByte();
        final byte b8 = readByte();
        return (((long) b1 & 0xff)) |
                (((long) b2 & 0xff) << 8) |
                (((long) b3 & 0xff) << 16) |
                (((long) b4 & 0xff) << 24) |
                (((long) b5 & 0xff) << 32) |
                (((long) b6 & 0xff) << 40) |
                (((long) b7 & 0xff) << 48) |
                (((long) b8 & 0xff) << 56);
    }

    public void fastForwardTo(long absolutePosition) throws IOException {
        reallySkip(absolutePosition - position);
        position = absolutePosition;
    }

    public boolean canFastForwardTo(long absolutePosition) {
        return absolutePosition >= position;
    }

    private void reallySkip(long bytes) throws IOException {
        while (bytes > 0) {
            bytes -= _is.skip(bytes);
        }
    }

    public void skip(long bytes) throws IOException {
        reallySkip(bytes);
        position += bytes;
    }

    private static int decodeZigZag32(final int n) {
        return (n >>> 1) ^ -(n & 1);
    }

    private static long decodeZigZag64(final long n) {
        return (n >>> 1) ^ -(n & 1);
    }

    public void mark() {
        mark = position;
    }

    public long bytesReadSinceMark() {
        return position - mark;
    }

    public void close() throws IOException {
        _is.close();
    }

}
