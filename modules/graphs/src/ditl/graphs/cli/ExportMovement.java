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


public class ExportMovement extends GraphApp {

	private File storeFile = null;
	private boolean useNS2;
	private Long maxTime;
	private Long dtps;
	private String traceName;
	private long interval;
	private String outFile;
	
	public ExportMovement(String[] args) {
		super(args);
	}

	@Override
	protected void setUsageString(){
		usageString = "Usage: [OPTIONS] STORE";
	}
	
	@Override
	protected void parseArgs(CommandLine cli, String[] args) throws ParseException, HelpException {
		storeFile = new File(args[0]);
		useNS2 = cli.getOptionValue(fmtOption, ns2Format).equals(ns2Format);
		maxTime = (Long) cli.getParsedOptionValue(maxTimeOption);
		dtps = getTicsPerSecond( cli.getOptionValue(destTimeUnitOption,"s"));
		if ( dtps == null )
			throw new HelpException();
		traceName = cli.getOptionValue(movementOption, GraphStore.defaultMovementName);
		interval = (long)Double.parseDouble(cli.getOptionValue(intervalOption,"1"));
		outFile = cli.getOptionValue(outputOption);
	}
	
	@Override
	protected void initOptions() {
		options.addOption(null, fmtOption, true, "ns2 or one");
		options.addOption(null, maxTimeOption, true, "maximum movement time (for ONE only)");
		options.addOption(null, destTimeUnitOption, true, "time unit of destination trace [s, ms, us, ns] (default: s)");
		options.addOption(null, movementOption, true, "movement trace name (default: '"+GraphStore.defaultMovementName+"')");
		options.addOption(null, intervalOption, true, "interval (for ONE only)");
		options.addOption(null, outputOption, true, "output file");
	}
	
	@Override
	protected void run() throws IOException, MissingTraceException {
		Store store = Store.open(storeFile);
		Trace trace = getTrace(store, traceName);
		long otps = trace.ticsPerSecond();
		interval *= otps;
		if ( maxTime != null ) maxTime *= otps;
		double timeMul = getTimeMul(otps,dtps);
		OutputStream out = (outFile != null)? new FileOutputStream(outFile) : System.out;
		StatefulReader<MovementEvent,Movement> movementReader = 
			new GraphStore(store).getMovementReader(trace);
		if ( useNS2 )
			NS2Movement.toNS2(movementReader, out, timeMul);
		else
			ONEMovement.toONE(movementReader, out, timeMul, interval, maxTime);
		store.close();
	}
}
