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
package ditl.graphs.viz;

import java.awt.Dimension;
import java.io.*;
import java.util.*;

import javax.swing.*;

import ditl.*;
import ditl.graphs.*;
import ditl.viz.*;



@SuppressWarnings("serial")
public class GraphPlayer extends SimplePlayer {
	
	protected GraphScene scene;
	protected GraphRunner runner;
	protected LinksSelectorPanel linksSelector;
	protected GroupsPanel groups;
	protected MovementTrace movement;
	
	public GraphPlayer(){
		build();
	}
	
	protected void build(){
		scene = new GraphScene();
		runner = new GraphRunner();
		linksSelector = new LinksSelectorPanel(runner, scene);
		groups = new GroupsPanel();
		
		scene.setPreferredSize(new Dimension(700,500));
		runner.addMovementHandler(scene);
		runner.addLinkHandler(scene);
		runner.addEdgeHandler(scene);
		
		List<JPanel> widgets = new LinkedList<JPanel>();
		widgets.add(new SpeedPanel(runner));
		widgets.add(new FPSPanel(runner));
		widgets.add(new ShowIdsPanel(scene));
		widgets.add(new ToggleAntialiasingPanel(scene));
		widgets.add(linksSelector); 
		widgets.add(groups);
		
		init(scene,runner, widgets);
		enableControls(false);
	}
	
	protected void loadReaders(){
		movement = new MovementTrace(_store.listTraces(GraphStore.movementType).get(0)); // use first movement trace
		
		if ( groups != null ){
			List<Trace> groupsTraces = _store.listTraces(GraphStore.groupType);
			if ( ! groupsTraces.isEmpty() )
				loadGroups(groupsTraces.get(0));
		}
		
		setMovementReader(movement);
		
		if ( linksSelector != null ){
			linksSelector.setStore(_store);
			linksSelector.load(_store.listTraces(GraphStore.linksType));
		}		
	}
	
	protected void loadGroups(Trace groupTrace){
		try {
			StatefulReader<GroupEvent,Group> groupReader = new GraphStore(_store).getGroupReader(groupTrace);
			Set<Group> grps = GroupTrace.staticGroups(groupReader);
			Groups g = new Groups(grps, new GroupTrace(groupTrace));
			if ( g != null ){
				groups.load(g);
				scene.setGroups(g);
			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, "Error opening groups file", "Warning", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	protected void setMovementReader(MovementTrace movement) {
		StatefulReader<MovementEvent, Movement> movementReader = null;
		try {
			movementReader = new GraphStore(_store).getMovementReader(movement);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, "Failed to load movement file '"+movement.name()+"'", "Warning", JOptionPane.ERROR_MESSAGE);
		}
		scene.updateSize(movement.minX(),movement.minY(),movement.maxX(),movement.maxY());
		runner.setMovementReader(movementReader);
		runner.setTicsPerSecond(movement.ticsPerSecond());
		controls.updateTimes(movement.ticsPerSecond(),movement.minTime(), movement.maxTime());
		runner.seek(movement.minTime());
	}
}
