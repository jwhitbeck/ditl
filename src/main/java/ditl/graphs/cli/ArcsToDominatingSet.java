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
import ditl.graphs.ArcsToDominatingSetConverter;
import ditl.graphs.GroupTrace;
import ditl.graphs.PresenceTrace;

@Command(pkg = "graphs", cmd = "arcs-to-dominating-set", alias = "a2ds")
public class ArcsToDominatingSet extends ConvertApp {

    private final GraphOptions.CliParser graph_options = new GraphOptions.CliParser(GraphOptions.GROUPS, GraphOptions.ARCS, GraphOptions.PRESENCE);

    @Override
    protected void run() throws Exception {
        final PresenceTrace presence = orig_store.getTrace(graph_options.get(GraphOptions.PRESENCE));
        final ArcTrace arcs = orig_store.getTrace(graph_options.get(GraphOptions.ARCS));
        final GroupTrace ds = dest_store.newTrace(graph_options.get(GraphOptions.GROUPS), GroupTrace.class, force);
        new ArcsToDominatingSetConverter(ds, arcs, presence).convert();
    }

    @Override
    protected void parseArgs(CommandLine cli, String[] args) throws ParseException, HelpException {
        super.parseArgs(cli, args);
        graph_options.parse(cli);
    }

    @Override
    protected void initOptions() {
        super.initOptions();
        graph_options.setOptions(options);
    }

}
