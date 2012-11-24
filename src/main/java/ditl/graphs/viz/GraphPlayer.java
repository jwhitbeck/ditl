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

import java.awt.Dimension;
import java.io.IOException;
import java.util.*;

import javax.swing.*;

import ditl.graphs.*;
import ditl.viz.*;



@SuppressWarnings("serial")
public class GraphPlayer extends SimplePlayer {
	
	protected GraphScene scene;
	protected GraphRunner runner;
	protected LinksSelectorPanel linksSelector;
	protected TimeUnitPanel timeUnitPanel;
	protected ReachabilitySelectorPanel reachabilitySelector; 
	protected GroupsPanel groups;
	protected MovementTrace movement;
	
	public GraphPlayer(){
		build();
	}
	
	protected void build(){
		scene = new GraphScene();
		runner = new GraphRunner();
		linksSelector = new LinksSelectorPanel(runner, scene);
		groups = new GroupsPanel(runner, scene);
		timeUnitPanel = new TimeUnitPanel(controls);
		reachabilitySelector = new ReachabilitySelectorPanel(runner, scene);
		
		scene.setPreferredSize(new Dimension(700,500));
		runner.addMovementHandler(scene);
		runner.addLinkHandler(scene);
		runner.addEdgeHandler(scene);
		runner.addGroupHandler(scene);
		scene.setGroupColorMap(groups.colorMap());
		
		List<JPanel> widgets = new LinkedList<JPanel>();
		widgets.add(timeUnitPanel);
		widgets.add(new SpeedPanel(runner));
		widgets.add(new FPSPanel(runner));
		widgets.add(new ShowIdsPanel(scene));
		widgets.add(new ToggleAntialiasingPanel(scene));
		widgets.add(linksSelector); 
		widgets.add(reachabilitySelector);
		widgets.add(groups);
		
		init(scene,runner, widgets);
		enableControls(false);
	}
	
	protected void loadReaders(){
		movement = (MovementTrace)_store.listTraces(MovementTrace.type).get(0); // use first movement trace
		
		setMovementTrace(movement);
		
		if ( groups != null ){
			groups.setStore(_store);
			groups.load(_store.listTraces(GroupTrace.type));
		}
		
		if ( linksSelector != null ){
			linksSelector.setStore(_store);
			linksSelector.load(_store.listTraces(LinkTrace.type));
		}

		if ( reachabilitySelector != null ){
			reachabilitySelector.setStore(_store);
			reachabilitySelector.load(_store.listTraces(ReachabilityTrace.type));
		}
	}
			
	protected void setMovementTrace(MovementTrace movement) {
		try {
			scene.updateSize(movement.minX(),movement.minY(),movement.maxX(),movement.maxY());
			scene.setIdMap(movement.idMap());
			runner.setMovementTrace(movement);
			runner.setTicsPerSecond(movement.ticsPerSecond());
			controls.setBounds(movement.ticsPerSecond(),movement.minTime(), movement.maxTime());
			timeUnitPanel.setPreferredTimeUnit(movement);
			runner.seek(movement.minTime());
		} catch ( IOException ioe ){
			JOptionPane.showMessageDialog(this, "Error opening movement file", "Warning", JOptionPane.ERROR_MESSAGE);
		}
	}
}
