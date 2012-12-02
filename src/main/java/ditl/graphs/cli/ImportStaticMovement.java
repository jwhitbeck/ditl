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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

import ditl.IdMap;
import ditl.StatefulWriter;
import ditl.Trace;
import ditl.cli.Command;
import ditl.cli.WriteApp;
import ditl.graphs.Movement;
import ditl.graphs.MovementEvent;
import ditl.graphs.MovementTrace;
import ditl.graphs.Point;

@Command(pkg = "graphs", cmd = "import-positions", alias = "ip")
public class ImportStaticMovement extends WriteApp {

    private String[] positions_specs;
    private boolean use_id_map;
    private final GraphOptions.CliParser graph_options = new GraphOptions.CliParser(GraphOptions.PRESENCE, GraphOptions.MOVEMENT);

    @Override
    protected String getUsageString() {
        return "[OPTIONS] STORE ID1:X:Y [ID2:X:Y..]";
    }

    @Override
    protected void parseArgs(CommandLine cli, String[] args) throws ArrayIndexOutOfBoundsException, ParseException, HelpException {
        super.parseArgs(cli, args);
        graph_options.parse(cli);
        positions_specs = Arrays.copyOfRange(args, 1, args.length);
        use_id_map = cli.hasOption(stringIdsOption);
    }

    @Override
    protected void initOptions() {
        super.initOptions();
        graph_options.setOptions(options);
        options.addOption(null, stringIdsOption, false, "treat node ids as strings (default: false)");
    }

    @Override
    public void run() throws Exception {
        final Trace<?> presence = _store.getTrace(graph_options.get(GraphOptions.PRESENCE));
        final IdMap id_map = presence.idMap();
        final MovementTrace movement = _store.newTrace(graph_options.get(GraphOptions.MOVEMENT), MovementTrace.class, force);
        final StatefulWriter<MovementEvent, Movement> movementWriter = movement.getWriter();
        final Set<Movement> initState = new HashSet<Movement>();
        for (final String spec : positions_specs) {
            final String[] elems = spec.split(":");
            Integer id;
            if (use_id_map)
                id = id_map.getInternalId(elems[0]);
            else
                id = Integer.parseInt(elems[0]);
            final double x = Double.parseDouble(elems[1]);
            final double y = Double.parseDouble(elems[2]);
            final Point p = new Point(x, y);
            final Movement m = new Movement(id, p);
            initState.add(m);
        }

        movementWriter.setInitState(presence.minTime(), initState);
        movementWriter.setPropertiesFromTrace(presence);
        movementWriter.close();
    }
}
