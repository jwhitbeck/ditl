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
import ditl.Reader;
import ditl.graphs.*;


public class BeaconsToEdges extends GraphApp {
	
	final static String toleranceOption = "tolerance";
	final static String expandOption = "expand";

	protected File origStoreFile;
	protected File destStoreFile;
	protected String beaconsName;
	protected String edgesName;
	
	long snap_interval;
	long period;
	int tol;
	double expansion;
	
	public BeaconsToEdges(String[] args) {
		super(args);
	}

	@Override
	protected void parseArgs(CommandLine cli, String[] args) throws ParseException, ArrayIndexOutOfBoundsException {
		origStoreFile = new File(args[0]);
		destStoreFile = new File(cli.getOptionValue(storeOutputOption,args[0]));
		
		beaconsName = cli.getOptionValue(beaconsOption, GraphStore.defaultBeaconsName);
		edgesName = cli.getOptionValue(edgesOption, GraphStore.defaultEdgesName);
		
		period = Long.parseLong(args[1]);
		tol = Integer.parseInt(cli.getOptionValue(toleranceOption,"0"));
		expansion = Double.parseDouble(cli.getOptionValue(expandOption, "0.0"));
		
		snap_interval = Long.parseLong(cli.getOptionValue(snapIntervalOption, "60")); // by default, snap every minute
	}

	@Override
	protected void initOptions() {
		options.addOption(null, storeOutputOption, true, "Name of store to output new traces to");
		options.addOption(null, edgesOption, true, "Name of edges trace to use");
		options.addOption(null, beaconsOption, true, "Name of intermediary beacons trace");
		options.addOption(null, toleranceOption, true, "Missed beacon tolerance (default 0)");
		options.addOption(null, expandOption, true, "Expand contacts by this fraction (default 0.0)");
		options.addOption(null, snapIntervalOption, true, "snapshot interval in seconds (default 60)");
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
		
		GraphStore ogStore = new GraphStore(origStore);
		GraphStore dgStore = new GraphStore(destStore);
		
		Trace beacons = getTrace(origStore,beaconsName);
		
		snap_interval *= beacons.ticsPerSecond();
		period *= beacons.ticsPerSecond();
		
		Reader<Edge> beaconsReader = ogStore.getBeaconsReader(beacons);
		StatefulWriter<EdgeEvent,Edge> edgesWriter = dgStore.getEdgeWriter(edgesName, snap_interval);
		Converter converter = new BeaconsToEdgesConverter(edgesWriter, beaconsReader, period, tol, expansion);
		converter.run();
		converter.close();
		
		destStore.close();
		if ( origStore != destStore )
			origStore.close();
	}

	@Override
	protected void setUsageString() {
		usageString = "Resample [OPTIONS] STORE BEACONNING_PERIOD";
	}

}
