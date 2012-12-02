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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import ditl.Listener;
import ditl.StatefulListener;
import ditl.Store;
import ditl.Trace;
import ditl.graphs.Group;
import ditl.graphs.GroupEvent;
import ditl.graphs.GroupTrace;
import ditl.viz.Scene;

@SuppressWarnings("serial")
public class GroupsPanel extends JPanel implements
        GroupTrace.Handler, ActionListener, ItemListener {

    public final static Color[] colorList = { Color.BLUE, Color.RED, Color.GREEN,
            Color.MAGENTA, Color.ORANGE, Color.DARK_GRAY,
            Color.YELLOW };

    public final static Color noGroupColor = Color.BLACK;

    protected int rectSize = 10;
    protected int nextColor = 0;
    protected Store _store;
    protected JComboBox groupsChooser;
    protected GroupRunner runner;
    protected JCheckBox enabledBox;
    protected Scene _scene;
    protected JPanel colorPanel;

    private final Map<Integer, Color> color_map = new LinkedHashMap<Integer, Color>();
    private GroupTrace cur_groups = null;

    public GroupsPanel(GroupRunner groupRunner, Scene scene) {
        _scene = scene;
        runner = groupRunner;
        runner.addGroupHandler(this);

        setBorder(BorderFactory.createTitledBorder("Groups"));

        final GridBagLayout gridbag = new GridBagLayout();
        final GridBagConstraints c = new GridBagConstraints();

        c.gridy = 0;
        c.gridx = 0;
        c.anchor = GridBagConstraints.EAST;
        final JLabel enabledLabel = new JLabel("Enabled");
        gridbag.setConstraints(enabledLabel, c);
        add(enabledLabel);

        c.gridx = 1;
        enabledBox = new JCheckBox();
        enabledBox.setSelected(false);
        gridbag.setConstraints(enabledBox, c);
        add(enabledBox);

        c.gridy = 1;
        c.gridx = 0;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        groupsChooser = new JComboBox();
        gridbag.setConstraints(groupsChooser, c);
        add(groupsChooser);

        c.gridy = 2;
        c.gridx = 0;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.WEST;
        colorPanel = new JPanel();
        colorPanel.setLayout(new GridBagLayout());
        gridbag.setConstraints(colorPanel, c);
        add(colorPanel);

        setLayout(gridbag);
        setVisible(false);
    }

    public void setStore(Store store) {
        _store = store;
    }

    public Map<Integer, Color> colorMap() {
        return Collections.unmodifiableMap(color_map);
    }

    private JPanel createColorRect(Color color) {
        final JPanel rect = new JPanel();
        rect.setPreferredSize(new Dimension(rectSize, rectSize));
        rect.setBackground(color);
        return rect;
    }

    @Override
    public Listener<GroupEvent> groupEventListener() {
        return new Listener<GroupEvent>() {
            @Override
            public void handle(long time, Collection<GroupEvent> events) {
                for (final GroupEvent gev : events)
                    switch (gev.type()) {
                        case NEW:
                            addGroup(gev.gid());
                            updateColorPanel();
                            break;
                        case DELETE:
                            color_map.remove(gev.gid());
                            updateColorPanel();
                            break;
                    }
            }
        };
    }

    private void addGroup(Integer gid) {
        final Color color = colorList[(nextColor++ % colorList.length)];
        color_map.put(gid, color);
    }

    @Override
    public Listener<Group> groupListener() {
        return new StatefulListener<Group>() {
            @Override
            public void handle(long time, Collection<Group> events) {
                for (final Group g : events)
                    addGroup(g.gid());
                updateColorPanel();
            }

            @Override
            public void reset() {
                color_map.clear();
                nextColor = 0;
            }
        };
    }

    private void updateColorPanel() {
        colorPanel.removeAll();
        if (enabledBox.isSelected()) {
            final GridBagLayout gridbag = (GridBagLayout) colorPanel.getLayout();
            int i = 0;
            for (final Map.Entry<Integer, Color> e : color_map.entrySet()) {
                final GridBagConstraints c = new GridBagConstraints();
                c.insets = new Insets(3, 0, 3, 3);
                c.gridx = 0;
                c.gridy = i;
                final JPanel colorRect = createColorRect(e.getValue());
                gridbag.setConstraints(colorRect, c);
                colorPanel.add(colorRect);

                final Integer gid = e.getKey();
                c.gridx = 1;
                c.insets = new Insets(0, 0, 0, 0);
                c.fill = GridBagConstraints.HORIZONTAL;
                c.weightx = 1.0;
                String label = null;
                if (cur_groups.hasLabels())
                    label = cur_groups.getLabel(gid);
                if (label == null)
                    label = gid.toString();
                final JLabel groupLabel = new JLabel(label);
                gridbag.setConstraints(groupLabel, c);
                colorPanel.add(groupLabel);

                i++;
            }
        }
        colorPanel.revalidate();
    }

    public void load(Collection<GroupTrace> traces) {
        groupsChooser.removeActionListener(this);
        groupsChooser.removeAllItems();
        enabledBox.removeItemListener(this);
        if (!traces.isEmpty()) {
            for (final Trace<?> trace : traces)
                groupsChooser.addItem(trace.name());
            enabledBox.setSelected(true);
            groupsChooser.addActionListener(this);
            groupsChooser.setSelectedIndex(0);
            enabledBox.addItemListener(this);
            setVisible(true);
        } else
            setVisible(false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        updateGroupTrace();
    }

    @Override
    public void itemStateChanged(ItemEvent arg0) {
        groupsChooser.setEnabled(enabledBox.isSelected());
        updateGroupTrace();
        updateColorPanel();
    }

    private void updateGroupTrace() {
        String name = "null";
        try {
            cur_groups = null;
            if (enabledBox.isSelected()) {
                name = (String) groupsChooser.getSelectedItem();
                cur_groups = _store.getTrace(name);
            }
            runner.setGroupTrace(cur_groups);
        } catch (final Exception ioe) {
            JOptionPane.showMessageDialog(this, "Failed to load groups file '" + name + "'", "Warning", JOptionPane.ERROR_MESSAGE);
        }
        _scene.repaint();
    }
}
