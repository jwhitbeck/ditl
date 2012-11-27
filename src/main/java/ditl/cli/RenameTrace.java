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

import java.io.IOException;

import org.apache.commons.cli.*;

import ditl.*;
import ditl.WritableStore.AlreadyExistsException;

@Command(cmd="mv")
public class RenameTrace extends WriteApp {

	private String orig_name, dest_name;

	@Override
	protected void parseArgs(CommandLine cli, String[] args) throws ParseException, HelpException, ArrayIndexOutOfBoundsException {
		super.parseArgs(cli, args);
		orig_name = args[1];
		dest_name = args[2];
	}

	@Override
	protected String getUsageString() {
		return "[OPTIONS] STORE ORIG_TRACE DEST_TRACE";
	}
	
	@Override
	protected void run() throws IOException, Store.NoSuchTraceException, AlreadyExistsException {
		_store.moveTrace(orig_name, dest_name, force);
	}	
}
