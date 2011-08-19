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

public abstract class WritableStore extends Store {
	
	private Set<Writer<?>> openWriters = new HashSet<Writer<?>>();
	
	public abstract void deleteFile(String name) throws IOException;
	public abstract void deleteTrace(Trace trace) throws IOException;
	public abstract OutputStream getOutputStream (String name) throws IOException;
	
	public void putFile(File file, String name) throws IOException{
		copy ( new FileInputStream(file), getOutputStream(name) );
	}
	
	public void copy(InputStream ins, OutputStream outs) throws IOException{
		BufferedOutputStream out = new BufferedOutputStream(outs);
		BufferedInputStream in = new BufferedInputStream(ins);
		int c;
		while ( (c=in.read()) != -1 ){
			out.write(c);
		}
		in.close();
		out.close();
	}

	public <E, S> StatefulWriter<E, S> getStatefulWriter(String name,
			StateUpdater<E, S> stateUpdater, long snapInterval) throws IOException {
		StatefulWriter<E,S> writer = new StatefulWriter<E,S>(this,getOutputStream(traceFile(name)),
												getOutputStream(infoFile(name)),
												getOutputStream(snapshotsFile(name)),
												snapInterval, stateUpdater, new PersistentMap());
		openWriters.add(writer);
		return writer;
	}

	public <I> Writer<I> getWriter(String name) throws IOException {
		Writer<I> writer = new Writer<I>(this, getOutputStream(traceFile(name)), 
				getOutputStream(infoFile(name)), new PersistentMap());
		openWriters.add(writer);
		return writer;
	}
	
	void notifyClose(Writer<?> writer) throws IOException {
		openWriters.remove(writer);
		refresh();
	}
	
	@Override
	public void close() throws IOException {
		super.close();
		for ( Iterator<Writer<?>> i = openWriters.iterator(); i.hasNext(); ){
			Writer<?> writer = i.next();
			i.remove();
			writer.close();
		}
	}
	
	public static WritableStore open(File file) throws IOException {
		if ( file.isDirectory() )
			return new DirectoryStore(file);
		return new JarDirectoryStore(file); 
	}
	
	public void copyTrace(Store store, Trace trace ) throws IOException {
		String[] files = new String[]{
				infoFile(trace.name()), 
				snapshotsFile(trace.name()),
				traceFile(trace.name())};
		
		for ( String file : files ){
			InputStream in = store.getInputStream(file);
			OutputStream out = getOutputStream(file);
			copy(in,out);
		}
	}
	
	abstract void refresh() throws IOException;
}
