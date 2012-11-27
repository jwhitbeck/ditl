/*******************************************************************************
 * This file is part of DITL.                                                  *
 *                                                                             *
 * Copyright (C) 2011-2012 John Whitbeck <john@whitbeck.fr>                    *
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

import ditl.*;
import ditl.Store.LoadTraceException;
import ditl.WritableStore.AlreadyExistsException;
import ditl.cli.ImportApp;
import ditl.graphs.*;

import static ditl.graphs.cli.ExternalFormat.*;

public class ImportEdges extends ImportApp {

	private final ExternalFormat.CLIParser ext_fmt_parser = new ExternalFormat.CLIParser(CRAWDAD, ONE);
	private ExternalFormat ext_fmt;
	private long ticsPerSecond;
	private Double timeMul;
	private GraphOptions graph_options = new GraphOptions(GraphOptions.EDGES);
	private long offset;
	private boolean use_id_map;
	private int min_id;
	
	public final static String PKG_NAME = "graphs";
	public final static String CMD_NAME = "import-edges";
	public final static String CMD_ALIAS = "ie";
	
	@Override
	protected void parseArgs(CommandLine cli, String[] args) throws ArrayIndexOutOfBoundsException, ParseException, HelpException {
		super.parseArgs(cli, args);
		graph_options.parse(cli);
		ext_fmt = ext_fmt_parser.parse(cli);
		ticsPerSecond = getTicsPerSecond(cli.getOptionValue(destTimeUnitOption,"ms"));
		Long otps = getTicsPerSecond(cli.getOptionValue(origTimeUnitOption,"s"));
		offset = Long.parseLong(cli.getOptionValue(offsetOption,"0")) * ticsPerSecond;
		timeMul = getTimeMul(otps,ticsPerSecond);
		if ( timeMul == null )
			throw new HelpException();	
		use_id_map = cli.hasOption(stringIdsOption);
		min_id = Integer.parseInt(cli.getOptionValue(minIdOption, "0"));
	}

	@Override
	protected void initOptions() {
		super.initOptions();
		graph_options.setOptions(options);
		ext_fmt_parser.setOptions(options);
		options.addOption(null, origTimeUnitOption, true, "time unit of original trace [s, ms, us, ns] (default: s)");
		options.addOption(null, destTimeUnitOption, true, "time unit of destination trace [s, ms, us, ns] (default: ms)");
		options.addOption(null, offsetOption, true, "offset to add to all times in seconds (default 0)");
		options.addOption(null, stringIdsOption, false, "treat node ids as strings (default: false)");
		options.addOption(null, minIdOption, true, "ensure that all imported ids are greater than <arg> (default: 0)");
	}
	
	@Override
	public void run() throws IOException, AlreadyExistsException, LoadTraceException {
		EdgeTrace edges = (EdgeTrace) _store.newTrace(graph_options.get(GraphOptions.EDGES), EdgeTrace.type, force);
		IdGenerator id_gen = (use_id_map)? new IdMap.Writer(min_id) : new OffsetIdGenerator(min_id);
		switch ( ext_fmt ){
		case CRAWDAD: CRAWDADContacts.fromCRAWDAD(edges, _in, timeMul, ticsPerSecond, offset, id_gen); break;
		case ONE: ONEContacts.fromONE(edges, _in, timeMul, ticsPerSecond, offset, id_gen); break;
		}
	}
}
