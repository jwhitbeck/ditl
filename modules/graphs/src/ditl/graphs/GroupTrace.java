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
package ditl.graphs;

import java.io.IOException;
import java.util.*;

import ditl.*;

public class GroupTrace extends WrappedTrace {

	final public static String labelsKey = "labels";
	final public static String delim = ",";
	
	private Map<Integer,String> labels = new HashMap<Integer,String>();
	
	public GroupTrace(Trace trace) {
		super(trace);
		String labelsString = trace.getValue(labelsKey);
		if ( labelsString != null ){
			int id = 0;
			for ( String label : labelsString.split(delim) ){
				labels.put(id, label);
				id++;
			}
		}
	}
	
	public String getLabel(Integer id){
		return labels.get(id);
	}
	
	public boolean hasLabels(){
		return ! labels.isEmpty();
	}
	
	public static Set<Group> staticGroups(StatefulReader<GroupEvent,Group> groupReader) throws IOException{
		return staticGroups(groupReader, groupReader.trace().minTime());
	}
	
	public static Set<Group> staticGroups(StatefulReader<GroupEvent,Group> groupReader, long time) throws IOException{
		groupReader.seek(time);
		return groupReader.referenceState();
	}
}
