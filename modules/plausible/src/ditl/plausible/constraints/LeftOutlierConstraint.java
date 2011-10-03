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

import java.util.Collection;

import ditl.plausible.*;
import ditl.graphs.Point;

public class LeftOutlierConstraint implements Constraint, Interaction {

	private Collection<Node> _nodes = null;
	
	@Override
	public void apply(InferredNode node) {
		if ( _nodes != null ){
			Point r = node.nextPosition();
			for ( Node other_node : _nodes ){
				Point or = other_node.nextPosition();
				if ( or.x < r.x )
					r.x = or.x;
			}
		}
	}

	@Override
	public void setNodeCollection(Collection<Node> nodes) {
		_nodes = nodes;
	}

}
