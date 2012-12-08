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
package ditl.transfers;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import net.sf.json.JSONObject;
import ditl.Listener;
import ditl.StateUpdater;
import ditl.StateUpdaterFactory;
import ditl.StatefulTrace;
import ditl.Store;
import ditl.Trace;

@Trace.Type("messages")
public class MessageTrace extends StatefulTrace<MessageEvent, Message> {

    public final static int defaultPriority = 50;

    public final static class Updater implements StateUpdater<MessageEvent, Message> {
        private final Set<Message> messages = new HashSet<Message>();

        @Override
        public void handleEvent(long time, MessageEvent event) {
            final Message msg = event.message();
            if (event.isNew())
                messages.add(msg);
            else
                messages.remove(msg);
        }

        @Override
        public void setState(Collection<Message> state) {
            messages.clear();
            messages.addAll(state);
        }

        @Override
        public Set<Message> states() {
            return messages;
        }
    }

    public interface Handler {
        Listener<MessageEvent> messageEventListener();

        Listener<Message> messageListener();
    }

    public MessageTrace(Store store, String name, JSONObject config) throws IOException {
        super(store, name, config, new MessageEvent.Factory(), new Message.Factory(),
                new StateUpdaterFactory<MessageEvent, Message>() {
                    @Override
                    public StateUpdater<MessageEvent, Message> getNew() {
                        return new MessageTrace.Updater();
                    }
                });
        set(Trace.defaultPriorityKey, defaultPriority);
    }

}
