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
import ditl.cli.Command;
import ditl.cli.ConvertApp;
import ditl.graphs.*;

@Command(pkg="graphs", cmd="resample", alias="s")
public class Resample extends ConvertApp {
	
	final static String missProbabilityOption = "miss-probability";
	final static String randomizeOption = "randomize";

	private GraphOptions graph_options = new GraphOptions(GraphOptions.BEACONS, GraphOptions.PRESENCE, GraphOptions.EDGES);
	private long period;
	private double p;
	private boolean randomize;
	
	@Override
	protected void parseArgs(CommandLine cli, String[] args) throws ParseException, ArrayIndexOutOfBoundsException, HelpException {
		super.parseArgs(cli, args);
		graph_options.parse(cli);
		period = Long.parseLong(args[1]);
		p = Double.parseDouble(cli.getOptionValue(missProbabilityOption,"0.0"));
		randomize = cli.hasOption(randomizeOption);
	}

	@Override
	protected void initOptions() {
		super.initOptions();
		options.addOption(null, missProbabilityOption, true, "Missed beacon probability (default 0.0)");
		options.addOption(null, randomizeOption, false, "Randomize beaconning starting times");
	}

	@Override
	protected void run() throws IOException, NoSuchTraceException, AlreadyExistsException, LoadTraceException {
		EdgeTrace edges = (EdgeTrace) orig_store.getTrace(graph_options.get(GraphOptions.EDGES));
		PresenceTrace presence = (PresenceTrace) orig_store.getTrace(graph_options.get(GraphOptions.PRESENCE));
		BeaconTrace beacons = (BeaconTrace) dest_store.newTrace(graph_options.get(GraphOptions.BEACONS), BeaconTrace.type, force);
		period *= edges.ticsPerSecond();
		new BeaconningConverter(beacons, presence, edges, period, p, randomize).convert();
	}

	@Override
	protected String getUsageString() {
		return "[OPTIONS] STORE BEACONNING_PERIOD";
	}

}
