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

public class BufferUpdater implements StateUpdater<BufferEvent,Buffer>{

	private Map<Integer,Buffer> buffer_map = new HashMap<Integer,Buffer>();
	private Set<Buffer> buffers = new HashSet<Buffer>();
	
	@Override
	public void handleEvent(long time, BufferEvent event) {
		Buffer b;
		Integer id = event._id;
		switch ( event._type ){
		case BufferEvent.ADD:
			b = buffer_map.get(id);
			b.msg_ids.add(event.msg_id);
			break;
		case BufferEvent.REMOVE:
			b = buffer_map.get(id);
			b.msg_ids.remove(event.msg_id);
			break;
		case BufferEvent.IN:
			b = new Buffer(id);
			buffer_map.put(id, b);
			break;
		case BufferEvent.OUT:
			b = buffer_map.get(id);
			buffers.remove(b);
			buffer_map.remove(id);
			break;
		}
	}

	@Override
	public void setState(Collection<Buffer> state) {
		buffer_map.clear();
		buffers.clear();
		for ( Buffer b : state ){
			buffer_map.put(b._id, b);
			buffers.add(b);
		}
	}

	@Override
	public Set<Buffer> states() {
		return buffers;
	}

}
