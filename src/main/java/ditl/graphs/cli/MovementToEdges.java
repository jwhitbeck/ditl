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

import ditl.cli.App;
import ditl.cli.ConvertApp;
import ditl.graphs.EdgeTrace;
import ditl.graphs.MovementToEdgesConverter;
import ditl.graphs.MovementTrace;

@App.Cli(pkg = "graphs", cmd = "movement-to-edges", alias = "m2e")
public class MovementToEdges extends ConvertApp {

    private final GraphOptions.CliParser graph_options = new GraphOptions.CliParser(GraphOptions.EDGES, GraphOptions.MOVEMENT);
    private double range;
    private Long max_interval = null;

    @Override
    protected void run() throws Exception {
        final MovementTrace movement = orig_store.getTrace(graph_options.get(GraphOptions.MOVEMENT));
        final EdgeTrace edges = dest_store.newTrace(graph_options.get(GraphOptions.EDGES), EdgeTrace.class, force);
        if (max_interval == null)
            max_interval = movement.maxTime() - movement.minTime();
        else
            max_interval *= movement.ticsPerSecond();
        new MovementToEdgesConverter(edges, movement, range, max_interval).convert();
    }

    @Override
    protected void parseArgs(CommandLine cli, String[] args) throws ParseException, HelpException {
        super.parseArgs(cli, args);
        graph_options.parse(cli);
        range = Double.parseDouble(args[1]);
        if (cli.hasOption(intervalOption))
            max_interval = Long.parseLong(cli.getOptionValue(intervalOption));
    }

    @Override
    protected String getUsageString() {
        return "[OPTIONS] STORE RANGE";
    }

    @Override
    protected void initOptions() {
        super.initOptions();
        graph_options.setOptions(options);
        options.addOption(null, intervalOption, true, "interval beyond which not to look for new meetings (useful if positions are updated every seconds)");
    }

}
