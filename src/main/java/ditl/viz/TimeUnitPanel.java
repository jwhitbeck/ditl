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
package ditl.viz;

import java.awt.event.*;

import javax.swing.*;

import ditl.Trace;

@SuppressWarnings("serial")
public class TimeUnitPanel extends JPanel {
	
	final static int SECONDS = 0;
	final static int MINUTES = 1;
	final static int HOURS = 2;
	final static int DAYS = 3;
	
	final static String[] long_units = { "seconds", "minutes", "hours", "days" };
	final static String[] short_units = { "s", "m", "h", "d" };
	final static int[] modifiers = { 1, 60, 3600, 24*3600 };
	final static int[] thresholds = {300, 3*3600, 72*3600, Integer.MAX_VALUE }; 
	
	protected JComboBox unit_list;
	protected int cur_unit = SECONDS;
	protected ControlsPanel _controls;
	
	public TimeUnitPanel(ControlsPanel controls){
		_controls = controls;
		unit_list = new JComboBox();
		for ( int i=0; i<4; ++i){
			unit_list.addItem(long_units[i]);
		}
		unit_list.setSelectedIndex(SECONDS);
		unit_list.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				cur_unit = unit_list.getSelectedIndex();
				_controls.setModifier(modifiers[cur_unit], short_units[cur_unit]);
			}
		});
		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		add(Box.createHorizontalGlue());
		add(new JLabel("Unit: "));
		add(unit_list);
	}
	
	public void setPreferredTimeUnit(Trace<?> trace){
		long d = (trace.maxTime() - trace.minTime())/trace.ticsPerSecond();
		int i = 0;
		while ( i < 4){
			if ( d < thresholds[i] )
				break;
			i++;
		}
		cur_unit = i;
		unit_list.setSelectedIndex(i);
	}

}
