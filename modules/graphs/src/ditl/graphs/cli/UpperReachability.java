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



public class UpperReachability extends ConvertApp {
	
	final static String lowerPrefixOption = "lower-prefix";
	final static String upperPrefixOption = "upper-prefix";
	
	private String lower_name;
	private String upper_name;
	
	public final static String PKG_NAME = "graphs";
	public final static String CMD_NAME = "upper-reachability";
	public final static String CMD_ALIAS = "ur";
	
	@Override
	protected String getUsageString(){
		return "[OPTIONS] STORE TAU MAXDELAY";
	}

	@Override
	protected void parseArgs(CommandLine cli, String[] args) throws ParseException, ArrayIndexOutOfBoundsException, HelpException {
		super.parseArgs(cli, args);
		long tau = Long.parseLong(args[1]);
		long delay = Long.parseLong(args[2]);
		String lower_prefix = cli.hasOption(lowerPrefixOption)? cli.getOptionValue(lowerPrefixOption) : LinkTrace.defaultName;
		lower_name = ReachabilityTrace.defaultName(lower_prefix, tau, delay);
		String upper_prefix = cli.hasOption(upperPrefixOption)? cli.getOptionValue(upperPrefixOption) : lower_prefix+"_upper";
		upper_name = ReachabilityTrace.defaultName(upper_prefix, tau, delay);
		
	}
	
	@Override
	protected void initOptions() {
		super.initOptions();
		options.addOption(null, lowerPrefixOption, true, "Prefix for lower reachability traces (default: name of the 'links' trace)");
		options.addOption(null, upperPrefixOption, true, "Prefix for upper reachability traces (default: prefix+'_upper')");
	}


	@Override
	protected void run() throws IOException, AlreadyExistsException, LoadTraceException, NoSuchTraceException {
		ReachabilityTrace upper_trace = (ReachabilityTrace)dest_store.newTrace(upper_name, ReachabilityTrace.type, force);
		ReachabilityTrace lower_trace = (ReachabilityTrace)orig_store.getTrace(lower_name);
		new UpperReachableConverter(upper_trace, lower_trace).convert();
	}
}
