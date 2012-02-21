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

import org.apache.commons.cli.*;

public abstract class ExportApp extends ReadOnlyApp {

	protected String out_file_name;
	protected OutputStream _out;
	
	@Override
	protected void initOptions(){
		super.initOptions();
		options.addOption(null, outputOption, true, "output file");
	}
	
	protected void parseArgs(CommandLine cli, String[] args) 
		throws ParseException, ArrayIndexOutOfBoundsException, HelpException {
		super.parseArgs(cli, args);
		out_file_name = cli.getOptionValue(outputOption);
	}
	
	@Override
	protected void init() throws IOException {
		super.init();
		_out = (out_file_name != null)? new FileOutputStream(out_file_name) : System.out;
	}

}
