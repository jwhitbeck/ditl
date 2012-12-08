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
package ditl.graphs;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import ditl.Converter;
import ditl.Listener;
import ditl.Runner;
import ditl.StatefulReader;
import ditl.StatefulWriter;

public class MovementToPresenceConverter implements Converter, MovementTrace.Handler {

    private StatefulReader<MovementEvent, Movement> movement_reader;
    private StatefulWriter<PresenceEvent, Presence> presence_writer;
    private final MovementTrace _movement;
    private final PresenceTrace _presence;

    public MovementToPresenceConverter(PresenceTrace presence, MovementTrace movement) {
        _movement = movement;
        _presence = presence;
    }

    @Override
    public void convert() throws IOException {
        movement_reader = _movement.getReader();
        presence_writer = _presence.getWriter();
        movement_reader.bus().addListener(movementEventListener());
        movement_reader.stateBus().addListener(movementListener());
        final Runner runner = new Runner(_movement.maxUpdateInterval(), _movement.minTime(), _movement.maxTime());
        runner.addGenerator(movement_reader);
        runner.run();
        presence_writer.setPropertiesFromTrace(_movement);
        presence_writer.close();
        movement_reader.close();
    }

    @Override
    public Listener<MovementEvent> movementEventListener() {
        return new Listener<MovementEvent>() {
            @Override
            public void handle(long time, Collection<MovementEvent> events) throws IOException {
                for (final MovementEvent event : events)
                    switch (event.type) {
                        case IN:
                            presence_writer.append(time, new PresenceEvent(event.id, PresenceEvent.Type.IN));
                            break;
                        case OUT:
                            presence_writer.append(time, new PresenceEvent(event.id, PresenceEvent.Type.OUT));
                            break;
                    }
            }
        };
    }

    @Override
    public Listener<Movement> movementListener() {
        return new Listener<Movement>() {
            @Override
            public void handle(long time, Collection<Movement> events) throws IOException {
                final Set<Presence> initState = new HashSet<Presence>();
                for (final Movement m : events)
                    initState.add(new Presence(m.id));
                presence_writer.setInitState(time, initState);
            }
        };
    }

}
