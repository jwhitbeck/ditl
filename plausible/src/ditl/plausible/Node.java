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

import java.io.IOException;

import ditl.StatefulWriter;
import ditl.graphs.*;

public abstract class Node {
	
	protected Point last_ref = new Point(0,0); 	// last reference point
	protected Point prev_sample = new Point(0,0); // previously sampled point
	protected Tube tube; // the current movement tube
	protected boolean paused = true;
	protected long last_time;

	protected Point cur = new Point(0,0); // current position
	protected Point next = new Point(0, 0); // next values
	
	Integer _id;
	
	public Node(Integer id){
		_id = id;
	}
	
	public Integer id(){
		return _id;
	}
	
	public abstract void step(long time, long dt, long tps);
	
	public void commit(){
		cur.x = next.x;
		cur.y = next.y;
	}
	
	public void sampleCurrent(){
		prev_sample = cur.copy();
	}
	
	public void setReference(long time, Point p){
		last_time = time;
		last_ref = p.copy();
	}
	
	public void writeMovement(long cur_time, long prev_time, double s, double e,
			StatefulWriter<MovementEvent,Movement> writer) throws IOException {
		double d = dist(prev_sample,cur);
		if ( d < s ){ // node hasn't moved this step
			if ( paused ){ // node already paused, just update last times
				last_time = cur_time;
			} else { // node was mobile until now, stop its movement and pause it
				double dt = (double)(prev_time - last_time);
				double ld = dist(last_ref,prev_sample);
				double sp = ld/dt;
				writer.queue(last_time, new MovementEvent(_id,sp,prev_sample));
				writer.queue(prev_time, new MovementEvent(_id,0,prev_sample));
				paused = true;
				last_time = cur_time;
				last_ref = prev_sample.copy();
			}
		} else { // node has moved
			if ( paused ){ // move from last_ref
				tube = new Tube(last_ref, e);
				paused = false;
			} else {
				if ( ! tube.addToTube(cur) ){ // current position breaks current tube
					double ld = dist(last_ref,prev_sample);
					double dt = (double)(prev_time - last_time);
					double sp = ld/dt;
					writer.queue(last_time, new MovementEvent(_id,sp,prev_sample));
					last_ref = prev_sample.copy();
					last_time = prev_time;
					tube = new Tube(last_ref,e);
				}
			}
		}
		sampleCurrent();		
	}
	
	public Point currentPosition(){
		return cur;
	}
	
	public Point nextPosition(){
		return next;
	}
	
	public long lastRefTime(){
		return last_time;
	}
	
	private double dist(Point from, Point to){
		double dx = from.x-to.x;
		double dy = from.y-to.y;
		return Math.sqrt(dx*dx + dy*dy);
	}
}
