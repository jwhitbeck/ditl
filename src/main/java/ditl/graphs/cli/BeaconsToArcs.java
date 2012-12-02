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
import ditl.graphs.ArcTrace;
import ditl.graphs.BeaconTrace;
import ditl.graphs.BeaconsToArcsConverter;

@Command(pkg = "graphs", cmd = "beacons-to-arcs", alias = "b2a")
public class BeaconsToArcs extends ConvertApp {

    final static String toleranceOption = "tolerance";
    final static String expandOption = "expand";

    private final GraphOptions.CliParser graph_options = new GraphOptions.CliParser(GraphOptions.BEACONS, GraphOptions.ARCS);

    int tol;
    double expansion;

    @Override
    protected void parseArgs(CommandLine cli, String[] args) throws ParseException, ArrayIndexOutOfBoundsException, HelpException {
        super.parseArgs(cli, args);
        graph_options.parse(cli);
        tol = Integer.parseInt(cli.getOptionValue(toleranceOption, "0"));
        expansion = Double.parseDouble(cli.getOptionValue(expandOption, "0.0"));
    }

    @Override
    protected void initOptions() {
        super.initOptions();
        graph_options.setOptions(options);
        options.addOption(null, storeOutputOption, true, "Name of store to output new traces to");
        options.addOption(null, toleranceOption, true, "Missed beacon tolerance (default 0)");
        options.addOption(null, expandOption, true, "Expand contacts by this fraction (default 0.0)");
    }

    @Override
    protected void run() throws Exception {
        final ArcTrace arcs = dest_store.newTrace(graph_options.get(GraphOptions.ARCS), ArcTrace.class, force);
        final BeaconTrace beacons = orig_store.getTrace(graph_options.get(GraphOptions.BEACONS));

        new BeaconsToArcsConverter(arcs, beacons, tol, expansion).convert();
    }
}
