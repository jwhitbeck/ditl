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
	
	enum Unit {
		
		SECONDS ( "seconds", "s", 1, 300),
		MINUTES ( "minutes", "m", 60, 3*3600),
		HOURS ( "hours", "h", 3600, 72*3600),
		DAYS ( "days", "d", 24*3600, Integer.MAX_VALUE);
		
		private final String _long, _short;
		private final int _mod, _thresh;
		
		private Unit(String longUnit, String shortUnit, int mod, int thresh){
			_long = longUnit; _short = shortUnit;
			_mod = mod; _thresh = thresh;
		}
	}
	
	protected JComboBox unit_list;
	protected Unit cur_unit = Unit.SECONDS;
	protected ControlsPanel _controls;
	
	public TimeUnitPanel(ControlsPanel controls){
		_controls = controls;
		unit_list = new JComboBox();
		for ( Unit u : Unit.values() ){
			unit_list.addItem(u._long);
		}
		unit_list.setSelectedIndex(Unit.SECONDS.ordinal());
		unit_list.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				cur_unit = Unit.values()[unit_list.getSelectedIndex()];
				_controls.setModifier(cur_unit._mod, cur_unit._short);
			}
		});
		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		add(Box.createHorizontalGlue());
		add(new JLabel("Unit: "));
		add(unit_list);
	}
	
	public void setPreferredTimeUnit(Trace<?> trace){
		long d = (trace.maxTime() - trace.minTime())/trace.ticsPerSecond();
		for ( Unit u : Unit.values() ){
			if ( d < u._thresh ){
				cur_unit = u;
				break;
			}
		}
		unit_list.setSelectedIndex(cur_unit.ordinal());
	}

}
