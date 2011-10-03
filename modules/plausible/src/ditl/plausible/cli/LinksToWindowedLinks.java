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
package ditl.plausible.cli;

import java.io.IOException;

import org.apache.commons.cli.*;

import ditl.Store.*;
import ditl.WritableStore.AlreadyExistsException;
import ditl.cli.ConvertApp;
import ditl.graphs.LinkTrace;
import ditl.graphs.cli.GraphOptions;
import ditl.plausible.*;

public class LinksToWindowedLinks extends ConvertApp {
	
	private GraphOptions graph_options = new GraphOptions(GraphOptions.LINKS);
	private String windowedLinksOption = "windowed-links";
	private String windowed_links_name;
	private long window;
	
	public final static String PKG_NAME = "plausible";
	public final static String CMD_NAME = "links-to-windowed-links";
	public final static String CMD_ALIAS = "l2wl";
	
	@Override
	protected void initOptions() {
		super.initOptions();
		graph_options.setOptions(options);
		options.addOption(null, windowedLinksOption, true, "name of windowed link trace (default: "+WindowedLinkTrace.defaultName+")");
	}

	@Override
	protected void parseArgs(CommandLine cli, String[] args)
			throws ParseException, ArrayIndexOutOfBoundsException,
			HelpException {
		super.parseArgs(cli, args);
		graph_options.parse(cli);
		windowed_links_name = cli.getOptionValue(windowedLinksOption, WindowedLinkTrace.defaultName);
		window = Long.parseLong(args[1]);
	}

	@Override
	protected void run() throws IOException, NoSuchTraceException, AlreadyExistsException, LoadTraceException {
		LinkTrace links = (LinkTrace) orig_store.getTrace(graph_options.get(GraphOptions.LINKS));
		WindowedLinkTrace windowed_links = (WindowedLinkTrace) dest_store.newTrace(windowed_links_name, WindowedLinkTrace.type, force);
		window *= links.ticsPerSecond();
		new WindowedLinkConverter(windowed_links, links, window).convert();
	}
	
	@Override
	protected String getUsageString(){
		return "[OPTIONS] STORE WINDOW";
	}
	

}
