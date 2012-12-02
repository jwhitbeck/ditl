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
package ditl.cli;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

import ditl.SubtraceConverter;
import ditl.Trace;

@App.Cli(cmd = "trunc")
public class Truncate extends ConvertApp {

    private String orig_trace_name;
    private String dest_trace_name;
    private long minTime;
    private long maxTime;

    @Override
    protected void parseArgs(CommandLine cli, String[] args)
            throws ParseException, ArrayIndexOutOfBoundsException,
            HelpException {
        if (args.length == 5) {
            super.parseArgs(cli, args);
            orig_trace_name = args[1];
            dest_trace_name = args[2];
            minTime = Long.parseLong(args[3]);
            maxTime = Long.parseLong(args[4]);
        } else {
            orig_store_file = new File(args[0]);
            dest_store_file = new File(args[1]);
            minTime = Long.parseLong(args[2]);
            maxTime = Long.parseLong(args[3]);
            force = cli.hasOption(forceOption);
        }
    }

    @Override
    protected void run() throws Exception {
        if (orig_trace_name != null) {
            final Trace<?> orig_trace = orig_store.getTrace(orig_trace_name);
            final Trace<?> dest_trace = dest_store.newTrace(dest_trace_name, orig_trace.type(), force);
            truncate(dest_trace, orig_trace);
        } else
            for (final Trace<?> orig_trace : orig_store.listTraces()) {
                final Trace<?> dest_trace = dest_store.newTrace(orig_trace.name(), orig_trace.type(), force);
                truncate(dest_trace, orig_trace);
            }
    }

    private void truncate(Trace<?> dest, Trace<?> orig) throws IOException {
        final long min_time = minTime * orig.ticsPerSecond();
        final long max_time = maxTime * orig.ticsPerSecond();
        new SubtraceConverter(dest, orig, min_time, max_time).convert();
    }

    @Override
    protected String getUsageString() {
        return "\t[OPTIONS] STORE NEWSTORE BEGIN END\n\t[OPTIONS] STORE ORIG_TRACE DEST_TRACE BEGIN END";
    }

}
