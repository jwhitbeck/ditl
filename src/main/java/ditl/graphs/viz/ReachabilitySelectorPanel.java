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

import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.util.*;

import javax.swing.*;

import ditl.*;
import ditl.graphs.ReachabilityTrace;
import ditl.viz.Scene;



@SuppressWarnings("serial")
public class ReachabilitySelectorPanel extends JPanel 
	implements ActionListener, ItemListener {
	
	final static int[] thresholds = {300, 3*3600, 72*3600, Integer.MAX_VALUE };
	final static int[] modifiers = { 1, 60, 3600, 24*3600 };
	final static String[] short_units = { "s", "m", "h", "d" };
	
	protected Store _store;
	protected JCheckBox enabledBox;
	protected JComboBox tauChooser;
	protected JComboBox delayChooser;
	protected SortedMap<Long,Item> taus = new TreeMap<Long,Item>();
	protected ArcRunner runner;
	protected Scene scene;
	protected ReachabilityTrace cur_trace = null;
	protected DecimalFormat df = new DecimalFormat("#.##");
	
	public ReachabilitySelectorPanel(ArcRunner traceRunner, Scene sc){
		runner = traceRunner;
		scene = sc;
		setBorder(BorderFactory.createTitledBorder("Reachability"));
		
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		
		c.gridy=0; c.gridx = 0;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.EAST;
		JLabel enabledLabel = new JLabel("Enabled");
		gridbag.setConstraints(enabledLabel, c);
		add(enabledLabel);
		
		c.gridx = 2;
		c.gridwidth = 1;
		enabledBox = new JCheckBox();
		enabledBox.setSelected(false);
		gridbag.setConstraints(enabledBox, c);
		add(enabledBox);
		
		c.gridy=1; c.gridx=0;
		JLabel tauLabel = new JLabel("Tau: ");
		gridbag.setConstraints(tauLabel, c);
		add(tauLabel);
		
		c.gridx = 1;
		c.gridwidth = 2;
		tauChooser = new JComboBox();
		gridbag.setConstraints(tauChooser, c);
		add(tauChooser);
		
		c.gridy=2; c.gridx=0;
		c.gridwidth = 1;
		JLabel delayLabel = new JLabel("Delay: ");
		gridbag.setConstraints(delayLabel, c);
		add(delayLabel);
		
		c.gridx=1;
		c.gridwidth = 2;
		delayChooser = new JComboBox();
		gridbag.setConstraints(delayChooser, c);
		add(delayChooser);
		
		setLayout(gridbag);
		setVisible(false);
	}
	
	public void setStore(Store store){
		_store = store;
	}
	
	public void load(Collection<Trace<?>> traces) {
		taus.clear();
		tauChooser.removeActionListener(this);
		enabledBox.removeItemListener(this);
		tauChooser.removeAllItems();
		if ( ! traces.isEmpty()){
			for ( Trace<?> trace : traces ){
				ReachabilityTrace r_trace = (ReachabilityTrace)trace;
				long tau = r_trace.tau();
				long tps = r_trace.ticsPerSecond();
				if ( ! taus.containsKey(tau) )
					taus.put(tau, new Item(tau,tps));
				Item i = new Item(r_trace.delay(), tps);
				i.trace = r_trace;
				taus.get(tau).addChild(i);
			}
			for ( Item taui : taus.values() )
				tauChooser.addItem(taui);
			enabledBox.setSelected(true);
			tauChooser.addActionListener(this);
			tauChooser.setSelectedIndex(0);
			enabledBox.addItemListener(this);
			setVisible(true);
		} else {
			delayChooser.removeActionListener(this);
			delayChooser.removeAllItems();
			setVisible(false);
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if ( e.getSource() == tauChooser ){
			delayChooser.removeActionListener(this);
			delayChooser.removeAllItems();
			/*Long tau = (Long)tauChooser.getSelectedItem();
			for ( Long delay : traceMap.get(tau).keySet() )
				delayChooser.addItem(delay);*/
			Item taui = (Item)tauChooser.getSelectedItem();
			for ( Item di : taui.children )
				delayChooser.addItem(di);
			delayChooser.addActionListener(this);
			delayChooser.setSelectedIndex(0);
		} else {
			updateReachabilityTrace();
		}
	}
	
	@Override
	public void itemStateChanged(ItemEvent arg0) {
		if ( enabledBox.isSelected() ){
			tauChooser.setEnabled(true);
			delayChooser.setEnabled(true);
			
		} else {
			tauChooser.setEnabled(false);
			delayChooser.setEnabled(false);
		}
		updateReachabilityTrace();
	}
	
	private void updateReachabilityTrace(){
		String name = "null";
		try {
			cur_trace = null;
			if ( enabledBox.isSelected() ){
				cur_trace = ((Item)delayChooser.getSelectedItem()).trace;
			}
			runner.setArcTrace(cur_trace);
		} catch (Exception ioe){
			JOptionPane.showMessageDialog(this, "Failed to load reachability file '"+name+"'", "Warning", JOptionPane.ERROR_MESSAGE);
		}
		scene.repaint();
	}
	
	private class Item implements Comparable<Item> {
		SortedSet<Item> children;
		ReachabilityTrace trace = null;
		long _n;
		long _tps;
		Item(long n, long tps){ _n = n; _tps = tps; }
		@Override
		public String toString(){
			long d = _n / _tps;
			int i = 0;
			while ( i < 4){
				if ( d < thresholds[i] )
					break;
				i++;
			}
			int mod = modifiers[i];
			return df.format(((double)d / (double)mod))+" "+short_units[i];
		}
		@Override
		public int compareTo(Item o) {
			if ( _n < o._n ) return -1;
			if ( _n > o._n ) return 1;
			return 0;
		}
		void addChild(Item i){
			if ( children == null )
				children = new TreeSet<Item>();
			children.add(i);
		}
	}
}
