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
package ditl.graphs.cli;

import java.io.*;
import java.util.*;

import org.apache.commons.cli.*;

import ditl.*;
import ditl.graphs.*;


public class ImportStaticMovement extends GraphApp {
	
	private String presenceName;
	private String movementName;
	private File storeFile;
	private String[] positions_specs;
	
	public ImportStaticMovement(String[] args) {
		super(args);
	}
	
	@Override
	protected void setUsageString(){
		usageString = "Usage: [OPTIONS] STORE ID1:X:Y [ID2:X:Y..]";
	}

	@Override
	protected void parseArgs(CommandLine cli, String[] args) throws ArrayIndexOutOfBoundsException, ParseException, HelpException {
		storeFile = new File(args[0]);
		positions_specs = Arrays.copyOfRange(args,1,args.length);
		presenceName = cli.getOptionValue(presenceOption, GraphStore.defaultPresenceName);
		movementName = cli.getOptionValue(movementOption, GraphStore.defaultMovementName);
	}

	@Override
	protected void initOptions() {
		options.addOption(null, presenceOption, true, "presence trace to use for minTime and maxTime (default: "+GraphStore.defaultPresenceName+")");
		options.addOption(null, movementOption, true, "name of imported movement trace (default: "+GraphStore.defaultMovementName+")");
	}
	
	@Override
	public void run() throws IOException, MissingTraceException {
		WritableStore store = WritableStore.open(storeFile);
		
		Trace presence = getTrace(store,presenceName);
		
		StatefulWriter<MovementEvent,Movement> movementWriter = new GraphStore(store).getMovementWriter(movementName, presence.snapshotInterval());
		Set<Movement> initState = new HashSet<Movement>();
		Bounds bounds = new Bounds();
		for ( String spec : positions_specs ){
			String[] elems = spec.split(":");
			Integer id = Integer.parseInt(elems[0]);
			double x = Double.parseDouble(elems[1]);
			double y = Double.parseDouble(elems[2]);
			Point p = new Point(x,y);
			bounds.update(p);
			Movement m = new Movement(id, p);
			initState.add(m);
		}
		
		movementWriter.setInitState(presence.minTime(), initState);
		movementWriter.setProperty(Trace.maxTimeKey, presence.maxTime());
		movementWriter.setProperty(Trace.ticsPerSecondKey, presence.ticsPerSecond());
		bounds.writeToTrace(movementWriter);
		
		movementWriter.close();
		
		store.close();		
	}
}
