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
package ditl.cli;

import java.io.*;
import java.util.*;

import org.apache.commons.cli.*;

import ditl.*;
import ditl.Store.LoadTraceException;
import ditl.Store.NoSuchTraceException;
import ditl.WritableStore.AlreadyExistsException;

public class Merge extends WriteApp {

	private String[] orig_store_names;
	private Store[] orig_stores;
	
	public final static String PKG_NAME = null;
	public final static String CMD_NAME = "merge";
	public final static String CMD_ALIAS = null;
	
	@Override
	protected void parseArgs(CommandLine cli, String[] args)
			throws ParseException, ArrayIndexOutOfBoundsException,
			HelpException {
		super.parseArgs(cli, args);
		orig_store_names = new String[args.length-1];
		for ( int i=1; i<args.length; ++i)
			orig_store_names[i-1] = args[i];
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected void run() throws IOException, NoSuchTraceException, AlreadyExistsException, LoadTraceException {
		for ( String traceName : getCommonTraceNames() ){
			List<Trace<?>> traces = new LinkedList<Trace<?>>();
			for ( Store store : orig_stores )
				traces.add(store.getTrace(traceName));

			Trace<?> ref_trace = traces.get(0);
			String type = ref_trace.type();
			boolean stateful = ref_trace.isStateful();
			
			Trace<?> merged = _store.newTrace(traceName, type, stateful);
			
			Converter merger;
			if ( stateful ){
				merger = new StatefulMergeConverter((StatefulTrace<?,?>)merged, traces);
			} else {
				merger = new MergeConverter(merged, traces);
			}
			merger.convert();
		}
	}	

	@Override
	protected String getUsageString() {
		return "[OPTIONS] MERGED_STORE STORE1 [STORE2 ...]";
	}
	
	@Override
	protected void init() throws IOException {
		super.init();
		orig_stores = new Store[orig_store_names.length];
		for(int i=0; i<orig_store_names.length; ++i)
			orig_stores[i] = Store.open(new File(orig_store_names[i]));
	}
	
	@Override
	protected void close() throws IOException {
		super.close();
		for ( Store store : orig_stores )
			store.close();
	}
	
	private List<String> getCommonTraceNames(){
		List<String> names = new LinkedList<String>();
		for ( Trace<?> trace : orig_stores[0].listTraces() ){
			String name = trace.name();
			boolean ok = true;
			for ( int i=1; i<orig_stores.length; ++i )
				if ( ! orig_stores[i].hasTrace(name) ){
					ok = false;
					break;
				}
			if ( ok ) names.add(name);
		}
		return names;
	}
	
}
