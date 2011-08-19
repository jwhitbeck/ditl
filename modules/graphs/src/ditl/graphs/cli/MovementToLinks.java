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

public class MovementToLinks extends GraphApp {
	
	private File origStoreFile;
	private File destStoreFile;
	private String linksName;
	private String movementName;
	private Long snapInterval;
	private double range;
	private Long max_interval;

	public MovementToLinks(String[] args) {
		super(args);
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
		StatefulWriter<LinkEvent,Link> linksWriter = new GraphStore(destStore).getLinkWriter(linksName, snapInterval);
		
		if ( max_interval == null )
			max_interval = movement.maxTime()-movement.minTime();
		else
			max_interval *= movement.ticsPerSecond();
		
		Converter converter = new MovementToLinksConverter(linksWriter, movementReader, range, max_interval);
		converter.run();
		converter.close();
		
		destStore.close();
		if ( origStore != destStore )
			origStore.close();
	}
	
	@Override
	protected void parseArgs(CommandLine cli, String[] args) throws ParseException, HelpException {
		origStoreFile = new File(args[0]);
		destStoreFile = new File(cli.getOptionValue(storeOutputOption,args[0]));
		linksName = cli.getOptionValue(linksOption, GraphStore.defaultLinksName);
		movementName = cli.getOptionValue(movementOption, GraphStore.defaultMovementName);
		range = Double.parseDouble(args[1]);
		snapInterval = (Long) cli.getParsedOptionValue(snapIntervalOption);
		max_interval = (Long) cli.getParsedOptionValue(intervalOption);
	}

	@Override
	protected void setUsageString() {
		usageString = "[OPTIONS] STORE RANGE";
	}
	
	@Override
	protected void initOptions(){
		options.addOption(null, storeOutputOption, true, "write new traces to this store");
		options.addOption(null, movementOption, true, "movement trace name (default: '"+GraphStore.defaultMovementName+"')");
		options.addOption(null, linksOption, true, "links trace name (default: '"+GraphStore.defaultLinksName+"')");
		options.addOption(null, snapIntervalOption, true, "snapshot interval (default: same as movement trace)");
		options.addOption(null, intervalOption, true, "interval beyond which not to look for new meetings (useful if positions are updated every seconds)");
	}

}
