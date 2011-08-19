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
import ditl.Writer;
import ditl.graphs.*;

public class Filter extends GraphApp {

	private File origStoreFile;
	private File destStoreFile;
	private String traceName;
	private String destName;
	private Set<Integer> group;
	
	public Filter(String[] args) {
		super(args);
	}

	@Override
	protected void initOptions() {
		options.addOption(null, storeOutputOption, true, "write new traces to this store");
	}

	@Override
	protected void parseArgs(CommandLine cli, String[] args)
			throws ParseException, ArrayIndexOutOfBoundsException,
			HelpException {
		origStoreFile = new File(args[0]);
		destStoreFile = new File(cli.getOptionValue(storeOutputOption,args[0]));
		traceName = args[1];
		destName = args[2];
		
		group = new HashSet<Integer>();
		String[] ranges = args[3].split(":");
		for ( String range : ranges ){
			String[] bounds = range.split("-");
			if ( bounds.length == 1 )
				group.add( Integer.parseInt(bounds[0]) );
			else {
				for ( Integer i = Integer.parseInt(bounds[0]); i<=Integer.parseInt(bounds[1]); ++i){
					group.add(i);
				}
			}
		}
		
	}

	@Override
	protected void run() throws IOException, MissingTraceException {
		Store origStore = Store.open(origStoreFile);
		WritableStore destStore = WritableStore.open(destStoreFile);
		
		filter(origStore, destStore);
		
		origStore.close();
		destStore.close();
	}
	
	private void filter(Store orig, WritableStore dest) throws IOException, MissingTraceException {
		Trace trace = getTrace(orig,traceName);
		String type = trace.type();
		long interval = trace.snapshotInterval();
		Converter converter = null;
		if ( trace.isStateful() ){
			if ( type.equals(GraphStore.presenceType) ){
				StatefulReader<PresenceEvent,Presence> reader = new GraphStore(orig).getPresenceReader(trace);
				StatefulWriter<PresenceEvent,Presence> writer = new GraphStore(dest).getPresenceWriter(destName, interval);
				converter = new StatefulFilterConverter<PresenceEvent,Presence>(writer,reader,
						PresenceEvent.groupMatcher(group), Presence.groupMatcher(group));
			} else if (type.equals(GraphStore.linksType) ){
				StatefulReader<LinkEvent,Link> reader = new GraphStore(orig).getLinkReader(trace);
				StatefulWriter<LinkEvent,Link> writer = new GraphStore(dest).getLinkWriter(destName, interval);
				converter = new StatefulFilterConverter<LinkEvent,Link>(writer,reader, 
						LinkEvent.internalLinkEventMatcher(group), Link.internalLinkMatcher(group));
			} else if ( type.equals(GraphStore.edgesType) ){
				StatefulReader<EdgeEvent,Edge> reader = new GraphStore(orig).getEdgeReader(trace);
				StatefulWriter<EdgeEvent,Edge> writer = new GraphStore(dest).getEdgeWriter(destName, interval);
				converter = new StatefulFilterConverter<EdgeEvent,Edge>(writer,reader,
						EdgeEvent.internalEdgeEventMatcher(group), Edge.internalEdgeMatcher(group));
			} else if ( type.equals(GraphStore.movementType) ){
				MovementTrace movement = new MovementTrace(trace);
				StatefulReader<MovementEvent,Movement> reader = new GraphStore(orig).getMovementReader(trace);
				StatefulWriter<MovementEvent,Movement> writer = new GraphStore(dest).getMovementWriter(destName, interval);
				writer.setProperty(MovementTrace.minXKey, movement.minX());
				writer.setProperty(MovementTrace.maxXKey, movement.maxX());
				writer.setProperty(MovementTrace.minYKey, movement.minY());
				writer.setProperty(MovementTrace.maxYKey, movement.maxY());
				converter = new StatefulFilterConverter<MovementEvent,Movement>(writer,reader,
						MovementEvent.groupMatcher(group), Movement.groupMatcher(group));
			}
		} else {
			if (type.equals(GraphStore.beaconsType) ){
				Reader<Edge> reader = new GraphStore(orig).getBeaconsReader(trace);
				Writer<Edge> writer = new GraphStore(dest).getBeaconsWriter(destName);
				converter = new FilterConverter<Edge>(writer,reader, Edge.internalEdgeMatcher(group));
			}
		}
		if ( converter != null ){
			converter.run();
			converter.close();
		}
	}
	

	@Override
	protected void setUsageString() {
		usageString = "[OPTIONS] STORE TRACE_NAME DEST_NAME GROUP_COMPOSITION";
	}
	
}
