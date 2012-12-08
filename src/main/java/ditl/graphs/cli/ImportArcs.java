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

import ditl.IdGenerator;
import ditl.IdMap;
import ditl.OffsetIdGenerator;
import ditl.cli.App;
import ditl.cli.ImportApp;
import ditl.graphs.ArcTrace;
import ditl.graphs.CRAWDADArcs;

@App.Cli(pkg = "graphs", cmd = "import-arcs", alias = "ia")
public class ImportArcs extends ImportApp {

    private long ticsPerSecond;
    private Double timeMul;
    private final GraphOptions.CliParser graph_options = new GraphOptions.CliParser(GraphOptions.ARCS);
    private long offset;
    private boolean use_id_map;
    private int min_id;

    @Override
    protected void parseArgs(CommandLine cli, String[] args) throws ArrayIndexOutOfBoundsException, ParseException, HelpException {
        super.parseArgs(cli, args);
        graph_options.parse(cli);
        ticsPerSecond = getTicsPerSecond(cli.getOptionValue(destTimeUnitOption, "ms"));
        final Long otps = getTicsPerSecond(cli.getOptionValue(origTimeUnitOption, "s"));
        offset = Long.parseLong(cli.getOptionValue(offsetOption, "0")) * ticsPerSecond;
        timeMul = getTimeMul(otps, ticsPerSecond);
        if (timeMul == null)
            throw new HelpException();
        use_id_map = cli.hasOption(stringIdsOption);
        min_id = Integer.parseInt(cli.getOptionValue(minIdOption, "0"));
    }

    @Override
    protected void initOptions() {
        super.initOptions();
        graph_options.setOptions(options);
        options.addOption(null, origTimeUnitOption, true, "time unit of original trace [s, ms, us, ns] (default: s)");
        options.addOption(null, destTimeUnitOption, true, "time unit of destination trace [s, ms, us, ns] (default: ms)");
        options.addOption(null, offsetOption, true, "offset to add to all times in seconds (default 0)");
        options.addOption(null, stringIdsOption, false, "treat node ids as strings (default: false)");
        options.addOption(null, minIdOption, true, "ensure that all imported ids are greater than <arg> (default: 0)");
    }

    @Override
    public void run() throws Exception {
        final ArcTrace arcs = _store.newTrace(graph_options.get(GraphOptions.ARCS), ArcTrace.class, force);
        final IdGenerator id_gen = (use_id_map) ? new IdMap.Writer(min_id) : new OffsetIdGenerator(min_id);
        CRAWDADArcs.fromCRAWDAD(arcs, _in, timeMul, ticsPerSecond, offset, id_gen);
    }
}
