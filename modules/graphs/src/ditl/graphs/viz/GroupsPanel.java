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

import java.util.*;

import java.awt.*;
import javax.swing.*;

@SuppressWarnings("serial")
public class GroupsPanel extends JPanel {
	
	protected JPanel colorPanel = null ;
	protected int rectSize = 10;
	
	public GroupsPanel(){
		setBorder(BorderFactory.createTitledBorder("Groups"));
		setVisible(false);
	}
	
	public void load(Groups groups) {
		if ( groups != null ){
			if ( colorPanel != null )
				remove(colorPanel);
			colorPanel = new JPanel();
			GridBagLayout gridbag = new GridBagLayout();
			GridBagConstraints c = new GridBagConstraints();
			colorPanel.setLayout(gridbag);
			c.gridy = 0;
			for ( Map.Entry<String, Color> e : groups.colorMap().entrySet() ){
				c.gridx = 0;
				c.anchor = GridBagConstraints.EAST;
				JPanel colorRect = createColorRect(e.getValue());
				gridbag.setConstraints(colorRect, c);
				colorPanel.add(colorRect);
				
				c.gridx = 1;
				c.anchor = GridBagConstraints.WEST;
				JLabel groupLabel = new JLabel(" "+e.getKey());
				gridbag.setConstraints(groupLabel, c);
				colorPanel.add(groupLabel);
				
				c.gridy++;
			}
			
			add(colorPanel);
			setVisible(true);
		} else {
			setVisible(false);
		}
	}
	
	private JPanel createColorRect(Color color){
		JPanel rect = new JPanel();
		rect.setPreferredSize(new Dimension(rectSize,rectSize));
		rect.setBackground(color);
		return rect;
	}
}
