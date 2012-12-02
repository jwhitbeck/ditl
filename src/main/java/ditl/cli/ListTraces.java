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

import java.util.Collection;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

import ditl.Trace;

@App.Cli(cmd = "ls")
public class ListTraces extends ReadOnlyApp {

    private final static String detailsOption = "all";
    private boolean show_descr;
    private String type;

    @Override
    protected void parseArgs(CommandLine cli, String[] args) throws ParseException, HelpException, ArrayIndexOutOfBoundsException {
        super.parseArgs(cli, args);
        show_descr = cli.hasOption(detailsOption);
        type = cli.getOptionValue(typeOption);
    }

    @Override
    protected void run() throws Exception {
        final Collection<? extends Trace<?>> traces = (type == null) ? _store.listTraces() : _store.listTraces(_store.getTraceClass(type));
        for (final Trace<?> trace : traces) {
            System.out.println(trace.name());
            if (show_descr) {
                System.out.println("   type:        " + trace.type());
                System.out.println("   description: " + trace.description());
                System.out.println();
            }
        }
    }

    @Override
    protected void initOptions() {
        super.initOptions();
        options.addOption("a", detailsOption, false, "Show trace descriptions");
        options.addOption("t", typeOption, true, "Show only traces of type <arg>");
    }

}
