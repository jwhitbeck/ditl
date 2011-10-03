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

import java.io.IOException;

import org.apache.commons.cli.*;

import ditl.Store.LoadTraceException;
import ditl.WritableStore.AlreadyExistsException;
import ditl.cli.ImportApp;
import ditl.graphs.*;


public class ImportLinks extends ImportApp {

	private ExternalFormat ext_fmt = new ExternalFormat(ExternalFormat.CRAWDAD, ExternalFormat.ONE);
	private long ticsPerSecond;
	private Double timeMul;
	private GraphOptions graph_options = new GraphOptions(GraphOptions.LINKS);
	private long interval;
	private long offset;
	
	public final static String PKG_NAME = "graphs";
	public final static String CMD_NAME = "import-links";
	public final static String CMD_ALIAS = "il";
	
	@Override
	protected void parseArgs(CommandLine cli, String[] args) throws ArrayIndexOutOfBoundsException, ParseException, HelpException {
		super.parseArgs(cli, args);
		graph_options.parse(cli);
		ext_fmt.parse(cli);
		ticsPerSecond = getTicsPerSecond(cli.getOptionValue(destTimeUnitOption,"ms"));
		Long otps = getTicsPerSecond(cli.getOptionValue(origTimeUnitOption,"s"));
		offset = Long.parseLong(cli.getOptionValue(offsetOption,"0")) * ticsPerSecond;
		timeMul = getTimeMul(otps,ticsPerSecond);
		if ( timeMul == null )
			throw new HelpException();	
		interval = Long.parseLong(cli.getOptionValue(snapIntervalOption, "60")) * ticsPerSecond; // by default, snap every minute
	}

	@Override
	protected void initOptions() {
		super.initOptions();
		graph_options.setOptions(options);
		ext_fmt.setOptions(options);
		options.addOption(null, origTimeUnitOption, true, "time unit of original trace [s, ms, us, ns] (default: s)");
		options.addOption(null, destTimeUnitOption, true, "time unit of destination trace [s, ms, us, ns] (default: ms)");
		options.addOption(null, snapIntervalOption, true, "snapshot interval in seconds (default 60)");
		options.addOption(null, offsetOption, true, "offset to add to all times in seconds (default 0)");
	}
	
	@Override
	public void run() throws IOException, AlreadyExistsException, LoadTraceException {
		LinkTrace links = (LinkTrace) _store.newTrace(graph_options.get(GraphOptions.LINKS), LinkTrace.type, force);
		if ( ext_fmt.is(ExternalFormat.CRAWDAD) )
			CRAWDADContacts.fromCRAWDAD(links, _in, timeMul, ticsPerSecond, offset, interval);
		else
			ONEContacts.fromONE(links, _in, timeMul, ticsPerSecond, offset, interval);
	}
}
