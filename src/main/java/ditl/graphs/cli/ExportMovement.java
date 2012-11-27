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

import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

import ditl.Store.NoSuchTraceException;
import ditl.cli.Command;
import ditl.cli.ExportApp;
import ditl.graphs.MovementTrace;
import ditl.graphs.NS2Movement;
import ditl.graphs.ONEMovement;

@Command(pkg = "graphs", cmd = "export-movement", alias = "xm")
public class ExportMovement extends ExportApp {

    private Long maxTime;
    private Long dtps;
    private final ExternalFormat.CLIParser ext_fmt_parser = new ExternalFormat.CLIParser(NS2, ONE);
    private ExternalFormat ext_fmt;
    private final GraphOptions.CliParser graph_options = new GraphOptions.CliParser(GraphOptions.MOVEMENT);
    private double d_interval;

    @Override
    protected void parseArgs(CommandLine cli, String[] args) throws ParseException, HelpException {
        super.parseArgs(cli, args);
        graph_options.parse(cli);
        ext_fmt = ext_fmt_parser.parse(cli);
        if (cli.hasOption(maxTimeOption))
            maxTime = Long.parseLong(cli.getOptionValue(maxTimeOption));
        dtps = getTicsPerSecond(cli.getOptionValue(destTimeUnitOption, "s"));
        if (dtps == null)
            throw new HelpException();
        d_interval = Double.parseDouble(cli.getOptionValue(intervalOption, "1"));
    }

    @Override
    protected void initOptions() {
        super.initOptions();
        graph_options.setOptions(options);
        ext_fmt_parser.setOptions(options);
        options.addOption(null, maxTimeOption, true, "maximum movement time (for ONE only)");
        options.addOption(null, destTimeUnitOption, true, "time unit of destination trace [s, ms, us, ns] (default: s)");
        options.addOption(null, intervalOption, true, "interval (for ONE only)");
    }

    @Override
    protected void run() throws IOException, NoSuchTraceException {
        final MovementTrace movement = (MovementTrace) _store.getTrace(graph_options.get(GraphOptions.MOVEMENT));
        final long otps = movement.ticsPerSecond();
        final long interval = Math.max((long) (d_interval * otps), 1);
        if (maxTime != null)
            maxTime *= otps;
        final double timeMul = getTimeMul(otps, dtps);
        switch (ext_fmt) {
            case NS2:
                NS2Movement.toNS2(movement, _out, timeMul);
                break;
            case ONE:
                ONEMovement.toONE(movement, _out, timeMul, interval, maxTime);
                break;
        }
    }
}
