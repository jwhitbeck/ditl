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

public class LinksToPresence extends GraphApp {

	private File origStoreFile;
	private File destStoreFile;
	private String presenceName;
	private String linksName;
	private Long snapInterval;
	
	public LinksToPresence(String[] args) {
		super(args);
	}

	@Override
	protected void initOptions() {
		options.addOption(null, storeOutputOption, true, "write new traces to this store");
		options.addOption(null, presenceOption, true, "name of presence trace");
		options.addOption(null, linksOption, true, "name of links trace");
		options.addOption(null, snapIntervalOption, true, "snapshot interval");
	}

	@Override
	protected void parseArgs(CommandLine cli, String[] args)
			throws ParseException, ArrayIndexOutOfBoundsException,
			HelpException {
		origStoreFile = new File(args[0]);
		destStoreFile = new File(cli.getOptionValue(storeOutputOption,args[0]));
		presenceName = cli.getOptionValue(presenceOption, GraphStore.defaultPresenceName);
		linksName = cli.getOptionValue(linksOption, GraphStore.defaultLinksName);
		snapInterval = (Long) cli.getParsedOptionValue(snapIntervalOption);
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
		
		Trace links = getTrace(origStore,linksName);
		if ( snapInterval == null )
			snapInterval = links.snapshotInterval();
		StatefulReader<LinkEvent,Link> linksReader = new GraphStore(origStore).getLinkReader(links);
		StatefulWriter<PresenceEvent,Presence> presenceWriter = new GraphStore(destStore).getPresenceWriter(presenceName, snapInterval);
		
		Converter converter = new LinksToPresenceConverter(presenceWriter, linksReader);
		converter.run();
		converter.close();
		
		destStore.close();
		if ( origStore != destStore )
			origStore.close();
	}

	@Override
	protected void setUsageString() {
		usageString = "[OPTIONS] STORE";
	}

}
