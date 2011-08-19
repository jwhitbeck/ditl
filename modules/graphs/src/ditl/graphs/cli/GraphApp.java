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

import ditl.cli.App;

abstract class GraphApp extends App {

	protected final static String presenceOption = "presence";
	protected final static String ccOption = "cc";
	protected final static String linksOption = "links";
	protected final static String edgesOption = "edges";
	protected final static String groupsOption = "groups";
	protected final static String movementOption = "movement";
	protected final static String beaconsOption = "beacons";
	
	protected final static String ns2Format = "NS2";
	protected final static String oneFormat = "ONE";
	protected final static String crawdadFormat = "CRAWDAD";
	
	
	public GraphApp(String[] args) {
		super(args);
	}
}
