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

import java.util.*;
import java.io.IOException ;

public class StatefulReader<E, S> extends Reader<E> {

	StateUpdater<E,S> _updater;
	Reader<S> snapshots_iterator;
	Bus<S> state_bus = null;
	
	
	StatefulReader(Store store, InputStreamOpener inputStreamOpener, long eventSeekInterval, ItemFactory<E> fact, 
				Reader<S> snapshotsReader, StateUpdater<E,S> updater, int priority, long offset) throws IOException {
		super(store, inputStreamOpener, eventSeekInterval, fact, priority, offset);
		_updater = updater;
		snapshots_iterator = snapshotsReader;
	}
	
	public Set<S> referenceState(){
		return _updater.states();
	}
	
	@Override
	public void seek(long time) throws IOException {
		snapshots_iterator.seek(time);
		if ( time == snapshots_iterator.nextTime() ){ // we have hit exactly on a snapshot, use it
			if ( snapshots_iterator.has_next_time )
				_updater.setState( snapshots_iterator.next() );
			super.seek(time);
		} else {
			long last_snap_time = snapshots_iterator.previousTime(); 
			_updater.setState( snapshots_iterator.previous() );
			super.seek(last_snap_time);
			while ( has_next_time && next_time < time+_offset ){
				for ( E event : next() ){
					_updater.handleEvent( cur_time, event); // cur_time is updated by call to next()
				}
			}
		}
		if ( state_bus != null )
			state_bus.queue(time, _updater.states());
		cur_time = time+_offset;
	}
	
	public void setStateBus(Bus<S> bus){
		state_bus = bus;
	}
	
	public Bus<S> stateBus(){
		return state_bus;
	}
	
	@Override
	public Bus<?>[] busses(){
		return new Bus<?>[]{_bus, state_bus};
	}

}
