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
package ditl.plausible;

import java.util.*;

import ditl.graphs.Point;

public class InferredNode extends Node {
	
	private final static double c1 = 1.0F/6.0F;
	private final static double c2 = 1.0F/3.0F;
	private final static double c3 = c2;
	private final static double c4 = c1;
	
	// current values
	protected Point curS = new Point(0.0F, 0.0F);

	// next values
	protected Point nextS = new Point(0.0F, 0.0F);

	private Point a = new Point(0.0F,0.0F);
	
	private List<Force> forces = new LinkedList<Force>();
	private List<Constraint> constraints = new LinkedList<Constraint>();
	
	public InferredNode(Integer id) {
		super(id);
	}

	public void addForce(Force force){
		forces.add(force);
	}
	
	public void addConstraint(Constraint constraint){
		constraints.add(constraint);
	}
	
	@Override
	public void step(long time, double rdt){ // the Runge-Kutta approximations
		
		//double rdt = (double)dt / (double)tps; // the real dt
		
		// store current values
		double ox = cur.x, oy = cur.y;
		double osx = curS.x, osy = curS.y;
		
		// round 1
		acc(time);
		double dx1 = curS.x * rdt;
		double dy1 = curS.y * rdt;
		double dsx1 = a.x * rdt;
		double dsy1 = a.y * rdt;
		
		// round 2
		cur.x = ox + dx1/2;
		cur.y = oy + dy1/2;
		curS.x = osx + dsx1/2;
		curS.y = osy + dsy1/2;
		acc(time);
		double dx2 = (curS.x + dsx1/2) * rdt;
		double dy2 = (curS.y + dsy1/2) * rdt;
		double dsx2 = a.x * rdt;
		double dsy2 = a.y * rdt;
		
		// round 3
		cur.x = ox + dx2/2;
		cur.y = oy + dy2/2;
		curS.x = osx + dsx2/2;
		curS.y = osy + dsy2/2;
		acc(time);
		double dx3 = (curS.x + dsx2/2) * rdt;
		double dy3 = (curS.y + dsy2/2) * rdt;
		double dsx3 = a.x * rdt;
		double dsy3 = a.y * rdt;
		
		// round 4
		cur.x = ox + dx3;
		cur.y = oy + dy3;
		curS.x = osx + dsx3;
		curS.y = osy + dsy3;
		acc(time);
		double dx4 = (curS.x + dsx3) * rdt;
		double dy4 = (curS.y + dsy3) * rdt;
		double dsx4 = a.x * rdt;
		double dsy4 = a.y * rdt;
		
		// final diffs
		double dx = c1*dx1 + c2*dx2 + c3*dx3 + c4*dx4;
		double dy = c1*dy1 + c2*dy2 + c3*dy3 + c4*dy4;
		double dsx = c1*dsx1 + c2*dsx2 + c3*dsx3 + c4*dsx4;
		double dsy = c1*dsy1 + c2*dsy2 + c3*dsy3 + c4*dsy4;
		
		// set next values
		next.x = ox+dx;
		next.y = oy+dy;
		nextS.x = osx+dsx;
		nextS.y = osy+dsy;
		
		// sanity check
		if ( Double.isNaN(next.x) || Double.isNaN(next.y) || 
				Double.isNaN(nextS.x) || Double.isNaN(nextS.y) ){
			// do not move
			next.x = ox;
			next.y = oy;
			nextS.x = osx;
			nextS.y = osy;
		} else {
			// apply constraints
			for ( Constraint cnstr : constraints)
				cnstr.apply(this);
		}
		
		// restore previous values
		cur.x = ox;
		cur.y = oy;
		curS.x = osx;
		curS.y = osy;
	}
	
	public void commit(){
		super.commit();
		curS.x = nextS.x;
		curS.y = nextS.y;
	}
	
	private void acc(long time){
		a.x = 0;
		a.y = 0;
		for ( Force f : forces ){
			Point acc = f.apply(time,this);
			a.x += acc.x;
			a.y += acc.y;
		}
	}
	
	public Point currentSpeed(){
		return curS;
	}
	
	public Point nextSpeed(){
		return nextS;
	}
}
