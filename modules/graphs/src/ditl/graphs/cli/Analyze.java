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
import java.util.*;

import org.apache.commons.cli.*;

import ditl.*;
import ditl.Store.NoSuchTraceException;
import ditl.cli.ExportApp;
import ditl.graphs.*;

public class Analyze extends ExportApp {
	
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
	final static String groupSizeOption = "group-size";
	
	private GraphOptions graph_options = new GraphOptions(GraphOptions.PRESENCE, GraphOptions.LINKS, GraphOptions.GROUPS, GraphOptions.EDGES);
	private ReportFactory<?> factory;
	private Long min_time;
	private Long max_time;
	
	public final static String PKG_NAME = "graphs";
	public final static String CMD_NAME = "analyze";
	public final static String CMD_ALIAS = "a";
	
	
	@Override
	protected void initOptions() {
		super.initOptions();
		graph_options.setOptions(options);
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
		reportGroup.addOption(new Option(null, groupSizeOption, false, "distribution of group sizes over time") );
		reportGroup.setRequired(true);
		options.addOptionGroup(reportGroup);
		options.addOption(null, maxTimeOption, true, "Ignore event after <arg> seconds");
		options.addOption(null, minTimeOption, true, "Ignore event before <arg> seconds");
	}

	@Override
	protected void parseArgs(CommandLine cli, String[] args)
			throws ParseException, ArrayIndexOutOfBoundsException,
			HelpException {
		
		super.parseArgs(cli, args);
		graph_options.parse(cli);
		
		if ( cli.hasOption(nodeCountOption) ){
			factory = new NodeCountReport.Factory();
		} else if ( cli.hasOption(transitTimesOption) ){
			factory = new TransitTimesReport.Factory();
		} else if ( cli.hasOption(timeToFirstContactOption) ){
			factory = new TimeToFirstContactReport.Factory();
		} else if ( cli.hasOption(numContactsOption) ){
			factory = new NumberContactsReport.Factory();
		} else if ( cli.hasOption(nodeDegreeOption) ){
			factory = new NodeDegreeReport.Factory();
		} else if ( cli.hasOption(contactsOption) ){
			factory = new ContactTimesReport.Factory(true);
		} else if ( cli.hasOption(interContactsOption) ){
			factory = new ContactTimesReport.Factory(false);
		} else if ( cli.hasOption(anyContactsOption) ){
			factory = new AnyContactTimesReport.Factory(true);
		} else if ( cli.hasOption(interAnyContactsOption) ){
			factory = new AnyContactTimesReport.Factory(false);
		} else if ( cli.hasOption(clusteringOption) ){
			factory = new ClusteringCoefficientReport.Factory(true);
		} else if ( cli.hasOption(groupSizeOption) ){
			factory = new GroupSizeReport.Factory();
		}
		
		if ( cli.hasOption(minTimeOption) )
			min_time = Long.parseLong(cli.getOptionValue(minTimeOption));
		if ( cli.hasOption(maxTimeOption) )
			max_time = Long.parseLong(cli.getOptionValue(maxTimeOption));
	}
	
	
	@Override
	protected void run() throws IOException, NoSuchTraceException {
		Report report = factory.getNew(_out);
		
		Long minTime=null, maxTime=null, incrTime=null;
		List<Reader<?>> readers = new LinkedList<Reader<?>>();
		
		Long tps = null;
		
		if ( report instanceof PresenceTrace.Handler ){
			PresenceTrace presence = (PresenceTrace)_store.getTrace(graph_options.get(GraphOptions.PRESENCE));
			StatefulReader<PresenceEvent,Presence> presenceReader = presence.getReader();

			PresenceTrace.Handler ph = (PresenceTrace.Handler)report;
			presenceReader.stateBus().addListener(ph.presenceListener());
			presenceReader.bus().addListener(ph.presenceEventListener());
			
			readers.add(presenceReader);
			
			minTime = presence.minTime();
			maxTime = presence.maxTime();
			incrTime = presence.maxUpdateInterval();
			tps = presence.ticsPerSecond();
		}		
		
		if ( report instanceof LinkTrace.Handler ){
			LinkTrace links = (LinkTrace)_store.getTrace(graph_options.get(GraphOptions.LINKS));			
			StatefulReader<LinkEvent,Link> linksReader = links.getReader();

			LinkTrace.Handler lh = (LinkTrace.Handler)report;
			linksReader.stateBus().addListener(lh.linkListener());
			linksReader.bus().addListener(lh.linkEventListener());
			
			readers.add(linksReader);
			
			if ( minTime == null || links.minTime() > minTime ) minTime = links.minTime();
			if ( maxTime == null || links.maxTime() < maxTime ) maxTime = links.maxTime();
			incrTime = links.maxUpdateInterval();
			tps = links.ticsPerSecond();
		}
		
		if ( report instanceof EdgeTrace.Handler ){
			EdgeTrace edges = (EdgeTrace)_store.getTrace(graph_options.get(GraphOptions.EDGES));		
			StatefulReader<EdgeEvent,Edge> edgeReader = edges.getReader();

			EdgeTrace.Handler eh = (EdgeTrace.Handler)report;
			edgeReader.stateBus().addListener(eh.edgeListener());
			edgeReader.bus().addListener(eh.edgeEventListener());
			
			readers.add(edgeReader);
			
			if ( minTime == null || edges.minTime() > minTime ) minTime = edges.minTime();
			if ( maxTime == null || edges.maxTime() < maxTime ) maxTime = edges.maxTime();
			incrTime = edges.maxUpdateInterval();
			tps = edges.ticsPerSecond();
		}
		
		if ( report instanceof GroupTrace.Handler ){
			GroupTrace groups = (GroupTrace)_store.getTrace(graph_options.get(GraphOptions.GROUPS));
			StatefulReader<GroupEvent,Group> groupReader = groups.getReader();
			
			GroupTrace.Handler gh = (GroupTrace.Handler)report;
			groupReader.bus().addListener(gh.groupEventListener());
			groupReader.stateBus().addListener(gh.groupListener());
			
			readers.add(groupReader);
			
			if ( minTime == null || groups.minTime() > minTime ) minTime = groups.minTime();
			if ( maxTime == null || groups.maxTime() < maxTime ) maxTime = groups.maxTime();
			incrTime = groups.maxUpdateInterval();
			tps = groups.ticsPerSecond();
		}

		if ( min_time != null && max_time != null && tps != null){
			minTime = min_time * tps;
			maxTime = max_time * tps;
		}
		
		Runner runner = new Runner(incrTime, minTime, maxTime);
		for ( Reader<?> reader : readers )
			runner.addGenerator(reader);
		runner.run();
		
		if ( report instanceof StateTimeReport )
			((StateTimeReport)report).finish(maxTime);
		else
			report.finish();
	}
}
