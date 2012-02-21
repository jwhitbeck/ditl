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


public class Resample extends ConvertApp {
	
	final static String missProbabilityOption = "miss-probability";

	private GraphOptions graph_options = new GraphOptions(GraphOptions.BEACONS, GraphOptions.PRESENCE, GraphOptions.LINKS);
	long period;
	double p;
	
	public final static String PKG_NAME = "graphs";
	public final static String CMD_NAME = "resample";
	public final static String CMD_ALIAS = "s";

	
	@Override
	protected void parseArgs(CommandLine cli, String[] args) throws ParseException, ArrayIndexOutOfBoundsException, HelpException {
		super.parseArgs(cli, args);
		graph_options.parse(cli);
		period = Long.parseLong(args[1]);
		p = Double.parseDouble(cli.getOptionValue(missProbabilityOption,"0.0"));	
	}

	@Override
	protected void initOptions() {
		super.initOptions();
		options.addOption(null, missProbabilityOption, true, "Missed beacon probability (default 0.0)");
	}

	@Override
	protected void run() throws IOException, NoSuchTraceException, AlreadyExistsException, LoadTraceException {
		LinkTrace links = (LinkTrace) orig_store.getTrace(graph_options.get(GraphOptions.LINKS));
		PresenceTrace presence = (PresenceTrace) orig_store.getTrace(graph_options.get(GraphOptions.PRESENCE));
		BeaconTrace beacons = (BeaconTrace) dest_store.newTrace(graph_options.get(GraphOptions.BEACONS), BeaconTrace.type, force);
		period *= links.ticsPerSecond();
		new BeaconningConverter(beacons, presence, links, period, p, true).convert();
	}

	@Override
	protected String getUsageString() {
		return "[OPTIONS] STORE BEACONNING_PERIOD";
	}

}
