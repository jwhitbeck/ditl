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
package ditl.graphs.cli;

import org.apache.commons.cli.*;

import ditl.graphs.*;

public class GraphOptions {

	public final static int PRESENCE = 0;
	public final static int LINKS = 1;
	public final static int EDGES = 2;
	public final static int MOVEMENT = 3;
	public final static int GROUPS = 4;
	public final static int BEACONS = 5;
	
	private final static String[] _options = {
		"presence",
		"links",
		"edges",
		"movement",
		"groups",
		"beacons",
	};
	
	private final static String[] _defaults = {
		PresenceTrace.defaultName,
		LinkTrace.defaultName,
		EdgeTrace.defaultName,
		MovementTrace.defaultName,
		GroupTrace.defaultName,
		BeaconTrace.defaultName,
	};
	
	private final static String[] _help = {
		"name of presence trace (default: "+PresenceTrace.defaultName+")",
		"name of link trace (default: "+LinkTrace.defaultName+")",
		"name of edge trace (default: "+EdgeTrace.defaultName+")",
		"name of movement trace (default: "+MovementTrace.defaultName+")",
		"name of group trace (default: "+GroupTrace.defaultName+")",
		"name of beacon trace (default: "+BeaconTrace.defaultName+")",
	};
	
	private Integer[] _opts;
	private String[] _values;
	
	public GraphOptions (Integer... opts){
		_opts = opts;
		_values = new String[_options.length];
	}
	
	public void setOptions(Options options){
		for ( Integer i : _opts )
			options.addOption(null, _options[i], true, _help[i]);
	}
	
	public void parse(CommandLine cli){
		for ( Integer i : _opts )
			_values[i] = cli.getOptionValue(_options[i],_defaults[i]);
	}
	
	public String get(Integer i){
		return _values[i];
	}
	
}
