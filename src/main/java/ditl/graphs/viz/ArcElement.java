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
package ditl.graphs.viz;

import java.awt.*;
import java.awt.geom.Path2D;

import ditl.graphs.Arc;
import ditl.viz.SceneElement;



public class ArcElement implements SceneElement {

	enum State { DOWN, LEFT, RIGHT, SYM }
	
	protected static Color 
		directionalColor = Color.RED,
		biColor = Color.GREEN;
	
	private NodeElement node1;
	private NodeElement node2;
	protected State state = State.DOWN;
	protected double alpha = Math.toRadians(30);
	protected float barb = 10;
	protected float offset = 5;
	
	public ArcElement(NodeElement n1, NodeElement n2){
		if ( n1.id() < n2.id() ){
			node1 = n1;
			node2 = n2;
		} else {
			node1 = n2;
			node2 = n1;
		}
	}
	
	public void bringArcUp(Arc a){ // from n1 to n2
		if ( node1.id().equals(a.from()) ){ // RIGHT EDGE
			switch ( state ){
			case DOWN: state=State.RIGHT; break;
			case LEFT: state=State.SYM; break;
			}
		} else { // LEFT EDGE
			switch ( state ){
			case DOWN: state=State.LEFT; break;
			case RIGHT: state=State.SYM; break;
			}
		}
	}
	
	public void bringArcDown(Arc a){ // from n1 to n2
		if ( node1.id().equals(a.from()) ){ // RIGHT EDGE
			switch ( state ){
			case SYM: state=State.LEFT; break;
			case RIGHT: state=State.DOWN; break;
			}
		} else { // LEFT EDGE
			switch ( state ){
			case SYM: state=State.RIGHT; break;
			case LEFT: state=State.DOWN; break;
			}
		}
	}

	@Override
	public void paint(Graphics2D g2) {
		g2.setColor( (state==State.SYM)? biColor : directionalColor );
		if ( state != State.SYM ){
			int DX = node2.sX-node1.sX;
			int DY = node2.sY-node1.sY;
			double theta = Math.atan2(DY, DX);
			double d = Math.sqrt(DX*DX+DY*DY);
			double ox = offset/d*DX;
			double oy = offset/d*DY;
			if ( state == State.LEFT )
				g2.draw(createArrowHead(node1.sX+ox,node1.sY+oy,theta));
			else 
				g2.draw(createArrowHead(node2.sX-ox,node2.sY-oy,theta + Math.PI)); 
		}
		g2.drawLine(node1.sX, node1.sY, node2.sX, node2.sY);
	}
	
	private Path2D.Float createArrowHead(double x, double y, double theta){
		Path2D.Float path = new Path2D.Float();
		double X1 = x + barb*Math.cos(theta-alpha);
		double Y1 = y + barb*Math.sin(theta-alpha);
		double X2 = x + barb*Math.cos(theta+alpha);
		double Y2 = y + barb*Math.sin(theta+alpha);
		path.moveTo(X1, Y1);
		path.lineTo(x, y);
		path.lineTo(X2, Y2);
		return path;
	}
	
	
	
}
