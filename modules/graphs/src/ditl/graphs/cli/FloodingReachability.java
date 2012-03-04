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

import ditl.Store.LoadTraceException;
import ditl.Store.NoSuchTraceException;
import ditl.WritableStore.AlreadyExistsException;
import ditl.cli.ConvertApp;
import ditl.graphs.*;



public class FloodingReachability extends ConvertApp {
	
	private double tau;
	private long delay;
	private GraphOptions graph_options = new GraphOptions(GraphOptions.LINKS,GraphOptions.PRESENCE);
	private Long min_time;
	
	public final static String PKG_NAME = "graphs";
	public final static String CMD_NAME = "flooding-reachability";
	public final static String CMD_ALIAS = "fr";
	
	@Override
	protected String getUsageString(){
		return "[OPTIONS] STORE TAU PERIOD";
	}

	@Override
	protected void parseArgs(CommandLine cli, String[] args) throws ParseException, ArrayIndexOutOfBoundsException, HelpException {
		super.parseArgs(cli, args);
		graph_options.parse(cli);		
		tau = Double.parseDouble(args[1]);
		delay = Integer.parseInt(args[2]);
		if ( cli.hasOption(minTimeOption) )
			min_time = Long.parseLong(cli.getOptionValue(minTimeOption));
	}	

	@Override
	protected void initOptions() {
		super.initOptions();
		graph_options.setOptions(options);
		options.addOption(null, minTimeOption, true, "Start flooding at time <arg>");
	}


	@Override
	protected void run() throws IOException, AlreadyExistsException, LoadTraceException, NoSuchTraceException {
		LinkTrace links = (LinkTrace)orig_store.getTrace(graph_options.get(GraphOptions.LINKS));
		PresenceTrace presence = (PresenceTrace)orig_store.getTrace(graph_options.get(GraphOptions.PRESENCE));
		if ( min_time == null )
			min_time = presence.minTime();
		else
			min_time *= links.ticsPerSecond();
		long _tau = (long)(tau * links.ticsPerSecond());
		delay *= links.ticsPerSecond();
		String name = links.name()+"_t"+_tau+"_pd"+delay;
		ReachabilityTrace reachability = (ReachabilityTrace)dest_store.newTrace(name, ReachabilityTrace.type, force);
		new FloodingReachableConverter(reachability, presence, links, _tau, delay, min_time).convert();
		
	}
}
