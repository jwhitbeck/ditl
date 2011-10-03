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

import org.apache.commons.cli.*;

import ditl.Store.*;
import ditl.WritableStore.AlreadyExistsException;
import ditl.cli.ConvertApp;
import ditl.graphs.*;


public class BeaconsToEdges extends ConvertApp {
	
	final static String toleranceOption = "tolerance";
	final static String expandOption = "expand";

	private GraphOptions graph_options = new GraphOptions(GraphOptions.BEACONS, GraphOptions.EDGES);
	
	long snap_interval;
	int tol;
	double expansion;
	
	public final static String PKG_NAME = "graphs";
	public final static String CMD_NAME = "beacons-to-edges";
	public final static String CMD_ALIAS = "b2e";
	
	@Override
	protected void parseArgs(CommandLine cli, String[] args) throws ParseException, ArrayIndexOutOfBoundsException, HelpException {
		super.parseArgs(cli, args);
		graph_options.parse(cli);
		tol = Integer.parseInt(cli.getOptionValue(toleranceOption,"0"));
		expansion = Double.parseDouble(cli.getOptionValue(expandOption, "0.0"));
		snap_interval = Long.parseLong(cli.getOptionValue(snapIntervalOption, "60")); // by default, snap every minute
	}

	@Override
	protected void initOptions() {
		super.initOptions();
		graph_options.setOptions(options);
		options.addOption(null, storeOutputOption, true, "Name of store to output new traces to");
		options.addOption(null, toleranceOption, true, "Missed beacon tolerance (default 0)");
		options.addOption(null, expandOption, true, "Expand contacts by this fraction (default 0.0)");
		options.addOption(null, snapIntervalOption, true, "snapshot interval in seconds (default 60)");
	}

	@Override
	protected void run() throws IOException, AlreadyExistsException, LoadTraceException, NoSuchTraceException {
		EdgeTrace edges = (EdgeTrace)dest_store.newTrace(graph_options.get(GraphOptions.EDGES), EdgeTrace.type, force);
		BeaconTrace beacons = (BeaconTrace) orig_store.getTrace(graph_options.get(GraphOptions.BEACONS));		
		snap_interval *= beacons.ticsPerSecond();
		
		new BeaconsToEdgesConverter(edges, beacons, tol, expansion, snap_interval)
			.convert();
	}
}
