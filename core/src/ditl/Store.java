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
package ditl;

import java.io.*;
import java.util.*;

public abstract class Store {
	
	final protected static String snapshotsFile = "snapshots";
	final protected static String infoFile = "info";
	final protected static String traceFile = "trace";
	
	protected String separator = "/";
	
	final HashMap<String,Trace> traces = new HashMap<String,Trace>();
	
	private Set<Reader<?>> openReaders = new HashSet<Reader<?>>();
	
	String traceFile(String name){
		return name+separator+traceFile;
	}
	
	String infoFile(String name){
		return name+separator+infoFile;
	}
	
	String snapshotsFile(String name){
		return name+separator+snapshotsFile;
	}
	
	public <E,S> StatefulReader<E,S> getStatefulReader(Trace trace,
			ItemFactory<E> eventFactory, ItemFactory<S> stateFactory,
			StateUpdater<E,S> stateUpdater, int priority, long offset) throws IOException {
		
		String snapshotsFile = snapshotsFile(trace.name());
		String traceFile = traceFile(trace.name());
		Reader<S> snap_iterator = new Reader<S>(this,
				getStreamOpener(snapshotsFile), Math.max(trace.snapshotInterval(), trace.maxUpdateInterval()),
				stateFactory, Runner.defaultPriority, offset);
		StatefulReader<E,S> iterator;
		iterator = new StatefulReader<E,S>(this,getStreamOpener(traceFile),
				trace.maxUpdateInterval(), eventFactory,
				snap_iterator, stateUpdater, priority, offset );
		iterator.setTrace(trace);
		openReaders.add(iterator);
		return iterator;
	}
	
	public <E,S> StatefulReader<E,S> getStatefulReader(Trace trace,
			ItemFactory<E> eventFactory, ItemFactory<S> stateFactory,
			StateUpdater<E,S> stateUpdater, int priority) throws IOException {
		
		return getStatefulReader(trace,eventFactory,stateFactory,stateUpdater,priority, 0L);
	}
	
	public <E,S> StatefulReader<E,S> getStatefulReader(Trace trace,
			ItemFactory<E> eventFactory, ItemFactory<S> stateFactory,
			StateUpdater<E,S> stateUpdater) throws IOException {
		
		return getStatefulReader(trace,eventFactory,stateFactory,stateUpdater,trace.defaultPriority(), 0L);
	}
	
	public <I> Reader<I> getReader(Trace trace, ItemFactory<I> factory, int priority, long offset) throws IOException {
		String traceFile = traceFile(trace.name());
		Reader<I> iterator = new Reader<I>(this, getStreamOpener(traceFile), 
				trace.maxUpdateInterval(), factory, priority, offset);
		iterator.setTrace(trace);
		openReaders.add(iterator);
		return iterator;
	}
	
	public <I> Reader<I> getReader(Trace trace, ItemFactory<I> factory, int priority) throws IOException{
		return getReader(trace,factory,priority,0L);
	}
	
	public <I> Reader<I> getReader(Trace trace, ItemFactory<I> factory) throws IOException{
		return getReader(trace,factory, trace.defaultPriority(), 0L);
	}
	
	public Collection<Trace> listTraces() {
		return traces.values();
	}
	
	public List<Trace> listTraces(String type){
		List<Trace> list = new LinkedList<Trace>();
		for ( Trace trace : traces.values() )
			if ( trace.type() != null )
				if ( trace.type().equals(type) )
					list.add(trace);
		return list;
	}
	
	Reader.InputStreamOpener getStreamOpener(final String name){
		return new Reader.InputStreamOpener(){
			@Override
			public InputStream open() throws IOException {
				return getInputStream(name);
			}
		};
	}
	
	public abstract boolean hasFile(String name);
	
	public boolean hasTrace(String name){
		return traces.containsKey(name);
	}
	
	public Trace getTrace(String name){
		return traces.get(name);
	}
	
	public abstract InputStream getInputStream (String name) throws IOException;
	
	public String getTraceResource(Trace trace, String resource) throws IOException{
		return trace.name() + separator + resource;
	}
	
	PersistentMap readTraceInfo(String path) throws IOException {
		PersistentMap info = new PersistentMap();
		info.read(getInputStream(infoFile(path)));
		return info;
	}
	
	public static Store open(File file) throws IOException {
		if ( file == null )
			return new JarStore();
		if ( file.isDirectory() )
			return new DirectoryStore(file);
		return new JarStore(file);
	}
	
	void notifyClose(Reader<?> reader){
		openReaders.remove(reader);
	}
	
	public void close() throws IOException {
		for ( Iterator<Reader<?>> i = openReaders.iterator(); i.hasNext(); ){
			Reader<?> reader = i.next();
			i.remove();
			reader.close();
		}
	}
	
	public Trace loadTrace(String name) throws IOException {
		final PersistentMap info = readTraceInfo(name);
		info.put(Trace.nameKey, name);
		Trace trace = new Trace(){
			@Override
			public String getValue(String key) {
				return info.get(key);
			}
		};
		traces.put(name, trace);
		return trace;
	}
}
