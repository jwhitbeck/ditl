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

import org.apache.commons.cli.*;

import ditl.*;
import ditl.Store.NoSuchTraceException;
import ditl.cli.ExportApp;
import ditl.graphs.*;


public class ExportPositions extends ExportApp {

	private GraphOptions graph_options = new GraphOptions(GraphOptions.MOVEMENT);
	private double time;
	
	public final static String PKG_NAME = "graphs";
	public final static String CMD_NAME = "export-positions";
	public final static String CMD_ALIAS = "xp";

	@Override
	protected void parseArgs(CommandLine cli, String[] args) throws ParseException, HelpException {
		super.parseArgs(cli, args);
		graph_options.parse(cli);
		time = Double.parseDouble(args[1]);
	}
	
	@Override
	protected void initOptions() {
		super.initOptions();
		graph_options.setOptions(options);
	}
	
	@Override
	protected void run() throws IOException, NoSuchTraceException {
		MovementTrace movement = (MovementTrace) _store.getTrace(graph_options.get(GraphOptions.MOVEMENT));
		long u_time = (long)(time*movement.ticsPerSecond());
		StatefulReader<MovementEvent,Movement> reader = movement.getReader();
		reader.seek(u_time);
		for ( Movement m : reader.referenceState() )
			System.out.println(m.id()+" "+m.positionAtTime(u_time));
		reader.close();
	}
	
	@Override
	protected String getUsageString() {
		return "[OPTIONS] STORE TIME";
	}
}
