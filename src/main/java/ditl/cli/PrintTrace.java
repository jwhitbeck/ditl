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

import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

import ditl.Reader;
import ditl.StatefulReader;
import ditl.StatefulTrace;
import ditl.Trace;

@App.Cli(cmd = "pt")
public class PrintTrace extends ReadOnlyApp {

    private String trace_name;

    @Override
    protected void parseArgs(CommandLine cli, String[] args) throws ParseException, HelpException, ArrayIndexOutOfBoundsException {
        super.parseArgs(cli, args);
        trace_name = args[1];
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected void run() throws IOException {
        final Trace trace = _store.getTrace(trace_name);
        final Reader reader = trace.getReader();
        if (trace instanceof StatefulTrace) {
            reader.seek(trace.minTime());
            for (Object state : ((StatefulReader) reader).referenceState()) {
                System.out.println(trace.minTime() + " " + state);
            }
            System.out.println("-----------------------------");
        }
        while (reader.hasNext()) {
            long time = reader.nextTime();
            for (Object obj : reader.next()) {
                System.out.println(time + " " + obj);
            }
        }
        reader.close();
    }

    @Override
    protected String getUsageString() {
        return "[OPTIONS] STORE TRACE_NAME TIME";
    }

}
