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

import java.util.HashSet;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

import ditl.IdMap;
import ditl.StatefulWriter;
import ditl.Trace;
import ditl.cli.App;
import ditl.cli.WriteApp;
import ditl.graphs.Movement;
import ditl.graphs.MovementEvent;
import ditl.graphs.MovementTrace;
import ditl.graphs.Point;

@App.Cli(pkg = "graphs", cmd = "import-positions", alias = "ip")
public class ImportStaticMovement extends WriteApp {

    private JSONObject positions_specs;
    private boolean use_id_map;
    private final GraphOptions.CliParser graph_options = new GraphOptions.CliParser(GraphOptions.PRESENCE, GraphOptions.MOVEMENT);

    @Override
    protected String getUsageString() {
        return "[OPTIONS] STORE POSITION_SPEC";
    }

    @Override
    protected String getHelpHeader() {
        return "POSITION_SPEC is a json string such as : \n" +
                "{ 1:[5,4], 3:[0,1] }\n" +
                "which states that node 1 is at position (x=5,y=4) and node 3 at position (x=0,y=1).";
    }

    @Override
    protected void parseArgs(CommandLine cli, String[] args) throws ArrayIndexOutOfBoundsException, ParseException, HelpException {
        super.parseArgs(cli, args);
        graph_options.parse(cli);
        positions_specs = JSONObject.fromObject(args[1]);
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
        for (Object obj : positions_specs.keySet()) {
            String key = (String) obj;
            Integer id = use_id_map ? id_map.getInternalId(key) : Integer.parseInt(key);
            JSONArray coords = positions_specs.getJSONArray(key);
            initState.add(new Movement(id, new Point(coords.getDouble(0), coords.getDouble(1))));
        }

        movementWriter.setInitState(presence.minTime(), initState);
        movementWriter.setPropertiesFromTrace(presence);
        movementWriter.close();
    }
}
