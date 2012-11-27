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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

import ditl.Store.LoadTraceException;
import ditl.Store.NoSuchTraceException;
import ditl.WritableStore.AlreadyExistsException;
import ditl.cli.Command;
import ditl.cli.ConvertApp;
import ditl.graphs.ArcTrace;
import ditl.graphs.ArcsToEdgesConverter;
import ditl.graphs.EdgeTrace;

@Command(pkg = "graphs", cmd = "arcs-to-edges", alias = "a2e")
public class ArcsToEdges extends ConvertApp {

    private final static String intersectOption = "intersect";

    private final GraphOptions.CliParser graph_options = new GraphOptions.CliParser(GraphOptions.EDGES, GraphOptions.ARCS);
    private boolean union;

    @Override
    protected void initOptions() {
        super.initOptions();
        graph_options.setOptions(options);
        options.addOption(null, storeOutputOption, true, "write new traces to this store");
        options.addOption(null, intersectOption, false, "intersect arcs (default: union)");
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
        final ArcTrace arcs = (ArcTrace) orig_store.getTrace(graph_options.get(GraphOptions.ARCS));
        final EdgeTrace edges = (EdgeTrace) dest_store.newTrace(graph_options.get(GraphOptions.EDGES), EdgeTrace.class, force);
        new ArcsToEdgesConverter(edges, arcs, union).convert();
    }
}
