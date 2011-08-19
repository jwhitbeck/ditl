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


public class ImportMovement extends GraphApp {
	
	public ImportMovement(String[] args) {
		super(args);
	}


	private File storeFile;
	private File importedFile;
	private boolean useNS2;
	private Long maxTime;
	private long ticsPerSecond;
	private Double timeMul;
	private String traceName;
	private long interval;
	protected long offset;
	
	@Override
	protected void setUsageString(){
		usageString = "Usage: ImportMovement [OPTIONS] STORE FILE";
	}
	
	@Override
	protected void parseArgs(CommandLine cli, String[] args) throws ParseException, ArrayIndexOutOfBoundsException, HelpException {
		storeFile = new File(args[0]);
		importedFile = new File(args[1]);
		
		useNS2 = cli.getOptionValue(fmtOption, ns2Format).equals(ns2Format);
		maxTime = (Long) cli.getParsedOptionValue(maxTimeOption);
		
		ticsPerSecond = getTicsPerSecond(cli.getOptionValue(destTimeUnitOption,"ms"));
		Long otps = getTicsPerSecond(cli.getOptionValue(origTimeUnitOption,"s"));
		timeMul = getTimeMul(otps,ticsPerSecond);
		if ( timeMul == null )
			throw new HelpException();
		offset = Long.parseLong(cli.getOptionValue(offsetOption,"0")) * ticsPerSecond;
		traceName = cli.getOptionValue(movementOption, GraphStore.defaultMovementName);	
		interval = Long.parseLong(cli.getOptionValue(snapIntervalOption, "60")) * ticsPerSecond; // by default, snap every minute
	}
	
	@Override
	protected void run() throws IOException {
		WritableStore store = WritableStore.open(storeFile);
		StatefulWriter<MovementEvent,Movement> movementWriter = new GraphStore(store).getMovementWriter(traceName, interval);
		movementWriter.setProperty(Trace.ticsPerSecondKey, ticsPerSecond);
		InputStream in = new FileInputStream(importedFile);
		if ( useNS2 )
			NS2Movement.fromNS2(movementWriter, in, maxTime, timeMul, offset);
		else
			ONEMovement.fromONE(movementWriter, in, maxTime, timeMul, offset);
		
		store.close();
	}


	@Override
	protected void initOptions() {
		options.addOption(null, fmtOption, true, "ns2 or one (default: ns2)");
		options.addOption(null, maxTimeOption, true, "maximum movement time");
		options.addOption(null, origTimeUnitOption, true, "time unit of original trace [s, ms, us, ns] (default: s)");
		options.addOption(null, destTimeUnitOption, true, "time unit of destination trace [s, ms, us, ns] (default: ms)");
		options.addOption(null, movementOption, true, "movement trace name (default: '"+GraphStore.defaultMovementName+"')");
		options.addOption(null, snapIntervalOption, true, "snapshot interval in seconds (default 60)");
		options.addOption(null, offsetOption, true, "offset to add to all times in seconds (default 0)");
	}
}
