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
package ditl.graphs.cli;

import java.io.IOException;

import org.apache.commons.cli.*;

import ditl.Store.*;
import ditl.WritableStore.AlreadyExistsException;
import ditl.cli.ConvertApp;
import ditl.graphs.*;

public class ArcsToDominatingSet extends ConvertApp {
	
	private GraphOptions graph_options = new GraphOptions(GraphOptions.GROUPS, GraphOptions.ARCS, GraphOptions.PRESENCE);
	
	public final static String PKG_NAME = "graphs";
	public final static String CMD_NAME = "arcs-to-dominating-set";
	public final static String CMD_ALIAS = "a2ds";


	@Override
	protected void run() throws IOException, NoSuchTraceException, AlreadyExistsException, LoadTraceException {
		PresenceTrace presence = (PresenceTrace) orig_store.getTrace(graph_options.get(GraphOptions.PRESENCE));
		ArcTrace arcs = (ArcTrace) orig_store.getTrace(graph_options.get(GraphOptions.ARCS));
		GroupTrace ds = (GroupTrace) dest_store.newTrace(graph_options.get(GraphOptions.GROUPS), GroupTrace.type, force);
		new ArcsToDominatingSetConverter(ds, arcs, presence).convert();
	}
	
	@Override
	protected void parseArgs(CommandLine cli, String[] args) throws ParseException, HelpException {
		super.parseArgs(cli, args);
		graph_options.parse(cli);
	}

	@Override
	protected void initOptions(){
		super.initOptions();
		graph_options.setOptions(options);
	}

}
