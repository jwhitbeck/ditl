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

import ditl.Groups;
import ditl.IdMap;
import ditl.StatefulWriter;
import ditl.Trace;
import ditl.cli.App;
import ditl.cli.WriteApp;
import ditl.graphs.Group;
import ditl.graphs.GroupEvent;
import ditl.graphs.GroupTrace;

@App.Cli(pkg = "graphs", cmd = "import-groups", alias = "ig")
public class ImportStaticGroups extends WriteApp {

    private boolean use_id_map;

    private final GraphOptions.CliParser graph_options = new GraphOptions.CliParser(GraphOptions.PRESENCE, GraphOptions.GROUPS);
    private JSONArray groups_json;

    @Override
    protected String getUsageString() {
        return "[OPTIONS] STORE GROUPS";
    }

    @Override
    protected String getHelpHeader() {
        return "A group is specified as a json array. For example, " +
                "[[1,3],5,[8,12]] corresponds to the group {1,2,3,5,8,9,10,11,12}.\n" +
                " The GROUPS parameter is an array of groups. For example: \n" +
                " [ {label:cars, members:[[1,3],5]}, {label:busses, members:[4,6]}] \n" +
                " describes two groups labeled 'cars' and 'busses'.";
    }

    @Override
    protected void parseArgs(CommandLine cli, String[] args) throws ArrayIndexOutOfBoundsException, ParseException, HelpException {
        super.parseArgs(cli, args);
        graph_options.parse(cli);
        groups_json = JSONArray.fromObject(args[1]);
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
        final IdMap id_map = (use_id_map) ? presence.idMap() : null;
        final GroupTrace groups = _store.newTrace(graph_options.get(GraphOptions.GROUPS), GroupTrace.class, force);
        final StatefulWriter<GroupEvent, Group> groupWriter = groups.getWriter();
        final Set<Group> initState = new HashSet<Group>();
        int i = 0;
        JSONObject labels = new JSONObject();
        for (final Object obj : groups_json) {
            JSONObject gspec = (JSONObject) obj;
            final Set<Integer> members = Groups.parse(gspec.getJSONArray("members"), id_map);
            if (gspec.has("label"))
                labels.accumulate(gspec.getString("label"), i);
            initState.add(new Group(i, members));
            i++;
        }

        groupWriter.setInitState(presence.minTime(), initState);
        groupWriter.setPropertiesFromTrace(presence);

        if (!labels.isEmpty()) {
            groupWriter.setProperty(GroupTrace.labelsKey, labels);
        }

        groupWriter.close();
    }
}
