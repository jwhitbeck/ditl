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

import java.io.IOException;
import java.util.*;

import org.apache.commons.cli.*;

import ditl.*;
import ditl.Store.*;
import ditl.WritableStore.AlreadyExistsException;
import ditl.cli.WriteApp;
import ditl.graphs.*;


public class ImportStaticGroups extends WriteApp {

	private static String labelsOption = "labels";
	
	private GraphOptions graph_options = new GraphOptions(GraphOptions.PRESENCE, GraphOptions.GROUPS);
	private String[] group_specs;
	String[] labels;
	
	public final static String PKG_NAME = "graphs";
	public final static String CMD_NAME = "import-groups";
	public final static String CMD_ALIAS = "ig";
	
	@Override
	protected String getUsageString(){
		return"[OPTIONS] STORE GROUP [GROUP..]";
	}

	@Override
	protected void parseArgs(CommandLine cli, String[] args) throws ArrayIndexOutOfBoundsException, ParseException, HelpException {
		super.parseArgs(cli, args);
		graph_options.parse(cli);
		group_specs = Arrays.copyOfRange(args,1,args.length);
		if ( cli.hasOption(labelsOption) )
			labels = cli.getOptionValue(labelsOption).split(",");
	}

	@Override
	protected void initOptions() {
		super.initOptions();
		graph_options.setOptions(options);
		options.addOption(null, labelsOption, true, "comma-separated list of groups labels");
	}
	
	@Override
	public void run() throws IOException, NoSuchTraceException, AlreadyExistsException, LoadTraceException {
		Trace<?> presence = _store.getTrace(graph_options.get(GraphOptions.PRESENCE));
		GroupTrace groups = (GroupTrace) _store.newTrace(graph_options.get(GraphOptions.GROUPS), GroupTrace.type, force);
		StatefulWriter<GroupEvent,Group> groupWriter = groups.getWriter(presence.snapshotInterval()); 
		Set<Group> initState = new HashSet<Group>();
		int i = 0;
		for ( String g_spec : group_specs ){
			Set<Integer> members = GroupSpecification.parse(g_spec);
			initState.add(new Group(i, members));
			i++;
		}
		
		groupWriter.setInitState(presence.minTime(), initState);
		groupWriter.setProperty(Trace.maxTimeKey, presence.maxTime());
		groupWriter.setProperty(Trace.ticsPerSecondKey, presence.ticsPerSecond());
		
		if ( labels != null ){
			StringBuffer buffer = new StringBuffer();
			for ( int j=0; j<labels.length; ++j){
				buffer.append(labels[j]);
				if ( j<labels.length-1)
					buffer.append(",");
			}
			groupWriter.setProperty(GroupTrace.labelsKey, buffer.toString());
		}
		
		groupWriter.close();		
	}
}
