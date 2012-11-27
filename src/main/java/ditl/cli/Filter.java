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
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

import ditl.Converter;
import ditl.FilterConverter;
import ditl.GroupSpecification;
import ditl.StatefulFilterConverter;
import ditl.StatefulTrace;
import ditl.Store.LoadTraceException;
import ditl.Store.NoSuchTraceException;
import ditl.Trace;
import ditl.WritableStore.AlreadyExistsException;

@Command(cmd = "filter")
public class Filter extends ConvertApp {

    private String orig_trace_name;
    private String dest_trace_name;
    private String group_spec;

    @Override
    protected void parseArgs(CommandLine cli, String[] args)
            throws ParseException, ArrayIndexOutOfBoundsException,
            HelpException {
        if (args.length == 4) {
            super.parseArgs(cli, args);
            orig_trace_name = args[1];
            dest_trace_name = args[2];
            group_spec = args[3];
        } else {
            orig_store_file = new File(args[0]);
            dest_store_file = new File(args[1]);
            group_spec = args[2];
            force = cli.hasOption(forceOption);
        }
    }

    @Override
    protected void run() throws IOException, NoSuchTraceException, AlreadyExistsException, LoadTraceException {
        if (orig_trace_name != null) {
            final Trace<?> orig_trace = orig_store.getTrace(orig_trace_name);
            final Trace<?> dest_trace = dest_store.newTrace(dest_trace_name, orig_trace.type(), force);
            filter(dest_trace, orig_trace);
        } else
            for (final Trace<?> orig_trace : orig_store.listTraces()) {
                final Trace<?> dest_trace = dest_store.newTrace(orig_trace.name(), orig_trace.type(), force);
                filter(dest_trace, orig_trace);
            }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void filter(Trace<?> dest, Trace<?> orig) throws IOException {
        Converter filterer = null;
        final Set<Integer> group = GroupSpecification.parse(group_spec, orig.idMap());
        if (orig instanceof Trace.Filterable)
            if (orig.isStateful()) {
                if (orig instanceof StatefulTrace.Filterable)
                    filterer = new StatefulFilterConverter((StatefulTrace<?, ?>) dest,
                            (StatefulTrace<?, ?>) orig, group);
            } else
                filterer = new FilterConverter(dest, orig, group);
        if (filterer == null)
            System.err.println("Trace '" + orig.name() + "' is not filterable. Skipping");
        else
            filterer.convert();
    }

    @Override
    protected String getUsageString() {
        return "\t[OPTIONS] STORE NEWSTORE GROUP_COMPOSITION\n\t[OPTIONS] STORE ORIG_TRACE DEST_TRACE GROUP_COMPOSITION";
    }

}
