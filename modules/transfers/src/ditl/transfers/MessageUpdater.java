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
package ditl.transfers;

import java.util.*;

import ditl.StateUpdater;

public class MessageUpdater implements StateUpdater<MessageEvent,Message> {
	private Set<Message> messages = new HashSet<Message>();

	@Override
	public void handleEvent(long time, MessageEvent event) {
		Message msg = event.message();
		if ( event.isNew() )
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
