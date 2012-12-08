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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

import ditl.Trace;
import ditl.cli.App;
import ditl.cli.ConvertApp;
import ditl.graphs.EdgeTrace;
import ditl.graphs.ReachabilityTrace;
import ditl.graphs.UpperReachableConverter;

@App.Cli(pkg = "graphs", cmd = "upper-reachability", alias = "ur")
public class UpperReachability extends ConvertApp {

    final static String lowerPrefixOption = "lower-prefix";
    final static String upperPrefixOption = "upper-prefix";

    private String lower_name;
    private String upper_name;

    @Override
    protected String getUsageString() {
        return "[OPTIONS] STORE TAU MAXDELAY";
    }

    @Override
    protected void parseArgs(CommandLine cli, String[] args) throws ParseException, ArrayIndexOutOfBoundsException, HelpException {
        super.parseArgs(cli, args);
        final long tau = Long.parseLong(args[1]);
        final long delay = Long.parseLong(args[2]);
        final String lower_prefix = cli.hasOption(lowerPrefixOption) ? cli.getOptionValue(lowerPrefixOption) : EdgeTrace.class.getAnnotation(Trace.Type.class).value();
        lower_name = ReachabilityTrace.defaultName(lower_prefix, tau, delay);
        final String upper_prefix = cli.hasOption(upperPrefixOption) ? cli.getOptionValue(upperPrefixOption) : lower_prefix + "_upper";
        upper_name = ReachabilityTrace.defaultName(upper_prefix, tau, delay);

    }

    @Override
    protected void initOptions() {
        super.initOptions();
        options.addOption(null, lowerPrefixOption, true, "Prefix for lower reachability traces (default: name of the 'edges' trace)");
        options.addOption(null, upperPrefixOption, true, "Prefix for upper reachability traces (default: prefix+'_upper')");
    }

    @Override
    protected void run() throws Exception {
        final ReachabilityTrace upper_trace = dest_store.newTrace(upper_name, ReachabilityTrace.class, force);
        final ReachabilityTrace lower_trace = orig_store.getTrace(lower_name);
        new UpperReachableConverter(upper_trace, lower_trace).convert();
    }
}
