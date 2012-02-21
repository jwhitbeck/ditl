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
import ditl.Store.NoSuchTraceException;
import ditl.WritableStore.AlreadyExistsException;
import ditl.cli.ImportApp;
import ditl.graphs.*;


public class ImportEdges extends ImportApp {
	
	private long ticsPerSecond;
	private Double timeMul;
	private GraphOptions graph_options = new GraphOptions(GraphOptions.EDGES);
	private long interval;
	private long offset;
	private boolean use_id_map;
	
	public final static String PKG_NAME = "graphs";
	public final static String CMD_NAME = "import-edges";
	public final static String CMD_ALIAS = "ie";

	@Override
	protected void parseArgs(CommandLine cli, String[] args) throws ArrayIndexOutOfBoundsException, ParseException, HelpException {
		super.parseArgs(cli, args);
		graph_options.parse(cli);	
		ticsPerSecond = getTicsPerSecond(cli.getOptionValue(destTimeUnitOption,"ms"));
		Long otps = getTicsPerSecond(cli.getOptionValue(origTimeUnitOption,"s"));
		offset = Long.parseLong(cli.getOptionValue(offsetOption,"0")) * ticsPerSecond;
		timeMul = getTimeMul(otps,ticsPerSecond);
		if ( timeMul == null )
			throw new HelpException();
		interval = Long.parseLong(cli.getOptionValue(snapIntervalOption, "60")) * ticsPerSecond; // by default, snap every minute
		use_id_map = cli.hasOption(stringIdsOption);
	}

	@Override
	protected void initOptions() {
		super.initOptions();
		graph_options.setOptions(options);
		options.addOption(null, origTimeUnitOption, true, "time unit of original trace [s, ms, us, ns] (default: s)");
		options.addOption(null, destTimeUnitOption, true, "time unit of destination trace [s, ms, us, ns] (default: ms)");
		options.addOption(null, snapIntervalOption, true, "snapshot interval in seconds (default 60)");
		options.addOption(null, offsetOption, true, "offset to add to all times in seconds (default 0)");
		options.addOption(null, stringIdsOption, false, "treat node ids as strings (default: false)");
	}
	
	@Override
	public void run() throws IOException, NoSuchTraceException, AlreadyExistsException, LoadTraceException {
		EdgeTrace edges = (EdgeTrace) _store.newTrace(graph_options.get(GraphOptions.EDGES), EdgeTrace.type, force);
		CRAWDADEdges.fromCRAWDAD(edges, _in, timeMul, ticsPerSecond, offset, interval, use_id_map);
	}
}
