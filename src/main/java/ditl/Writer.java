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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Collection;

public class Writer<I extends Item> extends Bus<I> implements Listener<I> {

    // trigger an index action every 10,000 events
    private final static int N_EVENT_TRIGGER = 10000;

    private final static int HEADER_BUFFER_SIZE = 32;

    private final BufferedOutputStream out;
    private final SeekMap.Writer sm;
    final CodedBuffer buffer = new CodedBuffer();
    private final CodedBuffer header_buffer = new CodedBuffer(HEADER_BUFFER_SIZE);
    long max_time;
    long min_time;
    private int n_events = 0;
    private int total_bytes_written = 0;
    private long max_update_interval = 1;

    private final PersistentMap _info;
    private final WritableStore _store;
    private final String _name;

    public Writer(Store store, String name, PersistentMap info) throws IOException {
        if (!(store instanceof WritableStore))
            throw new IOException();
        _store = (WritableStore) store;
        if (_store.isAlreadyWriting(name))
            throw new IOException();
        _info = info;
        _name = name;
        _store.notifyOpen(_name, this);
        sm = new SeekMap.Writer(_store.getOutputStream(_store.indexFile(_name)));
        out = new BufferedOutputStream(_store.getOutputStream(_store.traceFile(_name)));
        min_time = Long.MAX_VALUE;
        max_time = Long.MIN_VALUE;
        addListener(this);
    }

    void setRemainingInfo() {
        _info.put(Trace.maxUpdateIntervalKey, max_update_interval);
        _info.setIfUnset(Trace.maxTimeKey, max_time);
        _info.setIfUnset(Trace.minTimeKey, min_time);
        _info.setIfUnset(Trace.defaultPriorityKey, Trace.defaultPriority);
    }

    public void close() throws IOException {
        if (!buffer.isEmpty())
            flushBuffer();
        out.close();
        sm.close();
        setRemainingInfo();
        _info.save(_store.getOutputStream(_store.infoFile(_name)));
        _store.notifyClose(_name);
    }

    public void setProperty(String key, Object value) {
        _info.put(key, value);
    }

    public void setPropertiesFromTrace(Trace<?> trace) {
        _info.setIfUnset(Trace.minTimeKey, trace.minTime());
        _info.setIfUnset(Trace.maxTimeKey, trace.maxTime());
        _info.setIfUnset(Trace.timeUnitKey, trace.timeUnit());
        final String id_map_str = trace.getValue(Trace.idMapKey);
        if (id_map_str != null)
            _info.setIfUnset(Trace.idMapKey, id_map_str);
    }

    public void append(long time, I item) throws IOException {
        updateTime(time);
        write(time, item);
        n_events += 1;
    }

    @Override
    public void handle(long time, Collection<I> items) throws IOException {
        updateTime(time);
        for (final I item : items)
            write(time, item);
        n_events += items.size();
    }

    void write(long time, I item) {
        item.write(buffer);
    }

    private void updateTime(long time) throws IOException {
        if (time < max_time) {
            throw new IOException("States at time " + time + " are out of order");
        }
        if (time < min_time)
            min_time = time;
        if (time > max_time) {
            if (max_time != Long.MIN_VALUE) {
                flushBuffer();
                if (time - max_time > max_update_interval)
                    max_update_interval = time - max_time;
                if (n_events > N_EVENT_TRIGGER)
                    markPosition(time);
            }
            max_time = time;
        }

    }

    void markPosition(long time) throws IOException {
        sm.append(time, total_bytes_written);
        n_events = 0;
    }

    private void flushBuffer() throws IOException {
        writeItemBlockHeader((byte) 0, max_time);
        writeItemBlock();
    }

    void writeItemBlock() throws IOException {
        total_bytes_written += buffer.flush(out);
    }

    void writeItemBlockHeader(byte flag, long time) throws IOException {
        header_buffer.writeByte(flag);
        header_buffer.writeInt(buffer.bytesInBuffer());
        header_buffer.writeSLong(time);
        total_bytes_written += header_buffer.flush(out);
    }

    @Override
    public void reset() {
        throw new UnsupportedOperationException();
    }
}
