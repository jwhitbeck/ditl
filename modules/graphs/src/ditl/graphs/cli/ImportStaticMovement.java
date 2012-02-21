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
package ditl.graphs.cli;

import java.io.IOException;
import java.util.*;

import org.apache.commons.cli.*;

import ditl.*;
import ditl.Store.LoadTraceException;
import ditl.Store.NoSuchTraceException;
import ditl.WritableStore.AlreadyExistsException;
import ditl.cli.WriteApp;
import ditl.graphs.*;


public class ImportStaticMovement extends WriteApp {
	
	private String[] positions_specs;
	private boolean use_id_map;
	private GraphOptions graph_options = new GraphOptions(GraphOptions.PRESENCE, GraphOptions.MOVEMENT);
	
	public final static String PKG_NAME = "graphs";
	public final static String CMD_NAME = "import-positions";
	public final static String CMD_ALIAS = "ip";
	
	@Override
	protected String getUsageString(){
		return "[OPTIONS] STORE ID1:X:Y [ID2:X:Y..]";
	}

	@Override
	protected void parseArgs(CommandLine cli, String[] args) throws ArrayIndexOutOfBoundsException, ParseException, HelpException {
		super.parseArgs(cli, args);
		graph_options.parse(cli);
		positions_specs = Arrays.copyOfRange(args,1,args.length);
		use_id_map = cli.hasOption(stringIdsOption);
	}

	@Override
	protected void initOptions() {
		super.initOptions();
		graph_options.setOptions(options);
		options.addOption(null, stringIdsOption, false, "treat node ids as strings (default: false)");
	}
	
	@Override
	public void run() throws IOException, NoSuchTraceException, AlreadyExistsException, LoadTraceException {
		Trace<?> presence = _store.getTrace(graph_options.get(GraphOptions.PRESENCE));
		IdMap id_map = presence.idMap();
		MovementTrace movement = (MovementTrace) _store.newTrace(graph_options.get(GraphOptions.MOVEMENT), MovementTrace.type, force);
		StatefulWriter<MovementEvent,Movement> movementWriter = movement.getWriter(presence.snapshotInterval());
		Set<Movement> initState = new HashSet<Movement>();
		for ( String spec : positions_specs ){
			String[] elems = spec.split(":");
			Integer id;
			if ( use_id_map )
				id = id_map.getInternalId(elems[0]);
			else
				id = Integer.parseInt(elems[0]);
			double x = Double.parseDouble(elems[1]);
			double y = Double.parseDouble(elems[2]);
			Point p = new Point(x,y);
			Movement m = new Movement(id, p);
			initState.add(m);
		}
		
		movementWriter.setInitState(presence.minTime(), initState);
		movementWriter.setPropertiesFromTrace(presence);
		movementWriter.close();		
	}
}
