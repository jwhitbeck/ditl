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
package ditl;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import org.reflections.Reflections;
import org.reflections.scanners.TypeAnnotationsScanner;

public abstract class Store {
	
	final protected static String snapshotsFile = "snapshots";
	final protected static String infoFile = "info";
	final protected static String traceFile = "trace";
	
	protected String separator = "/";
	
	final Map<String,Trace<?>> traces = new HashMap<String,Trace<?>>();
	final static Map<String,Class<? extends Trace<?>>> type_class_map = buildTypeClassMap();
	
	private Set<Reader<?>> openReaders = new HashSet<Reader<?>>();
	private boolean closing = false;
	
	@SuppressWarnings("serial")
	public static class NoSuchTraceException extends Exception {
		private String trace_name; 
		public NoSuchTraceException(String traceName){ trace_name = traceName;}
		@Override
		public String toString(){
			return "Error! Could not find trace '"+trace_name+"'";
		}
	}
	
	@SuppressWarnings("serial")
	public static class LoadTraceException extends Exception {
		private String _name; 
		public LoadTraceException(String name){ _name = name;}
		@Override
		public String toString(){
			return "Error! Failed to load trace '"+_name+"'";
		}
	}
	
	@SuppressWarnings("unchecked")
	private static Map<String,Class<? extends Trace<?>>> buildTypeClassMap() {
		Map<String,Class<? extends Trace<?>>> type_class = new HashMap<String,Class<? extends Trace<?>>>();
		if ( type_class.isEmpty() ){
			Set<Class<?>> traceClasses = new Reflections("ditl", new TypeAnnotationsScanner()).
					getTypesAnnotatedWith(Trace.Type.class);
			for ( Class<?> klass : traceClasses ){
				if ( Trace.class.isAssignableFrom(klass) ){
					type_class.put(klass.getAnnotation(Trace.Type.class).value(), 
							(Class<? extends Trace<?>>)klass);
				}
			}
		}
		return type_class;
	}
	
	String traceFile(String name){
		return name+separator+traceFile;
	}
	
	String infoFile(String name){
		return name+separator+infoFile;
	}
	
	String snapshotsFile(String name){
		return name+separator+snapshotsFile;
	}
	
	
	public Collection<Trace<?>> listTraces() {
		return traces.values();
	}
	
	public List<Trace<?>> listTraces(String type){
		try {
			return listTraces(getTraceClass(type));
		} catch ( LoadTraceException e){
			return Collections.emptyList();
		}
	}
	
	public List<Trace<?>> listTraces(Class<? extends Trace<?>> klass){
		List<Trace<?>> list = new LinkedList<Trace<?>>();
		for ( Trace<?> trace : traces.values() )
			if ( klass.equals(trace.getClass()))
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
	
	public Trace<?> getTrace(String name) throws NoSuchTraceException {
		Trace<?> trace = traces.get(name);
		if ( trace == null ) throw new NoSuchTraceException(name);
		return trace;
	}
	
	public abstract InputStream getInputStream (String name) throws IOException;
	
	public String getTraceResource(Trace<?> trace, String resource) throws IOException{
		return trace.name() + separator + resource;
	}
	
	PersistentMap readTraceInfo(String path) throws IOException {
		PersistentMap info = new PersistentMap();
		info.read(getInputStream(infoFile(path)));
		return info;
	}
	
	public static Store open(File...files) throws IOException {
		switch ( files.length ){
		case 0: return new ClassPathStore();
		case 1: if ( files[0].isDirectory() )
					return new DirectoryStore(files[0]);
				return new JarStore(files[0]);
		default: return new MultiStore(files);
		}
	}
	
	void notifyClose(Reader<?> reader){
		if ( ! closing )
			openReaders.remove(reader);
	}
	
	void notifyOpen(Reader<?> reader){
		openReaders.add(reader);
	}
	
	public void close() throws IOException {
		closing = true;
		for ( Reader<?> reader : openReaders )
			reader.close();
		openReaders.clear();
		closing = false;
	}
	
	Class<? extends Trace<?>> getTraceClass(String type) throws LoadTraceException{
		buildTypeClassMap();
		if ( ! type_class_map.containsKey(type) ){
			throw new LoadTraceException(type);
		}
		return type_class_map.get(type);
	}
	
	public void loadTrace(String name) throws IOException, LoadTraceException {
		PersistentMap _info = readTraceInfo(name);
		Trace<?> trace = buildTrace(name, _info, type_class_map.get(_info.get(Trace.typeKey)));
		traces.put(name, trace);
	}
	
	Trace<?> buildTrace(String name, PersistentMap info, Class<? extends Trace<?>> klass) throws LoadTraceException {
		try {
			Constructor<?> ctor = klass.getConstructor(new Class[]{Store.class, String.class, PersistentMap.class});
			info.put(Trace.typeKey, klass.getAnnotation(Trace.Type.class).value());
			Trace<?> trace = (Trace<?>)ctor.newInstance(this, name, info);
			return trace;
		} catch ( Exception e ){
			throw new LoadTraceException(name);
		}
	}
}
