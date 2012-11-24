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
package ditl.transfers.viz;

import java.awt.Dimension;
import java.io.IOException;
import java.util.*;

import javax.swing.*;

import ditl.graphs.viz.*;
import ditl.transfers.*;
import ditl.viz.*;



@SuppressWarnings("serial")
public class TransferPlayer extends GraphPlayer {
	
	protected TransferSelectorPanel transferSelector;
	protected MessageSelectorPanel messageSelector;
	
	@Override
	protected void build(){
		scene = new TransferScene();
		runner = new RoutingRunner();
		linksSelector = new LinksSelectorPanel(runner, scene);
		transferSelector = new TransferSelectorPanel((RoutingRunner)runner,scene);
		timeUnitPanel = new TimeUnitPanel(controls);
		messageSelector = new MessageSelectorPanel((RoutingRunner)runner,(TransferScene)scene);
		
		scene.setPreferredSize(new Dimension(700,500));
		runner.addMovementHandler(scene);
		runner.addLinkHandler(scene);
		runner.addEdgeHandler(scene);
		((RoutingRunner)runner).addTransferHandler((TransferScene)scene);
		
		List<JPanel> widgets = new LinkedList<JPanel>();
		widgets.add(timeUnitPanel);
		widgets.add(new SpeedPanel(runner));
		widgets.add(new FPSPanel(runner));
		widgets.add(new ShowIdsPanel(scene));
		widgets.add(new ToggleAntialiasingPanel(scene));
		widgets.add(linksSelector); 
		widgets.add(transferSelector);
		widgets.add(messageSelector);
		
		init(scene,runner, widgets);
		
		enableControls(false);
	}
	
	@Override
	public void loadReaders(){
		super.loadReaders();
		
		transferSelector.setStore(_store);
		transferSelector.load(_store.listTraces(TransferTrace.type));
		
		BufferTrace buffers = (BufferTrace)_store.listTraces(BufferTrace.type).get(0);
		try {
			((RoutingRunner)runner).setBufferTrace(buffers);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, "Failed to load buffer trace '"+buffers+"'", "Warning", JOptionPane.ERROR_MESSAGE);
		}
		
		MessageTrace messages = (MessageTrace)_store.listTraces(MessageTrace.type).get(0);
		try {
			((RoutingRunner)runner).setMessageTrace(messages);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, "Failed to load buffer trace '"+messages+"'", "Warning", JOptionPane.ERROR_MESSAGE);
		}
		
		runner.seek(movement.minTime());
	}
}
