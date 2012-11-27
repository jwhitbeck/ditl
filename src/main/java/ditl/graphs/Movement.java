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
package ditl.graphs;

import java.util.Set;

import ditl.Filter;
import ditl.ItemFactory;
import ditl.Trace;

public final class Movement {

    private Integer id;
    double x, y;
    private double sx, sy;
    private long since;
    long arrival;
    private double dx, dy;

    private Movement() {
    };

    public Movement(Integer i, Point orig) {
        id = i;
        x = orig.x;
        y = orig.y;
        sx = 0;
        sy = 0;
    }

    public Integer id() {
        return id;
    }

    public Movement(Integer i, Point orig, long t, Point dest, double sp) {
        id = i;
        x = orig.x;
        y = orig.y;
        since = t;
        dx = dest.x - orig.x;
        dy = dest.y - orig.y;
        final double d = Math.sqrt(dx * dx + dy * dy);
        if (d > 0) {
            arrival = since + (long) Math.ceil(d / sp);
            sx = dx * sp / d;
            sy = dy * sp / d;
        } else {
            arrival = t;
            sx = 0;
            sy = 0;
        }
    }

    public static final class Factory implements ItemFactory<Movement> {
        @Override
        public Movement fromString(String s) {
            final String[] elems = s.trim().split(" ");
            try {
                final Integer id = Integer.parseInt(elems[0]);
                final double ox = Double.parseDouble(elems[1]);
                final double oy = Double.parseDouble(elems[2]);
                if (elems.length == 3)
                    return new Movement(id, new Point(ox, oy));
                final long since = Long.parseLong(elems[3]);
                final double tx = Double.parseDouble(elems[4]);
                final double ty = Double.parseDouble(elems[5]);
                final double sp = Double.parseDouble(elems[6]);
                return new Movement(id, new Point(ox, oy), since, new Point(tx, ty), sp);
            } catch (final Exception e) {
                System.err.println("Error parsing '" + s + "': " + e.getMessage());
                return null;
            }
        }
    }

    public Point positionAtTime(long t) {
        final long dt = t - since;
        final double dX = sx * dt;
        final double dY = sy * dt;
        return new Point(x + dX, y + dY);
    }

    public Point from() {
        return new Point(x, y);
    }

    public Point to() {
        return new Point(x + dx, y + dy);
    }

    public void setNewDestination(long time, Point dest, double sp) {
        final Point p = positionAtTime(time);
        x = p.x;
        y = p.y;
        since = time;
        dx = dest.x - x;
        dy = dest.y - y;
        final double d = Math.sqrt(dx * dx + dy * dy);
        if (sp > 0 && d > 0) {
            arrival = since + (long) Math.ceil(d / sp);
            sx = dx * sp / d;
            sy = dy * sp / d;
        } else {
            arrival = time;
            sx = 0;
            sy = 0;
        }
    }

    public void handleEvent(long time, MovementEvent event) {
        setNewDestination(time, event.dest, event.speed);
    }

    @Override
    public String toString() {
        if (sx == 0 && sy == 0)
            return id + " " + x + " " + y;
        return id + " " + x + " " + y + " " + since + " " + (x + dx) + " " + (y + dy) + " " + Math.sqrt(sx * sx + sy * sy);
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        final Movement m = (Movement) o;
        return id.equals(m.id);
    }

    public String ns2String() {
        return "$node_(" + id + ") set X_ " + x + "\n" +
                "$node_(" + id + ") set Y_ " + y + "\n";
    }

    public String oneString(long time, double mul) {
        final Point p = positionAtTime(time);
        return time * mul + " " + id + " " + p.x + " " + p.y + "\n";
    }

    public double dist2(long time, Movement m) {
        final Point p = positionAtTime(time);
        final Point op = m.positionAtTime(time);
        final double dx = op.x - p.x, dy = op.y - p.y;
        return dx * dx + dy * dy;
    }

    public long[] meetingTimes(Movement m, double r2) {
        final double Ax = (x - sx * since) - (m.x - m.sx * m.since);
        final double Ay = (y - sy * since) - (m.y - m.sy * m.since);
        final double Bx = sx - m.sx;
        final double By = sy - m.sy;
        final double A2 = Ax * Ax + Ay * Ay;
        final double B2 = Bx * Bx + By * By;
        if (B2 == 0) { // parallel trajectories or 2 immobile nodes
            final double d2 = (x - m.x) * (x - m.x) + (y - m.y) * (y - m.y);
            if (d2 <= r2)
                return new long[] { -Trace.INFINITY, Trace.INFINITY };
            else
                // will never meet
                return null;
        } else {
            final double AB = Ax * Bx + Ay * By;
            final double D2 = AB * AB - (A2 - r2) * B2;
            if (D2 >= 0) {
                final double d = Math.sqrt(D2);
                final long t1 = (long) (-AB / B2 - d / B2);
                final long t2 = (long) (-AB / B2 + d / B2);
                return new long[] { t1, t2 };
            }
        }
        return null;
    }

    public static final class GroupFilter implements Filter<Movement> {
        private final Set<Integer> _group;

        public GroupFilter(Set<Integer> group) {
            _group = group;
        }

        @Override
        public Movement filter(Movement item) {
            if (_group.contains(item.id))
                return item;
            return null;
        }
    }

    @Override
    public Movement clone() {
        final Movement m = new Movement();
        m.id = id;
        m.x = x;
        m.y = y;
        m.sx = sx;
        m.sy = sy;
        m.since = since;
        m.arrival = arrival;
        m.dx = dx;
        m.dy = dy;
        return m;
    }
}
