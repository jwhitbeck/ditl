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
package ditl.transfers.cli;

import java.io.*;

import org.apache.commons.cli.*;

import ditl.cli.App;
import ditl.transfers.viz.TransferPlayer;

public class Player extends App {

	private File[] files;
	
	public final static String PKG_NAME = "transfers";
	public final static String CMD_NAME = "play";
	public final static String CMD_ALIAS = "p";
	
	
	@Override
	protected void initOptions() {}

	@Override
	protected void parseArgs(CommandLine cli, String[] args)
			throws ParseException, ArrayIndexOutOfBoundsException,
			HelpException {
		if ( args.length > 0 ){
			files = new File[args.length];
			for(int i=0; i<args.length; ++i)
				files[i] = new File(args[i]);
		}
	}

	@Override
	protected void run() throws IOException {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				TransferPlayer player = new TransferPlayer();
				if ( files != null )
					player.load(files);
			}
		});
	}

	@Override
	protected String getUsageString() {
		return "STORE1 [STORE2...]";
	}
	
	

}
