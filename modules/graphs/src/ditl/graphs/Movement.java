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

import java.util.Set;

import ditl.*;

public final class Movement {
	
	private Integer id;
	double x, y;
	private double sx, sy;
	private long since;
	long arrival;
	private double dx,dy;
	
	private Movement(){};
	
	public Movement( Integer i, Point orig ){
		id = i;
		x = orig.x;
		y = orig.y;
		sx = 0;
		sy = 0;
	}
	
	public Integer id(){
		return id;
	}
	
	public Movement( Integer i, Point orig, long t, Point dest, double sp ){
		id = i;
		x = orig.x;
		y = orig.y;
		since = t;
		dx = dest.x-orig.x;
		dy = dest.y-orig.y;
		double d = Math.sqrt(dx*dx+dy*dy);
		if ( d > 0 ){
			arrival = since + (long)Math.ceil(d/sp);
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
			String[] elems = s.trim().split(" ");
			try {
				Integer id = Integer.parseInt(elems[0]);
				double ox = Double.parseDouble(elems[1]);
				double oy = Double.parseDouble(elems[2]);
				if ( elems.length == 3 )
					return new Movement(id,new Point(ox,oy));
				long since = Long.parseLong(elems[3]);
				double tx = Double.parseDouble(elems[4]);
				double ty = Double.parseDouble(elems[5]);
				double sp = Double.parseDouble(elems[6]);
				return new Movement(id, new Point(ox, oy), since, new Point(tx, ty), sp);					
			} catch ( Exception e ){
				System.err.println( "Error parsing '"+s+"': "+e.getMessage() );
				return null;
			}
		}
	}
	
	public Point positionAtTime(long t){
		long dt = t-since;
		double dX = sx*dt;
		double dY = sy*dt;
		return new Point(x+dX, y+dY);
	}
	
	public Point from(){
		return new Point(x,y);
	}
	
	public Point to(){
		return new Point(x+dx,y+dy);
	}
	
	public void setNewDestination(long time, Point dest, double sp){
		Point p = positionAtTime(time);
		x = p.x;
		y = p.y;
		since = time;
		dx = dest.x-x;
		dy = dest.y-y;
		double d = Math.sqrt(dx*dx+dy*dy);
		if ( sp > 0 && d > 0){
			arrival = since + (long)Math.ceil(d/sp);
			sx = dx * sp / d;
			sy = dy * sp / d;
		} else {
			arrival = time;
			sx = 0;
			sy = 0;
		}
	}
	
	public void handleEvent(long time, MovementEvent event){
		setNewDestination(time, event.dest, event.speed);
	}
	
	@Override
	public String toString(){
		if ( sx == 0 && sy == 0)
			return id+" "+x+" "+y;
		return id+" "+x+" "+y+" "+since+" "+(x+dx)+" "+(y+dy)+" "+Math.sqrt(sx*sx+sy*sy);
	}
	
	@Override
	public int hashCode(){
		return id;
	}
	
	@Override
	public boolean equals(Object o){
		Movement m = (Movement)o;
		return id.equals(m.id);
	}
	
	public String ns2String(){
		return 	"$node_("+id+") set X_ "+x+"\n"+
				"$node_("+id+") set Y_ "+y+"\n";
	}
	
	public String oneString(long time, double mul){
		Point p = positionAtTime(time);
		return time*mul+" "+id+" "+p.x+" "+p.y+"\n";
	}
	
	public double dist2(long time, Movement m){
		Point p = positionAtTime(time);
		Point op = m.positionAtTime(time);
		double dx = op.x-p.x, dy = op.y-p.y;
		return dx*dx + dy*dy;
	}
	
	public long[] meetingTimes(Movement m, double r2){
		double Ax = (x-sx*since) - (m.x-m.sx*m.since);
		double Ay = (y-sy*since) - (m.y-m.sy*m.since);
		double Bx = sx-m.sx;
		double By = sy-m.sy;
		double A2 = Ax*Ax + Ay*Ay;
		double B2 = Bx*Bx + By*By;
		if ( B2 == 0 ){ // parallel trajectories or 2 immobile nodes
			double d2 = (x-m.x)*(x-m.x)+(y-m.y)*(y-m.y);
			if ( d2 <= r2 ){ // will forever be with distance r2
				return new long[]{-Trace.INFINITY, Trace.INFINITY};
			} else // will never meet
				return null;
		} else {
			double AB = Ax*Bx + Ay*By;
			double D2 = AB*AB-(A2-r2)*B2;
			if ( D2 >= 0 ){
				double d = Math.sqrt(D2);
				long t1 = (long)(-AB/B2-d/B2);
				long t2 = (long)(-AB/B2+d/B2);
				return new long[]{t1, t2};
			}
		}
		return null;
	}
	
	public static final class GroupMatcher implements Matcher<Movement> {
		private Set<Integer> _group;
		public GroupMatcher(Set<Integer> group){ _group = group;}
		@Override
		public boolean matches(Movement item) { return _group.contains(item.id);}
	}
	
	@Override
	public Movement clone(){
		Movement m = new Movement();
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
