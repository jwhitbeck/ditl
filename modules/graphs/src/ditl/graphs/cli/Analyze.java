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
import java.util.*;

import org.apache.commons.cli.*;

import ditl.*;
import ditl.Reader;
import ditl.graphs.*;

public class Analyze extends GraphApp {
	
	final static String nodeCountOption = "node-count";
	final static String transitTimesOption = "transit-times";
	final static String timeToFirstContactOption = "first-contact-time";
	final static String numContactsOption = "num-contacts";
	final static String nodeDegreeOption = "node-degree";
	final static String contactsOption = "contacts";
	final static String interContactsOption = "inter-contacts";
	final static String anyContactsOption = "any-contacts";
	final static String interAnyContactsOption = "inter-any-contacts";
	final static String clusteringOption = "clustering";
	final static String numCCOption = "ccs";
	
	private File storeFile;
	private String outputFile;
	private String linksName;
	private String presenceName;
	private String ccName;
	private ReportFactory<?> factory;
	
	public Analyze(String[] args) {
		super(args);
	}

	@Override
	protected void initOptions() {
		options.addOption(null, presenceOption, true, "name of presence trace to use (default: "+GraphStore.defaultPresenceName+")");
		options.addOption(null, linksOption, true, "name of links trace (default: "+GraphStore.defaultLinksName+")");
		options.addOption(null, linksOption, true, "name of connected components trace (default: "+GraphStore.defaultConnectedComponentsName+")");
		options.addOption(null, outputOption, true, "name of file to write output to");
		OptionGroup reportGroup = new OptionGroup();
		reportGroup.addOption(new Option(null, nodeCountOption, false, "node count report") );
		reportGroup.addOption(new Option(null, transitTimesOption, false, "transit times report") );
		reportGroup.addOption(new Option(null, timeToFirstContactOption, false, "time to first contact report") );
		reportGroup.addOption(new Option(null, numContactsOption, false, "number of contacs distribution") );
		reportGroup.addOption(new Option(null, nodeDegreeOption, false, "node degree distribution over time") );
		reportGroup.addOption(new Option(null, contactsOption, false, "contact time distribution") );
		reportGroup.addOption(new Option(null, interContactsOption, false, "inter-contact time distribution") );
		reportGroup.addOption(new Option(null, anyContactsOption, false, "any-contact time distribution") );
		reportGroup.addOption(new Option(null, interAnyContactsOption, false, "inter-any-contact time distribution") );
		reportGroup.addOption(new Option(null, clusteringOption, false, "clustering coefficient distribution over time") );
		reportGroup.addOption(new Option(null, numCCOption, false, "distribution of connected component sizes over time") );
		reportGroup.setRequired(true);
		options.addOptionGroup(reportGroup);
	}

