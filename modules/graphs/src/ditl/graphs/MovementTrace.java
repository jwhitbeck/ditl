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
package ditl.graphs;

import ditl.*;

public class MovementTrace extends WrappedTrace {

	final public static String minXKey = "min X";
	final public static String maxXKey = "max X";
	final public static String minYKey = "min Y";
	final public static String maxYKey = "max Y";
	
	public MovementTrace(Trace trace){
		super(trace);
	}
	
	public double minX(){ 
		return Double.parseDouble(getValue(minXKey));
	}
	
	public double maxX(){
		return Double.parseDouble(getValue(maxXKey));
	}
	
	public double minY(){ 
		return Double.parseDouble(getValue(minYKey));
	}
	
	public double maxY(){ 
		return Double.parseDouble(getValue(maxYKey));
	}
	
}
