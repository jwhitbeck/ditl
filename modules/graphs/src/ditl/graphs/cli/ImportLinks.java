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


public class ImportLinks extends GraphApp {

	public ImportLinks(String[] args) {
		super(args);
	}

	private boolean useCrawdad;
	private long ticsPerSecond;
	private Double timeMul;
	private String traceName;
	private long interval;
	private long offset;
	private File storeFile;
	private File importedFile;
	
	@Override
	protected void setUsageString(){
		usageString = "Usage: [OPTIONS] STORE FILE";
	}

	@Override
	protected void parseArgs(CommandLine cli, String[] args) throws ArrayIndexOutOfBoundsException, ParseException, HelpException {
		storeFile = new File(args[0]);
		importedFile = new File(args[1]);
		
		useCrawdad = cli.getOptionValue(fmtOption, crawdadFormat).equals(crawdadFormat);
		
		ticsPerSecond = getTicsPerSecond(cli.getOptionValue(destTimeUnitOption,"ms"));
		Long otps = getTicsPerSecond(cli.getOptionValue(origTimeUnitOption,"s"));
		offset = Long.parseLong(cli.getOptionValue(offsetOption,"0")) * ticsPerSecond;
		timeMul = getTimeMul(otps,ticsPerSecond);
		if ( timeMul == null )
			throw new HelpException();
		
		traceName = cli.getOptionValue(linksOption, GraphStore.defaultLinksName);	
		interval = Long.parseLong(cli.getOptionValue(snapIntervalOption, "60")) * ticsPerSecond; // by default, snap every minute
	}

	@Override
	protected void initOptions() {
		options.addOption(null, fmtOption, true, "crawdad or one (default: crawdad)");
		options.addOption(null, origTimeUnitOption, true, "time unit of original trace [s, ms, us, ns] (default: s)");
		options.addOption(null, destTimeUnitOption, true, "time unit of destination trace [s, ms, us, ns] (default: ms)");
		options.addOption(null, linksOption, true, "links trace name (default: '"+GraphStore.defaultLinksName+"')");
		options.addOption(null, snapIntervalOption, true, "snapshot interval in seconds (default 60)");
		options.addOption(null, offsetOption, true, "offset to add to all times in seconds (default 0)");
	}
	
	@Override
	public void run() throws IOException {
		WritableStore store = WritableStore.open(storeFile);
		
		StatefulWriter<LinkEvent,Link> linkWriter = new GraphStore(store).getLinkWriter(traceName, interval);
		linkWriter.setProperty(Trace.ticsPerSecondKey, ticsPerSecond);
		InputStream in = new FileInputStream(importedFile);
		if ( useCrawdad )
			CRAWDADContacts.fromCRAWDAD(linkWriter, in, timeMul, offset);
		else
			ONEContacts.fromONE(linkWriter, in, timeMul, offset);
		
		store.close();		
	}
}
