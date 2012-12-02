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

import static ditl.graphs.cli.ExternalFormat.NS2;
import static ditl.graphs.cli.ExternalFormat.ONE;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

import ditl.IdGenerator;
import ditl.IdMap;
import ditl.OffsetIdGenerator;
import ditl.cli.Command;
import ditl.cli.ImportApp;
import ditl.graphs.MovementTrace;
import ditl.graphs.NS2Movement;
import ditl.graphs.ONEMovement;

@Command(pkg = "graphs", cmd = "import-movement", alias = "im")
public class ImportMovement extends ImportApp {

    private final ExternalFormat.CLIParser ext_fmt_parser = new ExternalFormat.CLIParser(NS2, ONE);
    private ExternalFormat ext_fmt;
    private Long maxTime;
    private long ticsPerSecond;
    private Double timeMul;
    private final GraphOptions.CliParser graph_options = new GraphOptions.CliParser(GraphOptions.MOVEMENT);
    private long offset;
    private boolean fix_pause_times;
    private boolean use_id_map;
    private int min_id;

    private final String fixPauseTimesOption = "fix-pause-times";

    @Override
    protected void parseArgs(CommandLine cli, String[] args) throws ParseException, ArrayIndexOutOfBoundsException, HelpException {
        super.parseArgs(cli, args);
        graph_options.parse(cli);
        ext_fmt = ext_fmt_parser.parse(cli);
        ticsPerSecond = getTicsPerSecond(cli.getOptionValue(destTimeUnitOption, "ms"));
        final Long otps = getTicsPerSecond(cli.getOptionValue(origTimeUnitOption, "s"));
        timeMul = getTimeMul(otps, ticsPerSecond);
        if (timeMul == null)
            throw new HelpException();
        offset = Long.parseLong(cli.getOptionValue(offsetOption, "0")) * ticsPerSecond;
        if (cli.hasOption(maxTimeOption))
            maxTime = Long.parseLong(cli.getOptionValue(maxTimeOption)) * ticsPerSecond;
        fix_pause_times = cli.hasOption(fixPauseTimesOption);
        use_id_map = cli.hasOption(stringIdsOption);
        min_id = Integer.parseInt(cli.getOptionValue(minIdOption, "0"));
    }

    @Override
    protected void run() throws Exception {
        final MovementTrace movement = _store.newTrace(graph_options.get(GraphOptions.MOVEMENT), MovementTrace.class, force);
        final IdGenerator id_gen = (use_id_map) ? new IdMap.Writer(min_id) : new OffsetIdGenerator(min_id);
        switch (ext_fmt) {
            case NS2:
                NS2Movement.fromNS2(movement, _in, maxTime, timeMul, ticsPerSecond, offset, fix_pause_times, id_gen);
                break;
            case ONE:
                ONEMovement.fromONE(movement, _in, maxTime, timeMul, ticsPerSecond, offset, id_gen);
                break;
        }
    }

    @Override
    protected void initOptions() {
        super.initOptions();
        graph_options.setOptions(options);
        ext_fmt_parser.setOptions(options);
        options.addOption(null, maxTimeOption, true, "maximum movement time in seconds");
        options.addOption(null, origTimeUnitOption, true, "time unit of original trace [s, ms, us, ns] (default: s)");
        options.addOption(null, destTimeUnitOption, true, "time unit of destination trace [s, ms, us, ns] (default: ms)");
        options.addOption(null, offsetOption, true, "offset to add to all times in seconds (default 0)");
        options.addOption(null, fixPauseTimesOption, false, "fix missing pause times in NS2");
        options.addOption(null, stringIdsOption, false, "treat node ids as strings (default: false)");
        options.addOption(null, minIdOption, true, "ensure that all imported ids are greater than <arg> (default: 0)");
    }
}
