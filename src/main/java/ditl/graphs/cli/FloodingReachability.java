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

import ditl.cli.Command;
import ditl.cli.ConvertApp;
import ditl.graphs.EdgeTrace;
import ditl.graphs.FloodingReachableConverter;
import ditl.graphs.PresenceTrace;
import ditl.graphs.ReachabilityTrace;

@Command(pkg = "graphs", cmd = "flooding-reachability", alias = "fr")
public class FloodingReachability extends ConvertApp {

    private double tau;
    private long delay;
    private final GraphOptions.CliParser graph_options = new GraphOptions.CliParser(GraphOptions.EDGES, GraphOptions.PRESENCE);
    private Long min_time;

    @Override
    protected String getUsageString() {
        return "[OPTIONS] STORE TAU PERIOD";
    }

    @Override
    protected void parseArgs(CommandLine cli, String[] args) throws ParseException, ArrayIndexOutOfBoundsException, HelpException {
        super.parseArgs(cli, args);
        graph_options.parse(cli);
        tau = Double.parseDouble(args[1]);
        delay = Integer.parseInt(args[2]);
        if (cli.hasOption(minTimeOption))
            min_time = Long.parseLong(cli.getOptionValue(minTimeOption));
    }

    @Override
    protected void initOptions() {
        super.initOptions();
        graph_options.setOptions(options);
        options.addOption(null, minTimeOption, true, "Start flooding at time <arg>");
    }

    @Override
    protected void run() throws Exception {
        final EdgeTrace edges = orig_store.getTrace(graph_options.get(GraphOptions.EDGES));
        final PresenceTrace presence = orig_store.getTrace(graph_options.get(GraphOptions.PRESENCE));
        if (min_time == null)
            min_time = presence.minTime();
        else
            min_time *= edges.ticsPerSecond();
        final long _tau = (long) (tau * edges.ticsPerSecond());
        delay *= edges.ticsPerSecond();
        final String name = edges.name() + "_t" + _tau + "_pd" + delay;
        final ReachabilityTrace reachability = dest_store.newTrace(name, ReachabilityTrace.class, force);
        new FloodingReachableConverter(reachability, presence, edges, _tau, delay, min_time).convert();

    }
}
