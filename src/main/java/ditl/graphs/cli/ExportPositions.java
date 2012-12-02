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

import ditl.StatefulReader;
import ditl.cli.App;
import ditl.cli.ExportApp;
import ditl.graphs.Movement;
import ditl.graphs.MovementEvent;
import ditl.graphs.MovementTrace;

@App.Cli(pkg = "graphs", cmd = "export-positions", alias = "xp")
public class ExportPositions extends ExportApp {

    private final GraphOptions.CliParser graph_options = new GraphOptions.CliParser(GraphOptions.MOVEMENT);
    private double time;

    @Override
    protected void parseArgs(CommandLine cli, String[] args) throws ParseException, HelpException {
        super.parseArgs(cli, args);
        graph_options.parse(cli);
        time = Double.parseDouble(args[1]);
    }

    @Override
    protected void initOptions() {
        super.initOptions();
        graph_options.setOptions(options);
    }

    @Override
    protected void run() throws IOException {
        final MovementTrace movement = _store.getTrace(graph_options.get(GraphOptions.MOVEMENT));
        final long u_time = (long) (time * movement.ticsPerSecond());
        final StatefulReader<MovementEvent, Movement> reader = movement.getReader();
        reader.seek(u_time);
        for (final Movement m : reader.referenceState())
            System.out.println(m.id() + " " + m.positionAtTime(u_time));
        reader.close();
    }

    @Override
    protected String getUsageString() {
        return "[OPTIONS] STORE TIME";
    }
}
