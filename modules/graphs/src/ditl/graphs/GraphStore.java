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
package ditl.graphs;

import java.io.IOException;

import ditl.*;



public class GraphStore {
	
	final static int maxNumNodes = 32768; // 2^15

	public final static String defaultEdgesName = "edges";
	public final static String defaultLinksName = "links";
	public final static String defaultPresenceName = "presence";
	public final static String defaultGroupsName = "groups";
	public final static String defaultMovementName = "movement";
	public final static String defaultBeaconsName = "beacons";
	public final static String defaultConnectedComponentsName = "ccs";
	
	public final static String linksType = "links";
	public final static String edgesType = "edges";
	public final static String presenceType = "presence";
	public final static String groupType = "groups";
	public final static String beaconsType = "beacons";
	public final static String movementType = "movement";
	public final static String connectedComponentsType = "ccs";
	
	public final static int defaultPresenceReaderPriority = 10;
	public final static int defaultMovementReaderPriority = 20;
	
	protected Store _store;
	protected WritableStore writable_store = null;
	
	public GraphStore(Store store){
		_store = store;
		if ( store instanceof WritableStore ){
			writable_store = (WritableStore)store;
		}
	}
	
	public StatefulReader<LinkEvent,Link> getLinkReader(Trace trace) throws IOException {
		return _store.getStatefulReader(trace, LinkEvent.factory(), Link.factory(), new LinkUpdater());
	}
	
	public StatefulReader<PresenceEvent,Presence> getPresenceReader(Trace trace) throws IOException {
		return _store.getStatefulReader(trace, PresenceEvent.factory(), Presence.factory(), new PresenceUpdater());
	}
	
	public StatefulReader<EdgeEvent,Edge> getEdgeReader(Trace trace) throws IOException {
		return _store.getStatefulReader(trace, EdgeEvent.factory(), Edge.factory(), new EdgeUpdater());
	}
	
	public StatefulReader<GroupEvent,Group> getGroupReader(Trace trace) throws IOException {
		return _store.getStatefulReader(trace, GroupEvent.factory(), Group.factory(), new GroupUpdater());
	}
	
	public Reader<Edge> getBeaconsReader(Trace trace) throws IOException {
		return _store.getReader(trace, Edge.factory());
	}

	public StatefulReader<MovementEvent,Movement> getMovementReader(Trace trace) throws IOException {
		return _store.getStatefulReader(trace, MovementEvent.factory(), Movement.factory(), new MovementUpdater());
	}

	public StatefulWriter<LinkEvent,Link> getLinkWriter(String name, long snapInterval) throws IOException {
		if ( writable_store == null ) throw new UnsupportedOperationException();
		StatefulWriter<LinkEvent,Link> writer = writable_store.getStatefulWriter(name, new LinkUpdater(), snapInterval);
		writer.setProperty(Trace.typeKey, linksType);
		return writer;
	}
	
	public StatefulWriter<PresenceEvent,Presence> getPresenceWriter(String name, long snapInterval) throws IOException {
		if ( writable_store == null ) throw new UnsupportedOperationException();
		StatefulWriter<PresenceEvent,Presence> writer = writable_store.getStatefulWriter(name, new PresenceUpdater(), snapInterval);
		writer.setProperty(Trace.typeKey, presenceType);
		writer.setProperty(Trace.defaultPriorityKey, defaultPresenceReaderPriority);
		return writer;
	}
	
	public StatefulWriter<EdgeEvent,Edge> getEdgeWriter(String name, long snapInterval) throws IOException {
		if ( writable_store == null ) throw new UnsupportedOperationException();
		StatefulWriter<EdgeEvent,Edge> writer = writable_store.getStatefulWriter(name, new EdgeUpdater(), snapInterval);
		writer.setProperty(Trace.typeKey, edgesType);
		return writer;
	}
	
	public StatefulWriter<GroupEvent,Group> getGroupWriter(String name, long snapInterval) throws IOException {
		if ( writable_store == null ) throw new UnsupportedOperationException();
		StatefulWriter<GroupEvent,Group> writer = writable_store.getStatefulWriter(name, new GroupUpdater(), snapInterval);
		writer.setProperty(Trace.typeKey, groupType);
		return writer;
	}
	
	public Writer<Edge> getBeaconsWriter(String name) throws IOException {
		if ( writable_store == null ) throw new UnsupportedOperationException();
		Writer<Edge> writer = writable_store.getWriter(name);
		writer.setProperty(Trace.typeKey, beaconsType);
		return writer;
	}
	
	public StatefulWriter<MovementEvent,Movement> getMovementWriter(String name, long snapInterval) throws IOException {
		if ( writable_store == null ) throw new UnsupportedOperationException();
		StatefulWriter<MovementEvent,Movement> writer = writable_store.getStatefulWriter(name, new MovementUpdater(), snapInterval);
		writer.setProperty(Trace.defaultPriorityKey, defaultMovementReaderPriority);
		writer.setProperty(Trace.typeKey, movementType);
		return writer;
	}
	
}
