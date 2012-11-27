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

import ditl.Store.LoadTraceException;
import ditl.Store.NoSuchTraceException;
import ditl.WritableStore.AlreadyExistsException;
import ditl.cli.Command;
import ditl.cli.ConvertApp;
import ditl.graphs.BufferEdgesConverter;
import ditl.graphs.EdgeTrace;

@Command(pkg = "graphs", cmd = "buffer-edges", alias = "be")
public class BufferEdges extends ConvertApp {

    final static String randomizeOption = "randomize";
    private boolean randomize;
    final static String bufferedEdgesOption = "buffered-edges";
    final static String defaultBufferedEdgesName = "buffered_edges";
    private String bufferedEdgesName;
    private long before_buffer_time;
    private long after_buffer_time;

    private final GraphOptions.CliParser graph_options = new GraphOptions.CliParser(GraphOptions.EDGES);

    @Override
    protected void initOptions() {
        super.initOptions();
        graph_options.setOptions(options);
        options.addOption(null, randomizeOption, false, "Randomize buffer time uniformly between 0 and BUFFER_TIME.");
        options.addOption(null, bufferedEdgesOption, true, "Name of buffered edges trace (default: " + defaultBufferedEdgesName + ")");
    }

    @Override
    protected void parseArgs(CommandLine cli, String[] args)
            throws ParseException, ArrayIndexOutOfBoundsException,
            HelpException {
        super.parseArgs(cli, args);
        graph_options.parse(cli);
        before_buffer_time = Long.parseLong(args[1]);
        if (args.length == 2)
            after_buffer_time = before_buffer_time;
        else
            after_buffer_time = Long.parseLong(args[2]);
        randomize = cli.hasOption(randomizeOption);
        bufferedEdgesName = cli.getOptionValue(bufferedEdgesOption, defaultBufferedEdgesName);
    }

    @Override
    protected void run() throws IOException, NoSuchTraceException, AlreadyExistsException, LoadTraceException {
        final EdgeTrace edges = (EdgeTrace) orig_store.getTrace(graph_options.get(GraphOptions.EDGES));
        final EdgeTrace buffered_edges = (EdgeTrace) dest_store.newTrace(bufferedEdgesName, EdgeTrace.class, force);
        before_buffer_time *= edges.ticsPerSecond();
        after_buffer_time *= edges.ticsPerSecond();
        new BufferEdgesConverter(buffered_edges, edges, before_buffer_time, after_buffer_time, randomize).convert();
    }

    @Override
    public String getUsageString() {
        return "\t[OPTIONS] STORE BUFFER_TIME\n\t[OPTIONS] store BEFORE_BUF_TIME AFTER_BUF_TIME";
    }
}
