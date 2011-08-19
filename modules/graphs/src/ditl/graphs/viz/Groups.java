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


import java.awt.Color;
import java.util.*;

import ditl.graphs.*;


public class Groups {

	private static Color[] colorList = {Color.BLUE, Color.RED, Color.GREEN, 
										Color.MAGENTA, Color.ORANGE, Color.DARK_GRAY,
										Color.YELLOW};
	
	private static Color noGroupColor = Color.BLACK; 
		
	private Map<String,Color> colorMap = new LinkedHashMap<String,Color>();
	private Map<Integer,Color> colors = new HashMap<Integer,Color>();
	
	public Groups(Set<Group> groups, GroupTrace trace){
		int nextColor = 0;
		for ( Group g : groups ){
			Color groupColor = colorList[ (nextColor++ % colorList.length) ];
			String label = (trace.hasLabels())? trace.getLabel(g.gid()) : g.gid().toString();
			colorMap.put(label, groupColor);
			for ( Integer i : g.members() )
				colors.put(i,groupColor);
		}
	}
	
	public Color getColor(int id){
		if ( colors.containsKey(id) )
			return colors.get(id);
		return noGroupColor;
	}
	
	public Map<String,Color> colorMap(){
		return colorMap;
	}
}
