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

import org.apache.commons.cli.*;

import ditl.*;
import ditl.graphs.*;

public class MovementToPresence extends GraphApp {

	private File origStoreFile;
	private File destStoreFile;
	private String presenceName;
	private String movementName;
	private Long snapInterval;
	
	public MovementToPresence(String[] args) {
		super(args);
	}

	@Override
	protected void initOptions() {
		options.addOption(null, storeOutputOption, true, "write new traces to this store");
		options.addOption(null, presenceOption, true, "name of presence trace");
		options.addOption(null, movementOption, true, "name of movement trace");
		options.addOption(null, snapIntervalOption, true, "snapshot interval");
	}

	@Override
	protected void parseArgs(CommandLine cli, String[] args)
			throws ParseException, ArrayIndexOutOfBoundsException,
			HelpException {
		origStoreFile = new File(args[0]);
		destStoreFile = new File(cli.getOptionValue(storeOutputOption,args[0]));
		presenceName = cli.getOptionValue(presenceOption, GraphStore.defaultPresenceName);
		movementName = cli.getOptionValue(movementOption, GraphStore.defaultMovementName);
		snapInterval = (Long) cli.getParsedOptionValue(snapIntervalOption);
	}

	@Override
	protected void run() throws IOException, MissingTraceException {
		Store origStore;
		WritableStore destStore = WritableStore.open(destStoreFile);
		if ( origStoreFile.equals(destStoreFile) ){
			origStore = destStore; 
		} else {
			origStore = Store.open(origStoreFile);
		}
		
		Trace movement = getTrace(origStore,movementName);
		if ( snapInterval == null )
			snapInterval = movement.snapshotInterval();
		StatefulReader<MovementEvent,Movement> movementReader = new GraphStore(origStore).getMovementReader(movement);
		StatefulWriter<PresenceEvent,Presence> presenceWriter = new GraphStore(destStore).getPresenceWriter(presenceName, snapInterval);
		
		Converter converter = new MovementToPresenceConverter(presenceWriter, movementReader);
		converter.run();
		converter.close();
		
		destStore.close();
		if ( origStore != destStore )
			origStore.close();
	}

	@Override
	protected void setUsageString() {
		usageString = "[OPTIONS] STORE";
	}

}
