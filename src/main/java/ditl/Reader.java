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
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Reader<I extends Item> implements Generator {

    long cur_time;
    long prev_time = Long.MIN_VALUE;
    long next_time = Long.MAX_VALUE;

    final private Item.Factory<I> _factory;
    private List<I> buffer;
    Bus<I> _bus = new Bus<I>();
    final private int _priority;
    final long _offset;
    final private String _name;

    byte next_flag;
    private int next_block_bytes;

    private CodedInputStream cis;
    final SeekMap seek_map;

    private final Store _store;

    Reader(Store store, String name, Item.Factory<I> factory, int priority, long offset) throws IOException {
        _offset = offset;
        _factory = factory;
        _priority = priority;
        _store = store;
        _name = name;
        seek_map = SeekMap.open(_store.getInputStream(_store.indexFile(_name)));
        init();
    }

    @Override
    public void seek(long time) throws IOException {
        fastSeek(time + _offset);
        while (hasNext() && next_time < time + _offset) {
            skipBlock();
            readHeader();
        }
        cur_time = time + _offset;
    }

    @Override
    public void incr(long incr_time) throws IOException {
        cur_time += incr_time;
        while (cur_time > next_time) {
            step();
            _bus.queue(prev_time - _offset, buffer);
        }
    }

    public long time() {
        return cur_time - _offset;
    }

    public long nextTime() {
        return next_time - _offset;
    }

    public long previousTime() {
        return prev_time - _offset;
    }

    public void close() throws IOException {
        cis.close();
        _store.notifyClose(this);
    }

    public boolean hasNext() {
        return next_time != Long.MAX_VALUE;
    }

    public List<I> next() throws IOException {
        step();
        cur_time = prev_time;
        return buffer;
    }

    void step() throws IOException {
        buffer = readItemBlock(_factory);
        prev_time = next_time;
        readHeader();
    }

    void skipBlock() throws IOException {
        cis.skip(next_block_bytes);
    }

    public List<I> previous() {
        return buffer;
    }

    public Bus<I> bus() {
        return _bus;
    }

    public void setBus(Bus<I> bus) {
        _bus = bus;
    }

    private void init() throws IOException {
        _store.notifyOpen(this);
        cis = new CodedInputStream(new BufferedInputStream(_store.getInputStream(_store.traceFile(_name))));
        prev_time = Long.MIN_VALUE;
        buffer = Collections.emptyList();
        cur_time = prev_time;
        readHeader();
    }

    void readHeader() throws IOException {
        if (!cis.isAtEnd()) {
            next_flag = cis.readByte();
            next_block_bytes = cis.readInt();
            next_time = cis.readSLong();
        } else {
            next_time = Long.MAX_VALUE;
        }
    }

    <E extends Item> List<E> readItemBlock(Item.Factory<E> factory) throws IOException {
        List<E> items = new LinkedList<E>();
        cis.mark();
        while (cis.bytesReadSinceMark() < next_block_bytes) {
            items.add(factory.fromBinaryStream(cis));
        }
        return items;
    }

    private void reset() throws IOException {
        close();
        init();
    }

    void fastSeek(long time) throws IOException {
        long absolutePosition = seek_map.getOffset(time);
        if (cis.canFastForwardTo(absolutePosition)) {
            cis.fastForwardTo(absolutePosition);
            readHeader();
        } else {
            reset();
            if (cis.canFastForwardTo(absolutePosition)) {
                cis.fastForwardTo(absolutePosition);
                readHeader();
            }
        }
    }

    @Override
    public Bus<?>[] busses() {
        return new Bus<?>[] { _bus };
    }

    @Override
    public int priority() {
        return _priority;
    }
}
