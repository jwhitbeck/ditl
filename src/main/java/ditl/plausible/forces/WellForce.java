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
package ditl.plausible.forces;

import ditl.graphs.Point;
import ditl.plausible.*;

public class WellForce implements Force {

	private double _height;
	private double _width;
	public double G = 10.0; // Gravitational constant
	public double er = 1.0; // guard to prevent denom from going to zero
	public double exp = 2.0; // the exponent in the coulomb force
	
	
	
	public WellForce ( double width, double height ){
		_height = height;
		_width = width;
	}
	
	@Override
	public Point apply(long time, InferredNode node) {
		Point r = node.currentPosition();
		double ax, ay;
		ax = G * ( 1 /Math.pow(r.x+er, exp) - 1 / Math.pow(_width - r.x+er, exp) );
		ay = G * ( 1 /Math.pow(r.y+er, exp) - 1 / Math.pow(_height - r.y+er, exp) );
		return new Point(ax,ay);
	}

}
