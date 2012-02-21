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
import java.util.Set;


public abstract class StatefulTrace<E, S> extends Trace<E> {

	protected ItemFactory<S> state_factory;
	protected StateUpdaterFactory<E,S> updater_factory; 
	
	public interface Filterable<E,S> extends Trace.Filterable<E> {
		public Filter<S> stateFilter(Set<Integer> group);
	}
	
	public StatefulTrace(Store store, String name, PersistentMap info, ItemFactory<E> itemFactory, 
			ItemFactory<S> stateFactory, StateUpdaterFactory<E,S> stateUpdaterFactory)
			throws IOException {
		super(store, name, info, itemFactory);
		state_factory = stateFactory;
		updater_factory = stateUpdaterFactory;
	}
	
	public StatefulReader<E,S> getReader(int priority, long offset) throws IOException {
		Reader<S> snap_iterator = new Reader<S>(_store,
				_store.getStreamOpener(_store.snapshotsFile(_name)), 
				Math.max(snapshotInterval(), maxUpdateInterval()),
				state_factory, Trace.defaultPriority, offset);
		return new StatefulReader<E,S>(_store,
				_store.getStreamOpener(_store.traceFile(_name)),
				maxUpdateInterval(), event_factory,
				snap_iterator, updater_factory.getNew(), 
				priority, offset );
	}
	
	public StatefulReader<E,S> getReader(int priority) throws IOException {
		return getReader(priority, 0L);
	}
	
	public StatefulReader<E,S> getReader() throws IOException {
		return getReader(defaultPriority(), 0L);
	}
	
	public StatefulWriter<E, S> getWriter(long snapInterval) throws IOException {
		return new StatefulWriter<E,S>(_store, _name,
				snapInterval, updater_factory.getNew(), _info);
	}
	
	public ItemFactory<S> stateFactory(){
		return state_factory;
	}
	
	public StateUpdaterFactory<E,S> stateUpdateFactory(){
		return updater_factory;
	}
	
	@Override
	public boolean isStateful(){
		return true;
	}

}
