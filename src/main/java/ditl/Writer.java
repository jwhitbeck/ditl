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
import java.io.OutputStreamWriter;
import java.util.Collection;

import net.sf.json.JSONObject;

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

    private final WritableStore _store;
    private final Trace<I> _trace;

    public Writer(Trace<I> trace) throws IOException {
        if (!(trace._store instanceof WritableStore))
            throw new IOException();
        _store = (WritableStore) trace._store;
        if (_store.isAlreadyWriting(trace.name()))
            throw new IOException();
        _trace = trace;
        _store.notifyOpen(trace.name(), this);
        sm = new SeekMap.Writer(_store.getOutputStream(trace.indexFile()));
        out = new BufferedOutputStream(_store.getOutputStream(trace.traceFile()));
        min_time = Long.MAX_VALUE;
        max_time = Long.MIN_VALUE;
        addListener(this);
    }

    void setRemainingInfo() {
        _trace.set(Trace.maxUpdateIntervalKey, max_update_interval);
        _trace.setIfUnset(Trace.maxTimeKey, max_time);
        _trace.setIfUnset(Trace.minTimeKey, min_time);
        _trace.setIfUnset(Trace.defaultPriorityKey, Trace.defaultPriority);
    }

    public void close() throws IOException {
        if (!buffer.isEmpty())
            flushBuffer();
        out.close();
        sm.close();
        setRemainingInfo();
        OutputStreamWriter info_os = new OutputStreamWriter(_store.getOutputStream(_trace.infoFile()));
        info_os.write(_trace.config.toString(4));
        info_os.close();
        _store.notifyClose(_trace.name());
    }

    public void setProperty(String key, Object value) {
        _trace.set(key, value);
    }

    public void setPropertiesFromTrace(Trace<?> trace) {
        _trace.setIfUnset(Trace.minTimeKey, trace.minTime());
        _trace.setIfUnset(Trace.maxTimeKey, trace.maxTime());
        _trace.setIfUnset(Trace.timeUnitKey, trace.timeUnit());
        final JSONObject id_map_str = (JSONObject) trace.config.get(Trace.idMapKey);
        if (id_map_str != null)
            _trace.setIfUnset(Trace.idMapKey, id_map_str);
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

    void write(long time, I item) throws IOException {
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
