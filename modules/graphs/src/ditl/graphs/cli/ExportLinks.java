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

public class ExportLinks extends GraphApp {
	
	private File storeFile = null;
	private boolean useCRAWDAD;
	private Long dtps;
	private String traceName;
	private String outFile;

	public ExportLinks(String[] args) {
		super(args);
	}

	@Override
	protected void initOptions() {
		options.addOption(null, fmtOption, true, "CRAWDAD or ONE");
		options.addOption(null, destTimeUnitOption, true, "time unit of destination trace [s, ms, us, ns] (default: s)");
		options.addOption(null, linksOption, true, "links trace name (default: '"+GraphStore.defaultLinksName+"')");
		options.addOption(null, outputOption, true, "output file");
	}

	@Override
	protected void parseArgs(CommandLine cli, String[] args)
			throws ParseException, ArrayIndexOutOfBoundsException, HelpException {
		storeFile = new File(args[0]);
		useCRAWDAD = cli.getOptionValue(fmtOption, crawdadFormat).equals(crawdadFormat);
		dtps = getTicsPerSecond( cli.getOptionValue(destTimeUnitOption,"s"));
		if ( dtps == null )
			throw new HelpException();
		traceName = cli.getOptionValue(linksOption, GraphStore.defaultLinksName);
		outFile = cli.getOptionValue(outputOption);
	}

	@Override
	protected void run() throws IOException, MissingTraceException {
		Store store = Store.open(storeFile);
		Trace trace = getTrace(store,traceName);
		long otps = trace.ticsPerSecond();
		double timeMul = getTimeMul(otps,dtps);
		OutputStream out = (outFile != null)? new FileOutputStream(outFile) : System.out;
		StatefulReader<LinkEvent,Link> linkReader = 
			new GraphStore(store).getLinkReader(trace);
		if ( useCRAWDAD )
			CRAWDADContacts.toCRAWDAD(linkReader, out, timeMul);
		else
			ONEContacts.toONE(linkReader, out, timeMul);
		store.close();
	}

	@Override
	protected void setUsageString() {
		usageString = "[OPTIONS] STORE";
	}

}
