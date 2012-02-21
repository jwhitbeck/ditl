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
	String _name;
	
	public Writer(Store store, String name, PersistentMap info) throws IOException {
		if ( ! ( store instanceof WritableStore )) throw new IOException();
		_store = (WritableStore)store;
		if ( _store.isAlreadyWriting(name) ) throw new IOException();
		_info = info;
		_name = name;
		info_out = _store.getOutputStream(_store.infoFile(_name));
		_store.notifyOpen(_name, this);
		writer = new BufferedWriter(new OutputStreamWriter(_store.getOutputStream(_store.traceFile(_name))));
		last_time = -Trace.INFINITY;
		addListener(this);
		min_time = Trace.INFINITY;
		max_time = -Trace.INFINITY;
		min_interval = Trace.INFINITY;
		max_interval = -Trace.INFINITY;
	}
	
	void setRemainingInfo(){
		_info.setIfUnset(Trace.maxTimeKey, max_time);
		_info.setIfUnset(Trace.minTimeKey, min_time);
		if ( max_interval < 0 ){ // single event trace
			max_interval = Long.parseLong(_info.get(Trace.maxTimeKey)) - Long.parseLong(_info.get(Trace.minTimeKey));
			min_interval = max_interval;
		}
		_info.setIfUnset(Trace.maxUpdateIntervalKey, max_interval);
		_info.setIfUnset(Trace.minUpdateIntervalKey, min_interval);
		_info.setIfUnset(Trace.defaultPriorityKey, Trace.defaultPriority);
	}
	
	public void close() throws IOException {
		writer.close();
		setRemainingInfo();
		_info.save(info_out);
		_store.notifyClose(_name);
	}
	
	public void setProperty(String key, Object value){
		_info.put(key, value);
	}
	
	public void setPropertiesFromTrace(Trace<?> trace){
		_info.setIfUnset(Trace.minTimeKey, trace.minTime());
		_info.setIfUnset(Trace.maxTimeKey, trace.maxTime());
		_info.setIfUnset(Trace.timeUnitKey, trace.timeUnit());
		String id_map_str = trace.getValue(Trace.idMapKey);
		if ( id_map_str != null )
			_info.setIfUnset(Trace.idMapKey, id_map_str);
	}

	public void append(long time, I item) throws IOException {
		updateTime(time);
		write(time,item);
	}
	
	@Override
	public void handle(long time, Collection<I> items) throws IOException {
		updateTime(time);
		for ( I item : items )
			write(time,item);
	}
	
	public void write(long time, I item) throws IOException {
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
