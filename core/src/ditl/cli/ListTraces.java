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

import java.io.*;
import java.util.*;

import org.apache.commons.cli.*;

import ditl.*;

public class ListTraces extends App {

	private final static String detailsOption = "all";
	
	protected List<File> storeFiles;
	protected boolean show_descr;
	
	public ListTraces(String[] args) {
		super(args);
	}
	
	@Override
	protected void parseArgs(CommandLine cli, String[] args) throws ParseException, HelpException {
		storeFiles = new LinkedList<File>();
		for ( String path : args )
			storeFiles.add(new File(path) );
		show_descr = cli.hasOption(detailsOption);
	}

	@Override
	protected void setUsageString() {
		usageString = "[OPTIONS] STORE [STORE..]";
	}
	
	@Override
	protected void run() throws IOException {
		for ( File storeFile : storeFiles ){
			Store store = Store.open(storeFile);
			for ( Trace trace : store.listTraces() ){
				System.out.println(trace.name());
				if ( show_descr ){
					System.out.println("   type:        "+trace.type());
					System.out.println("   description: "+trace.description());
					System.out.println();
				}
			}
			store.close();
		}
	}

	@Override
	protected void initOptions() {
		options.addOption("a",detailsOption, false, "Show trace descriptions");
	}
	
}
