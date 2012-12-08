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
package ditl.plausible.constraints;

import ditl.graphs.Point;
import ditl.plausible.Constraint;
import ditl.plausible.InferredNode;

public class MaxSpeedConstraint implements Constraint {

    private final double max_speed;
    private final double max_speed_2;

    public MaxSpeedConstraint(double maxSpeed) {
        max_speed = maxSpeed;
        max_speed_2 = max_speed * max_speed;
    }

    @Override
    public void apply(InferredNode node) {
        final Point s = node.nextSpeed();
        final double ns2 = s.x * s.x + s.y * s.y;
        if (ns2 > max_speed_2) {
            final double a = max_speed / Math.sqrt(ns2);
            s.x *= a;
            s.y *= a;
        }
    }

}
