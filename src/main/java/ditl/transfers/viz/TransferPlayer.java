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
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import ditl.graphs.viz.EdgeSelectorPanel;
import ditl.graphs.viz.GraphPlayer;
import ditl.graphs.viz.ShowIdsPanel;
import ditl.transfers.BufferTrace;
import ditl.transfers.MessageTrace;
import ditl.transfers.TransferTrace;
import ditl.viz.FPSPanel;
import ditl.viz.SpeedPanel;
import ditl.viz.TimeUnitPanel;
import ditl.viz.ToggleAntialiasingPanel;

@SuppressWarnings("serial")
public class TransferPlayer extends GraphPlayer {

    protected TransferSelectorPanel transferSelector;
    protected MessageSelectorPanel messageSelector;

    @Override
    protected void build() {
        scene = new TransferScene();
        runner = new RoutingRunner();
        edgesSelector = new EdgeSelectorPanel(runner, scene);
        transferSelector = new TransferSelectorPanel((RoutingRunner) runner, scene);
        timeUnitPanel = new TimeUnitPanel(controls);
        messageSelector = new MessageSelectorPanel((RoutingRunner) runner, (TransferScene) scene);

        scene.setPreferredSize(new Dimension(700, 500));
        runner.addMovementHandler(scene);
        runner.addEdgesHandler(scene);
        runner.addArcHandler(scene);
        ((RoutingRunner) runner).addTransferHandler((TransferScene) scene);

        final List<JPanel> widgets = new LinkedList<JPanel>();
        widgets.add(timeUnitPanel);
        widgets.add(new SpeedPanel(runner));
        widgets.add(new FPSPanel(runner));
        widgets.add(new ShowIdsPanel(scene));
        widgets.add(new ToggleAntialiasingPanel(scene));
        widgets.add(edgesSelector);
        widgets.add(transferSelector);
        widgets.add(messageSelector);

        init(scene, runner, widgets);

        enableControls(false);
    }

    @Override
    public void loadReaders() {
        super.loadReaders();

        transferSelector.setStore(_store);
        transferSelector.load(_store.listTraces(TransferTrace.class));

        final BufferTrace buffers = _store.listTraces(BufferTrace.class).get(0);
        try {
            ((RoutingRunner) runner).setBufferTrace(buffers);
        } catch (final IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to load buffer trace '" + buffers + "'", "Warning", JOptionPane.ERROR_MESSAGE);
        }

        final MessageTrace messages = _store.listTraces(MessageTrace.class).get(0);
        try {
            ((RoutingRunner) runner).setMessageTrace(messages);
        } catch (final IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to load buffer trace '" + messages + "'", "Warning", JOptionPane.ERROR_MESSAGE);
        }

        runner.seek(movement.minTime());
    }
}
