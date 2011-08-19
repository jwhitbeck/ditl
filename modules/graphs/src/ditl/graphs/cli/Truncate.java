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
import ditl.Reader;
import ditl.Writer;
import ditl.graphs.*;

public class Truncate extends GraphApp {

	private File origStoreFile;
	private File destStoreFile;
	private String traceName;
	private long minTime;
	private long maxTime;
	
	public Truncate(String[] args) {
		super(args);
	}

	@Override
	protected void initOptions() {
		options.addOption(null, traceOption, true, "trace to truncate (if not specified, will truncate all traces in store)");
	}

	@Override
	protected void parseArgs(CommandLine cli, String[] args)
			throws ParseException, ArrayIndexOutOfBoundsException,
			HelpException {
		origStoreFile = new File(args[0]);
		destStoreFile = new File(args[1]);
		minTime = Long.parseLong(args[2]);
		maxTime = Long.parseLong(args[3]);
		traceName = cli.getOptionValue(traceOption);
	}

	@Override
	protected void run() throws IOException, MissingTraceException {
		Store origStore = Store.open(origStoreFile);
		WritableStore destStore = WritableStore.open(destStoreFile);
		if ( traceName == null ){
			for ( Trace trace : origStore.listTraces() ){
				truncate(trace, origStore, destStore);
			}
		} else {
			Trace trace = getTrace(origStore,traceName);
			truncate(trace, origStore, destStore);
		}
		
		origStore.close();
		destStore.close();
	}
	
	private void truncate(Trace trace, Store orig, WritableStore dest) throws IOException {
		String type = trace.type();
		String name = trace.name();
		long min_time = minTime * trace.ticsPerSecond();
		long max_time = maxTime * trace.ticsPerSecond();
		long interval = trace.snapshotInterval();
		Converter converter = null;
		if ( trace.isStateful() ){
			if ( type.equals(GraphStore.presenceType) ){
				StatefulReader<PresenceEvent,Presence> reader = new GraphStore(orig).getPresenceReader(trace);
				StatefulWriter<PresenceEvent,Presence> writer = new GraphStore(dest).getPresenceWriter(name, interval);
				converter = new StatefulSubtraceConverter<PresenceEvent,Presence>(writer,reader,min_time,max_time);
			} else if (type.equals(GraphStore.linksType) ){
				StatefulReader<LinkEvent,Link> reader = new GraphStore(orig).getLinkReader(trace);
				StatefulWriter<LinkEvent,Link> writer = new GraphStore(dest).getLinkWriter(name, interval);
				converter = new StatefulSubtraceConverter<LinkEvent,Link>(writer,reader,min_time,max_time);
			} else if ( type.equals(GraphStore.edgesType) ){
				StatefulReader<EdgeEvent,Edge> reader = new GraphStore(orig).getEdgeReader(trace);
				StatefulWriter<EdgeEvent,Edge> writer = new GraphStore(dest).getEdgeWriter(name, interval);
				converter = new StatefulSubtraceConverter<EdgeEvent,Edge>(writer,reader,min_time,max_time);
			} else if ( type.equals(GraphStore.movementType) ){
				MovementTrace movement = new MovementTrace(trace);
				StatefulReader<MovementEvent,Movement> reader = new GraphStore(orig).getMovementReader(trace);
				StatefulWriter<MovementEvent,Movement> writer = new GraphStore(dest).getMovementWriter(name, interval);
				writer.setProperty(MovementTrace.minXKey, movement.minX());
				writer.setProperty(MovementTrace.maxXKey, movement.maxX());
				writer.setProperty(MovementTrace.minYKey, movement.minY());
				writer.setProperty(MovementTrace.maxYKey, movement.maxY());
				converter = new StatefulSubtraceConverter<MovementEvent,Movement>(writer,reader,min_time,max_time);
			} else if ( type.equals(GraphStore.groupType) ){
				GroupTrace groups = new GroupTrace(trace);
				StatefulReader<GroupEvent,Group> reader = new GraphStore(orig).getGroupReader(trace);
				StatefulWriter<GroupEvent,Group> writer = new GraphStore(dest).getGroupWriter(name, interval);
				if ( groups.hasLabels() ){
					writer.setProperty(GroupTrace.labelsKey, groups.getValue(GroupTrace.labelsKey));
				}
				converter = new StatefulSubtraceConverter<GroupEvent,Group>(writer,reader,min_time,max_time);
			}
		} else {
			if (type.equals(GraphStore.beaconsType) ){
				Reader<Edge> reader = new GraphStore(orig).getBeaconsReader(trace);
				Writer<Edge> writer = new GraphStore(dest).getBeaconsWriter(name);
				converter = new SubtraceConverter<Edge>(writer,reader,min_time,max_time);
			}
		}
		if ( converter != null ){
			converter.run();
			converter.close();
		}
	}
	

	@Override
	protected void setUsageString() {
		usageString = "[OPTIONS] STORE NEWSTORE BEGIN END";
	}
	
}
