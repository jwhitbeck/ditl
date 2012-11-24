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

public class EdgesToLinks extends ConvertApp {

	private final static String intersectOption = "intersect";
	
	private GraphOptions graph_options = new GraphOptions(GraphOptions.LINKS, GraphOptions.EDGES);
	private boolean union;
	
	public final static String PKG_NAME = "graphs";
	public final static String CMD_NAME = "edges-to-links";
	public final static String CMD_ALIAS = "e2l";
	
	@Override
	protected void initOptions() {
		super.initOptions();
		graph_options.setOptions(options);
		options.addOption(null, storeOutputOption, true, "write new traces to this store");
		options.addOption(null, intersectOption, false, "intersect edges (default: union)");
	}

	@Override
	protected void parseArgs(CommandLine cli, String[] args)
			throws ParseException, ArrayIndexOutOfBoundsException,
			HelpException {
		super.parseArgs(cli, args);
		graph_options.parse(cli);
		union = cli.hasOption(intersectOption);
	}

	@Override
	protected void run() throws IOException, NoSuchTraceException, AlreadyExistsException, LoadTraceException {
		EdgeTrace edges = (EdgeTrace) orig_store.getTrace(graph_options.get(GraphOptions.EDGES));
		LinkTrace links = (LinkTrace) dest_store.newTrace(graph_options.get(GraphOptions.LINKS), LinkTrace.type, force);
		new EdgesToLinksConverter(links, edges, union).convert();
	}
}
