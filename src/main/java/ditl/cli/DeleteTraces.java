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
import java.util.Arrays;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

import ditl.WritableStore;

@Command(cmd = "rm")
public class DeleteTraces extends App {

    protected String[] traceNames;
    protected File store_file;
    protected WritableStore _store;

    @Override
    protected void parseArgs(CommandLine cli, String[] args) throws ParseException, HelpException {
        store_file = new File(args[0]);
        traceNames = Arrays.copyOfRange(args, 1, args.length);
    }

    @Override
    protected String getUsageString() {
        return "[OPTIONS] STORE TRACE1 [TRACE2...]";
    }

    @Override
    protected void run() throws Exception {
        for (final String name : traceNames)
            if (_store.hasTrace(name))
                _store.deleteTrace(name);
    }

    @Override
    protected void init() throws IOException {
        _store = WritableStore.open(store_file);
    }

    @Override
    protected void close() throws IOException {
        _store.close();
    }
}
