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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ditl.Listener;
import ditl.StatefulListener;
import ditl.transfers.Buffer;
import ditl.transfers.BufferEvent;
import ditl.transfers.BufferTrace;
import ditl.transfers.Message;
import ditl.transfers.MessageEvent;
import ditl.transfers.MessageTrace;

@SuppressWarnings("serial")
public class MessageSelectorPanel extends JPanel
        implements ActionListener, ItemListener, MessageTrace.Handler, BufferTrace.Handler {

    protected JComboBox messageChooser;
    protected RoutingRunner runner;
    protected JCheckBox enabledBox;
    protected TransferScene scene;
    private final Map<Integer, Set<Integer>> message_holders = new HashMap<Integer, Set<Integer>>();
    Integer cur_msg = null;

    public MessageSelectorPanel(RoutingRunner pntRunner, TransferScene sc) {
        runner = pntRunner;
        scene = sc;

        runner.addMessageHandler(this);
        runner.addBufferHandler(this);

        setBorder(BorderFactory.createTitledBorder("Messages"));

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
        messageChooser = new JComboBox();
        gridbag.setConstraints(messageChooser, c);
        add(messageChooser);

        setLayout(gridbag);
    }

    public void updateMsgChooser() {
        cur_msg = (Integer) messageChooser.getSelectedItem();
        messageChooser.removeActionListener(this);
        messageChooser.removeAllItems();
        enabledBox.removeItemListener(this);
        int to_sel_index = 0;
        int c = 0;
        final boolean keep_cur_msg = (cur_msg != null && message_holders.containsKey(cur_msg));
        for (final Integer msgId : message_holders.keySet()) {
            messageChooser.addItem(msgId);
            if (keep_cur_msg && msgId.equals(cur_msg))
                to_sel_index = c;
            c++;
        }
        messageChooser.addActionListener(this);
        if (keep_cur_msg || !message_holders.isEmpty())
            messageChooser.setSelectedIndex(to_sel_index);
        enabledBox.setSelected(true);
        enabledBox.addItemListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        updateInfected();
    }

    @Override
    public void itemStateChanged(ItemEvent arg0) {
        messageChooser.setEnabled(enabledBox.isSelected());
        updateInfected();
    }

    private void updateInfected() {
        if (enabledBox.isSelected()) {
            cur_msg = (Integer) messageChooser.getSelectedItem();
            if (cur_msg != null)
                scene.setInfected(message_holders.get(cur_msg));
        }
        scene.repaint();
    }

    @Override
    public Listener<MessageEvent> messageEventListener() {
        return new Listener<MessageEvent>() {
            @Override
            public void handle(long time, Collection<MessageEvent> events) {
                for (final MessageEvent event : events) {
                    if (event.isNew())
                        message_holders.put(event.msgId(), new HashSet<Integer>());
                    else
                        message_holders.remove(event.msgId());
                    updateMsgChooser();
                }
            }
        };
    }

    @Override
    public Listener<Message> messageListener() {
        return new StatefulListener<Message>() {

            @Override
            public void reset() {
                message_holders.clear();
            }

            @Override
            public void handle(long time, Collection<Message> events) {
                for (final Message msg : events)
                    message_holders.put(msg.msgId(), new HashSet<Integer>());
                updateMsgChooser();
            }
        };
    }

    @Override
    public Listener<BufferEvent> bufferEventListener() {
        return new Listener<BufferEvent>() {
            @Override
            public void handle(long time, Collection<BufferEvent> events) throws IOException {
                for (final BufferEvent event : events) {
                    final Integer id = event.id();
                    Integer msgId;
                    Set<Integer> holders;
                    switch (event.type()) {
                        case ADD:
                            msgId = event.msgId();
                            holders = message_holders.get(msgId);
                            if (holders != null) {
                                holders.add(id);
                                if (cur_msg != null && cur_msg.equals(msgId))
                                    scene.addInfected(id);
                            }
                            break;
                        case REMOVE:
                            msgId = event.msgId();
                            holders = message_holders.get(msgId);
                            if (holders != null) {
                                holders.remove(id);
                                if (cur_msg != null && cur_msg.equals(msgId))
                                    scene.removeInfected(id);
                            }
                            break;
                    }
                }
            }
        };
    }

    @Override
    public Listener<Buffer> bufferListener() {
        return new StatefulListener<Buffer>() {
            @Override
            public void reset() {
                scene.setInfected(Collections.<Integer> emptySet());
            }

            @Override
            public void handle(long time, Collection<Buffer> events) {
                for (final Buffer buffer : events) {
                    final Integer id = buffer.id();
                    for (final Integer msgId : buffer)
                        message_holders.get(msgId).add(id);
                }
                if (cur_msg != null && message_holders.containsKey(cur_msg))
                    scene.setInfected(message_holders.get(cur_msg));
                else
                    scene.setInfected(Collections.<Integer> emptySet());
            }
        };
    }
}
