/*******************************************************************************
 * This file is part of DITL.                                                  *
 *                                                                             *
 * Copyright (C) 2011 John Whitbeck <john@whitbeck.fr>                         *
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

import java.io.*;
import java.util.*;

public class Writer<I> extends Bus<I> implements Listener<I> {
	
	BufferedWriter writer;
	long max_time;
	long min_time;
	long max_interval;
	long min_interval;
	long last_time;
	PersistentMap _info;
	OutputStream info_out;
	WritableStore _store;
	
	Writer(WritableStore store, OutputStream out, OutputStream infoOut, PersistentMap info) throws IOException {
		_store = store;
		_info = info;
		info_out = infoOut;
		setProperty(Trace.defaultPriorityKey, Runner.defaultPriority);
		writer = new BufferedWriter(new OutputStreamWriter(out));
		last_time = -Trace.INFINITY;
		addListener(this);
		min_time = Trace.INFINITY;
		max_time = -Trace.INFINITY;
		min_interval = Trace.INFINITY;
		max_interval = -Trace.INFINITY;
	}
	
	public void close() throws IOException {
		writer.close();
		setIfUnset(Trace.maxTimeKey, max_time);
		setIfUnset(Trace.minTimeKey, min_time);
		if ( max_interval < 0 ){ // single event trace
			max_interval = Long.parseLong(_info.get(Trace.maxTimeKey)) - Long.parseLong(_info.get(Trace.minTimeKey));
			min_interval = max_interval;
		}
		setIfUnset(Trace.maxUpdateIntervalKey, max_interval);
		setIfUnset(Trace.minUpdateIntervalKey, min_interval);
		_info.save(info_out);
		_store.notifyClose(this);
	}
	
	public void setProperty(String key, Object value){
		_info.put(key, String.valueOf(value));
	}

	public void append(long time, I item) throws IOException {
		updateTime(time);
		write(time,item);
	}
	
	private void setIfUnset(String key, Object value){
		if ( ! _info.containsKey(key) )
			_info.put(key, String.valueOf(value));
	}

	@Override
	public void handle(long time, Collection<I> items) throws IOException {
		updateTime(time);
		for ( I item : items )
			write(time,item);
	}
	
	void write(long time, I item) throws IOException {
		writer.write(item+"\n");
	}
	
	void updateTime(long time) throws IOException {
		if ( time < max_time ){
			System.err.println ( "States at time "+time+" are out of order");
			return;
		}
		if ( time > max_time ){
			writer.write(time+"\n");
			max_time = time;
		}
		if ( time < min_time ){
			min_time = time;
		}
		if ( last_time == -Trace.INFINITY ){ // first batch of items
			last_time = time;
		} else if ( time > last_time ){ // we have changed times
			long interval = time - last_time;
			if ( interval > max_interval ) max_interval = interval;
			if ( interval < min_interval ) min_interval = interval;
			last_time = time;
		}
	}

	@Override
	public void reset() {}
}
