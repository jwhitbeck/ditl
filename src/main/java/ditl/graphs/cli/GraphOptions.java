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

import org.apache.commons.cli.*;

import ditl.Trace;
import ditl.graphs.*;

public enum GraphOptions {
	
	PRESENCE(PresenceTrace.class),
	EDGES(EdgeTrace.class),
	ARCS(ArcTrace.class),
	MOVEMENT(MovementTrace.class),
	GROUPS(GroupTrace.class),
	BEACONS(BeaconTrace.class);
	
	final String name;
	
	private GraphOptions(Class<? extends Trace<?>> klass){
		name = klass.getAnnotation(Trace.Type.class).value();
	}
	
	public final static class CliParser {
		
		private final GraphOptions[] _opts;
		private final String[] _values = new String[GraphOptions.values().length];
		
		public CliParser(GraphOptions...opts){
			_opts = opts;
		}
		
		public void setOptions(Options options){
			for ( GraphOptions opt : _opts )
				options.addOption(null, opt.name, true, 
						String.format("name of %s trace (default: %s)", opt.name, opt.name));
		}
		
		public void parse(CommandLine cli){
			for ( GraphOptions opt : _opts )
				_values[opt.ordinal()] = cli.getOptionValue(opt.name,opt.name);
		}
		
		public String get(GraphOptions opt){
			return _values[opt.ordinal()];
		}
		
	}
}