	@Override
	protected void parseArgs(CommandLine cli, String[] args)
			throws ParseException, ArrayIndexOutOfBoundsException,
			HelpException {
		
		linksName = cli.getOptionValue(linksOption, GraphStore.defaultLinksName);
		presenceName = cli.getOptionValue(presenceOption, GraphStore.defaultPresenceName);
		ccName = cli.getOptionValue(ccOption, GraphStore.defaultConnectedComponentsName);
		storeFile = new File(args[0]);
		outputFile = cli.getOptionValue(outputOption);
	
		
		if ( cli.hasOption(nodeCountOption) ){
			factory = NodeCountReport.factory();
		} else if ( cli.hasOption(transitTimesOption) ){
			factory = TransitTimesReport.factory();
		} else if ( cli.hasOption(timeToFirstContactOption) ){
			factory = TimeToFirstContactReport.factory();
		} else if ( cli.hasOption(numContactsOption) ){
			factory = NumberContactsReport.factory();
		} else if ( cli.hasOption(nodeDegreeOption) ){
			factory = NodeDegreeReport.factory();
		} else if ( cli.hasOption(contactsOption) ){
			factory = ContactTimesReport.factory(true);
		} else if ( cli.hasOption(interContactsOption) ){
			factory = ContactTimesReport.factory(false);
		} else if ( cli.hasOption(anyContactsOption) ){
			factory = AnyContactTimesReport.factory(true);
		} else if ( cli.hasOption(interAnyContactsOption) ){
			factory = AnyContactTimesReport.factory(false);
		} else if ( cli.hasOption(clusteringOption) ){
			factory = ClusteringCoefficientReport.factory(true);
		} else if ( cli.hasOption(numCCOption) ){
			factory = ConnectedComponentsReport.factory();
		}
	}
	
	
	@Override
	protected void run() throws IOException, MissingTraceException {
		Store store = Store.open(storeFile);
		OutputStream out = System.out;
		if ( outputFile != null )
			out = new FileOutputStream( outputFile );
		
		GraphStore gStore = new GraphStore(store);
		Report report = factory.getNew(out);
		
		Long minTime=null, maxTime=null, incrTime=null;
		List<Reader<?>> readers = new LinkedList<Reader<?>>();
		
		if ( report instanceof PresenceHandler ){
			Trace presence = getTrace(store,presenceName);
			StatefulReader<PresenceEvent,Presence> presenceReader = gStore.getPresenceReader(presence);
			
			Bus<PresenceEvent> presenceEventBus = new Bus<PresenceEvent>();
			Bus<Presence> presenceBus = new Bus<Presence>();
			presenceReader.setBus(presenceEventBus);
			presenceReader.setStateBus(presenceBus);
			
			PresenceHandler ph = (PresenceHandler)report;
			presenceBus.addListener(ph.presenceListener());
			presenceEventBus.addListener(ph.presenceEventListener());
			
			readers.add(presenceReader);
			
			minTime = presence.minTime();
			maxTime = presence.maxTime();
			incrTime = presence.maxUpdateInterval();
		}		
		
		if ( report instanceof LinkHandler ){
			Trace links = getTrace(store,linksName);			
			StatefulReader<LinkEvent,Link> linksReader = gStore.getLinkReader(links);
			
			Bus<LinkEvent> linkEventBus = new Bus<LinkEvent>();
			Bus<Link> linkBus = new Bus<Link>();
			linksReader.setBus(linkEventBus);
			linksReader.setStateBus(linkBus);

			LinkHandler lh = (LinkHandler)report;
			linkBus.addListener(lh.linkListener());
			linkEventBus.addListener(lh.linkEventListener());
			
			readers.add(linksReader);
			
			if ( minTime == null ) minTime = links.minTime();
			if ( maxTime == null ) maxTime = links.maxTime();
			incrTime = links.maxUpdateInterval();
		}
		
		if ( report instanceof GroupHandler ){
			Trace groups = getTrace(store,ccName);
			StatefulReader<GroupEvent,Group> groupReader = gStore.getGroupReader(groups);
			
			Bus<GroupEvent> groupEventBus = new Bus<GroupEvent>();
			Bus<Group> groupBus = new Bus<Group>();
			groupReader.setBus(groupEventBus);
			groupReader.setStateBus(groupBus);
			
			GroupHandler gh = (GroupHandler)report;
			groupEventBus.addListener(gh.groupEventListener());
			groupBus.addListener(gh.groupListener());
			
			readers.add(groupReader);
			
			if ( minTime == null ) minTime = groups.minTime();
			if ( maxTime == null ) maxTime = groups.maxTime();
			incrTime = groups.maxUpdateInterval();
		}

		Runner runner = new Runner(incrTime, minTime, maxTime);
		for ( Reader<?> reader : readers )
			runner.addGenerator(reader);
		runner.run();
		
		report.finish();
		
		store.close();
	}

	@Override
	protected void setUsageString() {
		usageString = "Analyze [OPTIONS] STORE";
	}
	
}
