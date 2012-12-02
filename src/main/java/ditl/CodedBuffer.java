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
import java.io.OutputStream;

public class CodedBuffer {

    private final static int DEFAULT_BUFFER_SIZE = 4096;
    private final static int MAX_BUFFER_SIZE = (64 << 20); // 64MB

    private byte[] buffer;
    private int position = 0;

    public CodedBuffer() {
        buffer = new byte[DEFAULT_BUFFER_SIZE];
    }

    public CodedBuffer(int bufferSize) {
        buffer = new byte[bufferSize];
    }

    public int flush(OutputStream os) throws IOException {
        os.write(buffer, 0, position);
        int bytes_written = position;
        position = 0;
        return bytes_written;
    }

    public int bytesInBuffer() {
        return position;
    }

    public boolean isEmpty() {
        return position == 0;
    }

    public void writeByte(final int b) {
        if (position == buffer.length) { // buffer full!
            increaseBufferSize();
        }
        buffer[position++] = (byte) b;
    }

    public void writeInt(int value) {
        while (true) {
            if ((value & ~0x7F) == 0) {
                writeByte(value);
                return;
            } else {
                writeByte((value & 0x7F) | 0x80);
                value >>>= 7;
            }
        }
    }

    public void writeSInt(int value) {
        writeInt(encodeZigZag32(value));
    }

    public void writeLong(long value) {
        while (true) {
            if ((value & ~0x7FL) == 0) {
                writeByte((int) value);
                return;
            } else {
                writeByte(((int) value & 0x7F) | 0x80);
                value >>>= 7;
            }
        }
    }

    public void writeSLong(final long value) {
        writeLong(encodeZigZag64(value));
    }

    public void writeDouble(final double value) {
        writeRawLittleEndian64(Double.doubleToRawLongBits(value));
    }

    private int encodeZigZag32(final int n) {
        return (n << 1) ^ (n >> 31);
    }

    private long encodeZigZag64(final long n) {
        return (n << 1) ^ (n >> 63);
    }

    private void writeRawLittleEndian64(final long value) {
        writeByte((int) (value) & 0xFF);
        writeByte((int) (value >> 8) & 0xFF);
        writeByte((int) (value >> 16) & 0xFF);
        writeByte((int) (value >> 24) & 0xFF);
        writeByte((int) (value >> 32) & 0xFF);
        writeByte((int) (value >> 40) & 0xFF);
        writeByte((int) (value >> 48) & 0xFF);
        writeByte((int) (value >> 56) & 0xFF);
    }

    private void increaseBufferSize() {
        int buffer_size = buffer.length << 1;
        if (buffer_size <= MAX_BUFFER_SIZE) {
            byte[] old_buffer = buffer;
            buffer = new byte[buffer_size];
            System.arraycopy(old_buffer, 0, buffer, 0, old_buffer.length);
        } else {
            throw new IllegalStateException("Exceeded max buffer size!");
        }
    }

}
