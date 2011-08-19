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
import java.util.Arrays;

import org.apache.commons.cli.*;

import ditl.*;

public class DeleteTraces extends App {

	protected File storeFile;
	protected String[] traceNames;
	
	public DeleteTraces(String[] args) {
		super(args);
	}
	
	@Override
	protected void parseArgs(CommandLine cli, String[] args) throws ParseException, HelpException {
		storeFile = new File(args[0]);
		traceNames = Arrays.copyOfRange(args, 1, args.length);
	}

	@Override
	protected void setUsageString() {
		usageString = "[OPTIONS] STORE TRACE1 [TRACE2...]";
	}
	
	@Override
	protected void run() throws IOException, MissingTraceException {
		WritableStore store = WritableStore.open(storeFile);
		for ( String name : traceNames ){
			Trace trace = getTrace(store,name);
			store.deleteTrace(trace);
		}
		store.close();
	}

	@Override
	protected void initOptions() {}
	
}
