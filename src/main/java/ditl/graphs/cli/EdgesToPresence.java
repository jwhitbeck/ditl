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
import ditl.graphs.EdgeTrace;
import ditl.graphs.EdgesToPresenceConverter;
import ditl.graphs.PresenceTrace;
import ditl.graphs.StrictEdgesToPresenceConverter;

@Command(pkg = "graphs", cmd = "edges-to-presence", alias = "e2p")
public class EdgesToPresence extends ConvertApp {

    final static String strictOption = "strict";
    private boolean strict;

    private final GraphOptions.CliParser graph_options = new GraphOptions.CliParser(GraphOptions.EDGES, GraphOptions.PRESENCE);

    @Override
    protected void initOptions() {
        super.initOptions();
        graph_options.setOptions(options);
        options.addOption(null, strictOption, false, "Nodes enter with their first contact and leave after their last.");
    }

    @Override
    protected void parseArgs(CommandLine cli, String[] args)
            throws ParseException, ArrayIndexOutOfBoundsException,
            HelpException {
        super.parseArgs(cli, args);
        graph_options.parse(cli);
        strict = cli.hasOption(strictOption);
    }

    @Override
    protected void run() throws IOException, NoSuchTraceException, AlreadyExistsException, LoadTraceException {
        final EdgeTrace edges = (EdgeTrace) orig_store.getTrace(graph_options.get(GraphOptions.EDGES));
        final PresenceTrace presence = (PresenceTrace) dest_store.newTrace(graph_options.get(GraphOptions.PRESENCE), PresenceTrace.class, force);
        if (strict)
            new StrictEdgesToPresenceConverter(presence, edges).convert();
        else
            new EdgesToPresenceConverter(presence, edges).convert();
    }
}
