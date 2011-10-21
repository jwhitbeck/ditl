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
package ditl.cli;

import java.io.*;

import org.apache.commons.cli.*;

import ditl.*;
import ditl.Store.*;
import ditl.WritableStore.AlreadyExistsException;

public class Truncate extends ConvertApp {

	private String orig_trace_name;
	private String dest_trace_name;
	private long minTime;
	private long maxTime;
	
	public final static String PKG_NAME = null;
	public final static String CMD_NAME = "trunc";
	public final static String CMD_ALIAS = null;
	
	@Override
	protected void parseArgs(CommandLine cli, String[] args)
			throws ParseException, ArrayIndexOutOfBoundsException,
			HelpException {
		if ( args.length == 5 ){
			super.parseArgs(cli, args);
			orig_trace_name = args[1];
			dest_trace_name = args[2];
			minTime = Long.parseLong(args[3]);
			maxTime = Long.parseLong(args[4]);
		} else {
			orig_store_file = new File(args[0]);
			dest_store_file = new File(args[1]);
			minTime = Long.parseLong(args[2]);
			maxTime = Long.parseLong(args[3]);
			force = cli.hasOption(forceOption);
		}
	}

	@Override
	protected void run() throws IOException, NoSuchTraceException, AlreadyExistsException, LoadTraceException {
		if ( orig_trace_name != null ){
			Trace<?> orig_trace = orig_store.getTrace(orig_trace_name);
			Trace<?> dest_trace = dest_store.newTrace(dest_trace_name, orig_trace.type(), force);
			truncate(dest_trace, orig_trace);
		} else {
			for ( Trace<?> orig_trace : orig_store.listTraces() ){
				Trace<?> dest_trace = dest_store.newTrace(orig_trace.name(), orig_trace.type(), force);
				truncate ( dest_trace, orig_trace );
			}
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void truncate(Trace<?> dest, Trace<?> orig) throws IOException {
		long min_time = minTime * orig.ticsPerSecond();
		long max_time = maxTime * orig.ticsPerSecond();
		Converter truncater;
		if ( orig.isStateful() ){
			truncater = new StatefulSubtraceConverter((StatefulTrace<?,?>)dest, 
					(StatefulTrace<?,?>)orig, min_time, max_time);
		} else {
			truncater = new SubtraceConverter(dest, orig, min_time, max_time);
		}
		truncater.convert();
	}
	

	@Override
	protected String getUsageString() {
		return "\t[OPTIONS] STORE NEWSTORE BEGIN END\n\t[OPTIONS] STORE ORIG_TRACE DEST_TRACE BEGIN END";
	}
	
}
