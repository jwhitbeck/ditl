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
package ditl.plausible.constraints;

import ditl.plausible.*;
import ditl.graphs.Point;

public class BoxConstraint implements Constraint {

	private double _width, _height, _border;
	private boolean elastic = true;
	
	public BoxConstraint(double width, double height, double border){
		_width = width;
		_height = height;
		_border = border;
	}
	
	public void setElastic(boolean isElastic){
		elastic = isElastic;
	}

	@Override
	public void apply(InferredNode node) {
		Point r = node.nextPosition();
		Point s = node.nextSpeed();
		if ( r.x > _width - _border ){
			r.x = _width - _border;
			if ( elastic )
				s.x = -s.x;
			else
				s.x = 0;
		} else if ( r.x < _border ){
			r.x = _border;
			if ( elastic )
				s.x = -s.x;
			else
				s.x = 0;
		}
		if ( r.y > _height - _border ){
			r.y = _height - _border;
			if ( elastic )
				s.y = -s.y;
			else
				s.y = 0;
		} else if ( r.y < _border ){
			r.y = _border;
			if ( elastic )
				s.y = -s.y;
			else
				s.y = 0;
		}
	}
	

}
