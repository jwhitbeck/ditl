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

import java.io.IOException;
import java.util.Collection;

import org.apache.commons.cli.*;

import ditl.*;
import ditl.Store.NoSuchTraceException;

public class EventCounter extends ReadOnlyApp {
	
	private Double d_begin = null;
	private Double d_end = null;
	private String trace_name;
	
	public final static String PKG_NAME = null;
	public final static String CMD_NAME = "count";
	public final static String CMD_ALIAS = null;
	
	
	@Override
	protected void initOptions(){
		super.initOptions();
		options.addOption(null, maxTimeOption, true, "Maximum time");
		options.addOption(null, minTimeOption, true, "Minimum time");
	}
	
	@Override
	protected void parseArgs(CommandLine cli, String[] args) throws ParseException, HelpException, ArrayIndexOutOfBoundsException {
		super.parseArgs(cli, args);
		trace_name = args[1];
		if ( cli.hasOption(maxTimeOption) )
			d_end = Double.parseDouble(cli.getOptionValue(maxTimeOption));
		if ( cli.hasOption(minTimeOption) )
			d_begin = Double.parseDouble(cli.getOptionValue(minTimeOption));
	}

	
	@Override
	protected void run() throws IOException, NoSuchTraceException {
		Trace<?> trace = _store.getTrace(trace_name);
		long min_time = (d_begin != null )? 
					(long)(d_begin * trace.ticsPerSecond()) 
					: trace.minTime();
		long max_time = (d_end != null)?
					(long)(d_end * trace.ticsPerSecond()) 
					: trace.maxTime();
		Reader<?> reader = trace.getReader();
		long event_count = 0;
		long event_times_count = 0;
		reader.seek(min_time);
		while ( reader.hasNext() && reader.nextTime() <= max_time ){
			Collection<?> events = reader.next();
			event_times_count++;
			event_count += events.size();
		}
		reader.close();
		System.out.println(event_count+" "+event_times_count);
	}

	@Override
	protected String getUsageString(){
		return "[OPTIONS] STORE TRACE_NAME";
	}
	
}
