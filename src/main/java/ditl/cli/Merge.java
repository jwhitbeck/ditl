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
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

import ditl.Converter;
import ditl.MergeConverter;
import ditl.StatefulMergeConverter;
import ditl.StatefulTrace;
import ditl.Store;
import ditl.Trace;

@Command(cmd = "merge")
public class Merge extends WriteApp {

    private String[] orig_store_names;
    private Store[] orig_stores;

    @Override
    protected void parseArgs(CommandLine cli, String[] args)
            throws ParseException, ArrayIndexOutOfBoundsException,
            HelpException {
        super.parseArgs(cli, args);
        orig_store_names = new String[args.length - 1];
        for (int i = 1; i < args.length; ++i)
            orig_store_names[i - 1] = args[i];
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    protected void run() throws Exception {
        for (final String traceName : getCommonTraceNames()) {
            final List<Trace<?>> traces = new LinkedList<Trace<?>>();
            for (final Store store : orig_stores)
                traces.add(store.getTrace(traceName));

            final Trace<?> ref_trace = traces.get(0);
            final String type = ref_trace.type();
            final boolean stateful = ref_trace instanceof StatefulTrace;

            final Trace<?> merged = _store.newTrace(traceName, type, stateful);

            Converter merger;
            if (stateful)
                merger = new StatefulMergeConverter((StatefulTrace<?, ?>) merged, traces);
            else
                merger = new MergeConverter(merged, traces);
            merger.convert();
        }
    }

    @Override
    protected String getUsageString() {
        return "[OPTIONS] MERGED_STORE STORE1 [STORE2 ...]";
    }

    @Override
    protected void init() throws Exception {
        super.init();
        orig_stores = new Store[orig_store_names.length];
        for (int i = 0; i < orig_store_names.length; ++i)
            orig_stores[i] = Store.open(new File(orig_store_names[i]));
    }

    @Override
    protected void close() throws IOException {
        super.close();
        for (final Store store : orig_stores)
            store.close();
    }

    private List<String> getCommonTraceNames() {
        final List<String> names = new LinkedList<String>();
        for (final Trace<?> trace : orig_stores[0].listTraces()) {
            final String name = trace.name();
            boolean ok = true;
            for (int i = 1; i < orig_stores.length; ++i)
                if (!orig_stores[i].hasTrace(name)) {
                    ok = false;
                    break;
                }
            if (ok)
                names.add(name);
        }
        return names;
    }

}
