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
import ditl.Writer;
import ditl.graphs.*;


public class Resample extends GraphApp {
	
	final static String missProbabilityOption = "miss-probability";

	protected File origStoreFile;
	protected File destStoreFile;
	protected String beaconsName;
	protected String presenceName;
	protected String linksName;
	long period;
	double p;
	
	public Resample(String[] args) {
		super(args);
	}

	@Override
	protected void parseArgs(CommandLine cli, String[] args) throws ParseException, ArrayIndexOutOfBoundsException {
		origStoreFile = new File(args[0]);
		destStoreFile = new File(cli.getOptionValue(storeOutputOption,args[0]));
		
		beaconsName = cli.getOptionValue(beaconsOption, GraphStore.defaultBeaconsName);
		linksName = cli.getOptionValue(linksOption,GraphStore.defaultLinksName);
		presenceName = cli.getOptionValue(linksOption,GraphStore.defaultPresenceName);
		
		period = Long.parseLong(args[1]);
		p = Double.parseDouble(cli.getOptionValue(missProbabilityOption,"0.0"));	
	}

	@Override
	protected void initOptions() {
		options.addOption(null, storeOutputOption, true, "Name of store to output new traces to");
		options.addOption(null, linksOption, true, "Name of links trace to use");
		options.addOption(null, presenceOption, true, "Name of presence trace to use");
		options.addOption(null, beaconsOption, true, "Name of intermediary beacons trace");
		options.addOption(null, missProbabilityOption, true, "Missed beacon probability (default 0.0)");
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
		
		GraphStore gStore = new GraphStore(origStore);
		
		Trace links = getTrace(origStore,linksName);
		Trace presence = getTrace(origStore,presenceName);
		
		period *= links.ticsPerSecond();
		
		Converter converter;
		StatefulReader<LinkEvent,Link> linkReader = gStore.getLinkReader(links);
		StatefulReader<PresenceEvent,Presence> presenceReader = gStore.getPresenceReader(presence);
		Writer<Edge> beaconWriter = destStore.getWriter(beaconsName);
		converter = new BeaconningConverter(beaconWriter, presenceReader, linkReader, period, p, true);
		converter.run();
		converter.close();
		
		origStore.close();
		if ( destStore != origStore )
			destStore.close();
		
	}

	@Override
	protected void setUsageString() {
		usageString = "Resample [OPTIONS] STORE BEACONNING_PERIOD";
	}

}
