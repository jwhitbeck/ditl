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

public abstract class StatefulTrace<E extends Item, S extends Item> extends Trace<E> {

    protected Item.Factory<S> state_factory;
    protected StateUpdaterFactory<E, S> updater_factory;

    public interface Filterable<E extends Item, S extends Item> extends Trace.Filterable<E> {
        public Filter<S> stateFilter(Set<Integer> group);
    }

    public StatefulTrace(Store store, String name, PersistentMap info, Item.Factory<E> itemFactory,
            Item.Factory<S> stateFactory, StateUpdaterFactory<E, S> stateUpdaterFactory)
            throws IOException {
        super(store, name, info, itemFactory);
        state_factory = stateFactory;
        updater_factory = stateUpdaterFactory;
    }

    @Override
    public StatefulReader<E, S> getReader(int priority, long offset) throws IOException {
        return new StatefulReader<E, S>(_store,
                _name,
                event_factory,
                state_factory,
                updater_factory.getNew(),
                priority, offset);
    }

    @Override
    public StatefulReader<E, S> getReader(int priority) throws IOException {
        return getReader(priority, 0L);
    }

    @Override
    public StatefulReader<E, S> getReader() throws IOException {
        return getReader(defaultPriority(), 0L);
    }

    @Override
    public StatefulWriter<E, S> getWriter() throws IOException {
        return new StatefulWriter<E, S>(_store, _name, updater_factory.getNew(), _info);
    }

    public Item.Factory<S> stateFactory() {
        return state_factory;
    }

    public StateUpdaterFactory<E, S> stateUpdateFactory() {
        return updater_factory;
    }

}
