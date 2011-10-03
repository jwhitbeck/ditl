/*******************************************************************************
 * This file is part of DITL.                                                  *
 *                                                                             *
 * Copyright (C) 2011 John Whitbeck <john@whitbeck.fr>                         *
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

import org.apache.commons.cli.*;

import ditl.Trace;

public class ListTraces extends ExportApp {

	private final static String detailsOption = "all";
	private boolean show_descr;
	
	public final static String PKG_NAME = null;
	public final static String CMD_NAME = "ls";
	public final static String CMD_ALIAS = null;
	
	@Override
	protected void parseArgs(CommandLine cli, String[] args) throws ParseException, HelpException {
		super.parseArgs(cli, args);
		show_descr = cli.hasOption(detailsOption);
	}

	
	@Override
	protected void run() throws IOException {
		for ( Trace<?> trace : _store.listTraces() ){
			System.out.println(trace.name());
			if ( show_descr ){
				System.out.println("   type:        "+trace.type());
				System.out.println("   description: "+trace.description());
				System.out.println();
			}
		}
	}

	@Override
	protected void initOptions() {
		super.initOptions();
		options.addOption("a",detailsOption, false, "Show trace descriptions");
	}
	
}
