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
package ditl.graphs.viz;

import java.io.IOException;

import ditl.*;
import ditl.graphs.*;
import ditl.graphs.GroupTrace.Handler;
import ditl.viz.SceneRunner;



@SuppressWarnings("serial")
public class GraphRunner extends SceneRunner 
	implements EdgeRunner, LinkRunner, MovementRunner, GroupRunner {
	
	protected Bus<Link> linkBus = new Bus<Link>();
	protected Bus<LinkEvent> linkEventBus = new Bus<LinkEvent>();
	protected Bus<Edge> edgeBus = new Bus<Edge>();
	protected Bus<EdgeEvent> edgeEventBus = new Bus<EdgeEvent>();
	protected Bus<Movement> movementBus = new Bus<Movement>();
	protected Bus<MovementEvent> movementEventBus = new Bus<MovementEvent>();
	protected Bus<Group> groupBus = new Bus<Group>();
	protected Bus<GroupEvent> groupEventBus = new Bus<GroupEvent>();
	protected StatefulReader<MovementEvent,Movement> movement_reader;
	protected StatefulReader<LinkEvent,Link> links_reader;
	protected StatefulReader<EdgeEvent,Edge> edges_reader;
	protected StatefulReader<GroupEvent,Group> groups_reader;
	
	@Override
	public void setMovementTrace(MovementTrace movement) throws IOException {
		runner = new Runner(incrTime(), movement.minTime(), movement.maxTime());
		movement_reader = movement.getReader();
		movement_reader.setBus(movementEventBus);
		movement_reader.setStateBus(movementBus);
		runner.addGenerator(movement_reader);
	}
	
	@Override
	public void addMovementHandler(MovementTrace.Handler handler) {
		movementBus.addListener(handler.movementListener());
		movementEventBus.addListener(handler.movementEventListener());
	}

	@Override
	public void setLinkTrace(LinkTrace links) throws IOException {
		linkEventBus.reset();
		linkBus.reset();
		if ( links_reader != null ){
			runner.removeGenerator(links_reader);
			links_reader.close();
		}
		if ( links != null ){
			links_reader = links.getReader();
			links_reader.setBus(linkEventBus);
			links_reader.setStateBus(linkBus);
			links_reader.seek(runner.time());
			linkBus.flush();
			runner.addGenerator(links_reader);
		}
	}
	
	@Override
	public void addLinkHandler(LinkTrace.Handler handler) {
		linkBus.addListener(handler.linkListener());
		linkEventBus.addListener(handler.linkEventListener());
	}
	
	@Override
	public void setEdgeTrace(EdgeTrace edges) throws IOException {
		edgeBus.reset();
		edgeEventBus.reset();
		if ( edges_reader != null ){
			runner.removeGenerator(edges_reader);
			edges_reader.close();
		}
		if ( edges != null ){
			edges_reader = edges.getReader();
			edges_reader.setBus(edgeEventBus);
			edges_reader.setStateBus(edgeBus);
			edges_reader.seek(runner.time());
			edgeBus.flush();
			runner.addGenerator(edges_reader);
		}
	}
	
	@Override
	public void addEdgeHandler(EdgeTrace.Handler handler) {
		edgeBus.addListener(handler.edgeListener());
		edgeEventBus.addListener(handler.edgeEventListener());
	}

	@Override
	public void addGroupHandler(Handler handler) {
		groupBus.addListener(handler.groupListener());
		groupEventBus.addListener(handler.groupEventListener());
	}

	@Override
	public void setGroupTrace(GroupTrace groups) throws IOException {
		groupBus.reset();
		groupEventBus.reset();
		if ( groups_reader != null ){
			runner.removeGenerator(groups_reader);
			groups_reader.close();
		}
		if ( groups != null ){
			groups_reader = groups.getReader();
			groups_reader.setBus(groupEventBus);
			groups_reader.setStateBus(groupBus);
			groups_reader.seek(runner.time());
			groupBus.flush();
			runner.addGenerator(groups_reader);
		}
	}
}
