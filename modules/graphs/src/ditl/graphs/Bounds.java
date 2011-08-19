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

import ditl.Writer;

public final class Bounds {
	private double minX = Double.MAX_VALUE;
	private double maxX = Double.MIN_VALUE;
	private double minY = Double.MAX_VALUE;
	private double maxY = Double.MIN_VALUE;
	
	public void update(Point p){
		updateX(p.x);
		updateY(p.y);
	}
	
	public void updateX(double x){
		if ( x < minX ) minX = x;
		if ( x > maxX ) maxX = x;
	}
	
	public void updateY(double y){
		if ( y < minY ) minY = y;
		if ( y > maxY ) maxY = y;
	}
	
	public void writeToTrace(Writer<?> writer){
		writer.setProperty(MovementTrace.minXKey, minX);
		writer.setProperty(MovementTrace.maxXKey, maxX);
		writer.setProperty(MovementTrace.minYKey, minY);
		writer.setProperty(MovementTrace.maxYKey, maxY);
	}
}
