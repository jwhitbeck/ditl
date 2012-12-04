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
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import ditl.graphs.EdgeTrace;
import ditl.graphs.GroupTrace;
import ditl.graphs.MovementTrace;
import ditl.graphs.ReachabilityTrace;
import ditl.viz.FPSPanel;
import ditl.viz.SimplePlayer;
import ditl.viz.SpeedPanel;
import ditl.viz.TimeUnitPanel;
import ditl.viz.ToggleAntialiasingPanel;

@SuppressWarnings("serial")
public class GraphPlayer extends SimplePlayer {

    protected GraphScene scene;
    protected GraphRunner runner;
    protected EdgeSelectorPanel edgesSelector;
    protected TimeUnitPanel timeUnitPanel;
    protected ReachabilitySelectorPanel reachabilitySelector;
    protected GroupsPanel groups;
    protected MovementTrace movement;

    public GraphPlayer() {
        build();
    }

    protected void build() {
        scene = new GraphScene();
        runner = new GraphRunner();
        edgesSelector = new EdgeSelectorPanel(runner, scene);
        groups = new GroupsPanel(runner, scene);
        timeUnitPanel = new TimeUnitPanel(controls);
        reachabilitySelector = new ReachabilitySelectorPanel(runner, scene);

        scene.setPreferredSize(new Dimension(700, 500));
        runner.addMovementHandler(scene);
        runner.addEdgesHandler(scene);
        runner.addArcHandler(scene);
        runner.addGroupHandler(scene);
        scene.setGroupColorMap(groups.colorMap());

        final List<JPanel> widgets = new ArrayList<JPanel>();
        widgets.add(timeUnitPanel);
        widgets.add(new SpeedPanel(runner));
        widgets.add(new FPSPanel(runner));
        widgets.add(new ShowIdsPanel(scene));
        widgets.add(new ToggleAntialiasingPanel(scene));
        widgets.add(edgesSelector);
        widgets.add(reachabilitySelector);
        widgets.add(groups);

        init(scene, runner, widgets);
        enableControls(false);
    }

    @Override
    protected void loadReaders() {
        // use first movement trace
        movement = _store.listTraces(MovementTrace.class).get(0);

        setMovementTrace(movement);

        if (groups != null) {
            groups.setStore(_store);
            groups.load(_store.listTraces(GroupTrace.class));
        }

        if (edgesSelector != null) {
            edgesSelector.setStore(_store);
            edgesSelector.load(_store.listTraces(EdgeTrace.class));
        }

        if (reachabilitySelector != null) {
            reachabilitySelector.setStore(_store);
            reachabilitySelector.load(_store.listTraces(ReachabilityTrace.class));
        }
    }

    protected void setMovementTrace(MovementTrace movement) {
        try {
            scene.updateSize(movement.minX(), movement.minY(), movement.maxX(), movement.maxY());
            scene.setIdMap(movement.idMap());
            runner.setMovementTrace(movement);
            runner.setTicsPerSecond(movement.ticsPerSecond());
            controls.setBounds(movement.ticsPerSecond(), movement.minTime(), movement.maxTime());
            timeUnitPanel.setPreferredTimeUnit(movement);
            runner.seek(movement.minTime());
        } catch (final IOException ioe) {
            JOptionPane.showMessageDialog(this, "Error opening movement file", "Warning", JOptionPane.ERROR_MESSAGE);
        }
    }
}
