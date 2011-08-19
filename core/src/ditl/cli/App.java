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

import org.apache.commons.cli.*;

import ditl.*;

public abstract class App {
	
	protected final static String fmtOption = "format";
	protected final static String offsetOption = "offset";
	protected final static String origTimeUnitOption = "orig-time-unit";
	protected final static String destTimeUnitOption = "dest-time-unit";
	protected final static String maxTimeOption = "max-time";
	protected final static String minTimeOption = "min-time";
	protected final static String snapIntervalOption = "snap-interval";
	protected final static String intervalOption = "interval";
	protected final static String traceOption = "trace";
	protected final static String outputOption = "output";
	protected final static String storeOutputOption = "out-store";
	
	protected Options options = new Options();
	protected String usageString;
	protected boolean showHelp = false;
	
	protected abstract void initOptions();
	protected abstract void setUsageString();
	protected abstract void parseArgs(CommandLine cli, String[] args) 
		throws ParseException, ArrayIndexOutOfBoundsException, HelpException;

	@SuppressWarnings("serial")
	public class HelpException extends Exception {}
	@SuppressWarnings("serial")
	public class MissingTraceException extends Exception {
		private String trace_name; 
		public MissingTraceException(String traceName){ trace_name = traceName;}
		@Override
		public String toString(){
			return "Error! Could not find trace '"+trace_name+"'";
		}
	}

	public App(String[] args) {
		options.addOption(new Option("h","help",false,"Print help"));
		initOptions();
		setUsageString();
		try {
			CommandLine cli = new PosixParser().parse(options, args);
			if ( cli.hasOption("help") )
				throw new HelpException();
			parseArgs(cli, cli.getArgs());
		} catch (ParseException e) {
			System.err.println(e);
			printHelp();
		} catch ( ArrayIndexOutOfBoundsException e){
			printHelp();
		} catch ( NumberFormatException nfe ){
			System.err.println(nfe);
			printHelp();
		} catch ( HelpException he ){
			printHelp();
		}
	}
	
	protected abstract void run() throws IOException, MissingTraceException;
	
	public void exec() throws IOException {
		try {
			run();
		} catch ( MissingTraceException mte ){
			System.err.println(mte);
			System.exit(1);
		}
	}
	
	protected void printHelp(){
		new HelpFormatter().printHelp(usageString, options);
		System.exit(1);
	}
	
	protected Long getTicsPerSecond(String timeUnit){
		if ( timeUnit.equals("s") ){
			return 1L;
		} else if ( timeUnit.equals("ms") ){
			return 1000L;
		} else if ( timeUnit.equals("us") ){
			return 1000000L;
		} else if ( timeUnit.equals("ns") ){
			return 1000000000L;
		}
		return null;
	}
	
	protected Double getTimeMul(Long otps, Long dtps){
		if ( otps != null && dtps != null ){
			return dtps.doubleValue()/otps.doubleValue(); 
		}
		return null;
	}
	
	protected Trace getTrace(Store store, String traceName) throws MissingTraceException {
		if ( ! store.hasTrace(traceName) )
			throw new MissingTraceException (traceName);
		return store.getTrace(traceName);
	}
}
