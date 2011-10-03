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

public class StatefulWriter<E, S> extends Writer<E> {

	long last_snap;
	long snap_interval;
	BufferedWriter snap_writer;
	StateUpdater<E,S> _updater;
	
	public StatefulWriter(Store store, String name, long snapInterval, 
			StateUpdater<E,S> updater, PersistentMap info) throws IOException {
		super(store, name, info);
		snap_interval = snapInterval;
		last_snap = -Trace.INFINITY;
		_updater = updater;
		snap_writer = new BufferedWriter( new OutputStreamWriter(_store.getOutputStream(_store.snapshotsFile(_name))));
		setProperty(Trace.snapshotIntervalKey, snap_interval);
	}
	
	public Set<S> states(){
		return _updater.states();
	}
	
	@Override
	public void write(long time, E event) throws IOException {
		super.write(time, event);
		_updater.handleEvent(time, event);
	}
	
	@Override
	void updateTime(long time) throws IOException {
		super.updateTime(time);
		if ( last_snap == -Trace.INFINITY ){ // no snap has yet been made
			last_snap = last_time;
			write_snapshot (last_snap);
		} else {
			while ( time - last_snap >= snap_interval ){
				last_snap += snap_interval;
				write_snapshot (last_snap);
			}
		}
	}
	
	@Override
	public void close() throws IOException {
		super.close();
		snap_writer.close();
	}
	
	public void setInitState(long time, Collection<S> states) throws IOException {
		_updater.setState(states);
		write_snapshot(time);
		last_snap = time;
		min_time = time;
	}
	
	private void write_snapshot(long time) throws IOException {
		snap_writer.write(time+"\n");
		for ( S state : _updater.states() )
			snap_writer.write(state+"\n");
	}
}
