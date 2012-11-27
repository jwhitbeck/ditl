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
package ditl.plausible;

import ditl.graphs.Point;

public final class Tube {
    private final Point orig;
    private double min_theta = Double.NaN;
    private double max_theta = Double.NaN;
    private final double _e;

    public Tube(Point o, double e) {
        orig = o;
        _e = e;
    }

    public boolean addToTube(Point p) {
        final double dx = p.x - orig.x;
        final double dy = p.y - orig.y;
        final double phi = Math.atan2(dy, dx);
        final double d = Math.sqrt(dx * dx + dy * dy);
        final double alpha = Math.atan(_e / d);
        final double min_t = phi - alpha;
        final double max_t = phi + alpha;

        if (tube_set()) {
            if (min_t <= max_theta && min_t >= min_theta) {
                min_theta = min_t;
                return true;
            } else if (max_t <= max_theta && max_t >= min_theta) {
                max_theta = max_t;
                return true;
            }
            return false;
        }
        min_theta = phi - alpha;
        max_theta = phi + alpha;
        return true;
    }

    private boolean tube_set() {
        return !Double.isNaN(min_theta);
    }

}
