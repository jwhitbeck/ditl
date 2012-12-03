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
import java.util.Collection;
import java.util.Set;

public class StatefulWriter<E extends Item, S extends Item> extends Writer<E> {

    private final StateUpdater<E, S> _updater;

    final static byte STATE = 1;

    public StatefulWriter(StatefulTrace<E, S> trace) throws IOException {
        super(trace);
        _updater = trace.getNewUpdaterFactory();
    }

    public Set<S> states() {
        return _updater.states();
    }

    @Override
    public void write(long time, E event) {
        super.write(time, event);
        _updater.handleEvent(time, event);
    }

    public void setInitState(long time, Collection<S> states) throws IOException {
        _updater.setState(states);
        min_time = time;
        markPosition(time);
    }

    @Override
    void markPosition(long time) throws IOException {
        super.markPosition(time);
        for (S state : _updater.states()) {
            state.write(buffer);
        }
        writeItemBlockHeader(STATE, time);
        writeItemBlock();
    }
}
