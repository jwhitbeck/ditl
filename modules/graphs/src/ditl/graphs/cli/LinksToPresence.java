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

import ditl.Store.*;
import ditl.WritableStore.AlreadyExistsException;
import ditl.cli.ConvertApp;
import ditl.graphs.*;

public class LinksToPresence extends ConvertApp {

	final static String strictOption = "strict"; 
	private boolean strict; 
	
	private GraphOptions graph_options = new GraphOptions(GraphOptions.LINKS, GraphOptions.PRESENCE);
	
	public final static String PKG_NAME = "graphs";
	public final static String CMD_NAME = "links-to-presence";
	public final static String CMD_ALIAS = "l2p";
	
	@Override
	protected void initOptions() {
		super.initOptions();
		graph_options.setOptions(options);
		options.addOption(null, strictOption, false, "Nodes enter with their first contact and leave after their last." );
	}

	@Override
	protected void parseArgs(CommandLine cli, String[] args)
			throws ParseException, ArrayIndexOutOfBoundsException,
			HelpException {
		super.parseArgs(cli, args);
		graph_options.parse(cli);
		strict = cli.hasOption(strictOption);
	}

	@Override
	protected void run() throws IOException, NoSuchTraceException, AlreadyExistsException, LoadTraceException {
		LinkTrace links = (LinkTrace) orig_store.getTrace(graph_options.get(GraphOptions.LINKS));
		PresenceTrace presence = (PresenceTrace) dest_store.newTrace(graph_options.get(GraphOptions.PRESENCE), PresenceTrace.type, force);
		if ( strict )
			new StrictLinksToPresenceConverter(presence, links).convert();
		else
			new LinksToPresenceConverter(presence, links).convert();
	}
}
