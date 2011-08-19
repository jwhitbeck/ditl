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
package ditl.graphs.cli;

import java.io.*;
import java.util.*;

import org.apache.commons.cli.*;

import ditl.*;
import ditl.graphs.*;


public class ImportStaticGroups extends GraphApp {

	private static String labelsOption = "labels";
	
	private boolean useLabels;
	private String presenceName;
	private String groupsName;
	private File storeFile;
	private String[] group_specs;
	
	public ImportStaticGroups(String[] args) {
		super(args);
	}
	
	@Override
	protected void setUsageString(){
		usageString = "Usage: [OPTIONS] STORE GROUP [GROUP..]";
	}

	@Override
	protected void parseArgs(CommandLine cli, String[] args) throws ArrayIndexOutOfBoundsException, ParseException, HelpException {
		storeFile = new File(args[0]);
		group_specs = Arrays.copyOfRange(args,1,args.length);
		useLabels = cli.hasOption(labelsOption);
		presenceName = cli.getOptionValue(presenceOption, GraphStore.defaultPresenceName);
		groupsName = cli.getOptionValue(groupsOption, GraphStore.defaultGroupsName);
	}

	@Override
	protected void initOptions() {
		options.addOption(null, labelsOption, false, "consider first element of group specification as the group's label");
		options.addOption(null, presenceOption, true, "presence trace to use for minTime and maxTime (default: "+GraphStore.defaultPresenceName+")");
		options.addOption(null, groupsOption, true, "name of imported groups trace (default: "+GraphStore.defaultGroupsName+")");
	}
	
	@Override
	public void run() throws IOException, MissingTraceException {
		WritableStore store = WritableStore.open(storeFile);
		
		Trace presence = getTrace(store,presenceName);
		
		StatefulWriter<GroupEvent,Group> groupWriter = new GraphStore(store).getGroupWriter(groupsName, presence.snapshotInterval());
		Set<Group> initState = new HashSet<Group>();
		List<String> labels = new LinkedList<String>();
		int i=0;
		for ( String spec : group_specs ){
			Group group = new Group(i);
			String[] ranges = spec.split(":");
			int j = 0;
			if ( useLabels ){
				labels.add(ranges[j]);
				++j;
			}
			while ( j < ranges.length ){
				String[] bounds = ranges[j].split("-");
				Integer n;
				if ( bounds.length == 1 ) {
					n = Integer.parseInt(bounds[0]);
					group.handleEvent(new GroupEvent(group.gid(), GroupEvent.JOIN, new Integer[]{n}));
				} else {
					for ( n = Integer.parseInt(bounds[0]); n<=Integer.parseInt(bounds[1]); ++n){
						group.handleEvent(new GroupEvent(group.gid(), GroupEvent.JOIN, new Integer[]{n}));
					}
				}
				++j;
			}
			initState.add(group);
			++i;
		}
		
		groupWriter.setInitState(presence.minTime(), initState);
		groupWriter.setProperty(Trace.maxTimeKey, presence.maxTime());
		groupWriter.setProperty(Trace.ticsPerSecondKey, presence.ticsPerSecond());
		
		if ( useLabels ){
			StringBuffer buffer = new StringBuffer();
			Iterator<String> si = labels.iterator();
			while ( si.hasNext() ){
				buffer.append(si.next());
				if ( si.hasNext() )
					buffer.append(",");
			}
			groupWriter.setProperty(GroupTrace.labelsKey, buffer.toString());
		}
		
		groupWriter.close();
		
		store.close();		
	}
}
