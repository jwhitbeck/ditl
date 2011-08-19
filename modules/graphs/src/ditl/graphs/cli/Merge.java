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

public class Merge extends GraphApp {

	private File origStoreFile;
	private File destStoreFile;
	private String mergedName;
	private String[] traceNames;
	private String type;
	private boolean stateful;
	private long snapInterval;
	
	public Merge(String[] args) {
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
		
		mergedName = args[1];
		traceNames = new String[args.length-2];
		for ( int i=2; i<args.length; ++i)
			traceNames[i-2] = args[i];
	}

	@Override
	protected void run() throws IOException, MissingTraceException {
		Store origStore = Store.open(origStoreFile);
		WritableStore destStore = WritableStore.open(destStoreFile);
		
		List<Trace> traces = new LinkedList<Trace>();
		for ( String traceName : traceNames )
			traces.add(getTrace(origStore,traceName));
		
		// check types
		Trace t = traces.get(0);
		type = t.type();
		stateful = t.isStateful();
		snapInterval = 1L;
		if ( stateful )
			snapInterval = t.snapshotInterval(); 
		for ( Iterator<Trace> i = traces.iterator(); i.hasNext(); ){
			t = i.next();
			if ( ! t.type().equals(type) )
				i.remove();
		}
		
		merge(traces, origStore, destStore);
		
		
		origStore.close();
		destStore.close();
	}
	
	private void merge(List<Trace> traces, Store orig, WritableStore dest) throws IOException {
		Converter converter = null;
		if ( stateful ){
			if ( type.equals(GraphStore.presenceType) ){
				Collection<StatefulReader<PresenceEvent,Presence>> toMerge = new LinkedList<StatefulReader<PresenceEvent,Presence>>();
				for ( Trace t : traces)
					toMerge.add(new GraphStore(orig).getPresenceReader(t));
				StatefulWriter<PresenceEvent,Presence> writer = new GraphStore(dest).getPresenceWriter(mergedName, snapInterval);
				converter = new StatefulMergeConverter<PresenceEvent,Presence>(writer, toMerge);
			} else if (type.equals(GraphStore.linksType) ){
				Collection<StatefulReader<LinkEvent,Link>> toMerge = new LinkedList<StatefulReader<LinkEvent,Link>>();
				for ( Trace t : traces)
					toMerge.add(new GraphStore(orig).getLinkReader(t));
				StatefulWriter<LinkEvent,Link> writer = new GraphStore(dest).getLinkWriter(mergedName, snapInterval);
				converter = new StatefulMergeConverter<LinkEvent,Link>(writer, toMerge);
			} else if ( type.equals(GraphStore.edgesType) ){
				Collection<StatefulReader<EdgeEvent,Edge>> toMerge = new LinkedList<StatefulReader<EdgeEvent,Edge>>();
				for ( Trace t : traces)
					toMerge.add(new GraphStore(orig).getEdgeReader(t));
				StatefulWriter<EdgeEvent,Edge> writer = new GraphStore(dest).getEdgeWriter(mergedName, snapInterval);
				converter = new StatefulMergeConverter<EdgeEvent,Edge>(writer,toMerge);
			} else if ( type.equals(GraphStore.movementType) ){
				Collection<StatefulReader<MovementEvent,Movement>> toMerge = new LinkedList<StatefulReader<MovementEvent,Movement>>();
				Bounds bounds = new Bounds();
				for ( Trace t : traces){
					MovementTrace movement = new MovementTrace(traces.get(0));
					toMerge.add(new GraphStore(orig).getMovementReader(t));
					bounds.update(new Point(movement.minX(), movement.minY()));
					bounds.update(new Point(movement.maxX(), movement.maxY()));
				}
				StatefulWriter<MovementEvent,Movement> writer = new GraphStore(dest).getMovementWriter(mergedName, snapInterval);
				bounds.writeToTrace(writer);
				converter = new StatefulMergeConverter<MovementEvent,Movement>(writer, toMerge);
			}
		} else {
			if (type.equals(GraphStore.beaconsType) ){
				Collection<Reader<Edge>> toMerge = new LinkedList<Reader<Edge>>();
				for ( Trace t : traces )
					toMerge.add(new GraphStore(orig).getBeaconsReader(t));
				Writer<Edge> writer = new GraphStore(dest).getBeaconsWriter(mergedName);
				converter = new MergeConverter<Edge>(writer, toMerge);
			}
		}
		if ( converter != null ){
			converter.run();
			converter.close();
		}
	}
	

	@Override
	protected void setUsageString() {
		usageString = "[OPTIONS] STORE MERGED_NAME TRACE1 [TRACE2 ...]";
	}
	
}
