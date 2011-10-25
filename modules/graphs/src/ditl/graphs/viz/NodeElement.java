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
package ditl.graphs.viz;

import java.awt.*;

import ditl.graphs.*;
import ditl.graphs.Point;
import ditl.viz.*;



public class NodeElement implements Clickable, SceneElement, ScaleListener, TimeChangeListener {
	
	public final static Color defaultFillColor = GroupsPanel.noGroupColor;
	public final static Color defaultBorderColor = Color.BLACK;
	
	private Color fill = defaultFillColor;
	private Color borderColor = defaultBorderColor;
	private boolean showId = false;
	protected int R = 5;
	private Movement movement;
	public double X, Y;
	public int sX, sY;
	private String name;
	
	public NodeElement(Movement m){
		movement = m;
		name = String.valueOf(movement.id());
	}
	
	public NodeElement(Movement m, String nodeName){
		movement = m;
		name = nodeName;
	}
	
	public Integer id(){
		return movement.id();
	}
	
	public String name(){
		return name;
	}
	
	public void setShowId(boolean show){
		showId = show;
	}
	
	public void updateMovement(long time, MovementEvent mev){
		movement.handleEvent(time, mev);
	}
	
	public void setFillColor(Color color){
		fill = color;
	}
	
	public void setBorderColor(Color color){
		borderColor = color;
	}
	
	@Override
	public void paint(Graphics2D g2) {
		int x = sX - R;
		int y = sY - R;
		g2.setColor(fill);
		g2.fillOval(x,y,R*2,R*2);
		g2.setColor(borderColor);
		g2.drawOval(x,y,R*2,R*2);
		if ( showId )
			g2.drawString(name, x+R*2, y+R);
	}

	@Override
	public boolean contains(int x, int y) {
		return ( x <= sX+R && x >= sX-R ) && ( y <= sY+R && y >= sY-R );
	}

	@Override
	public void rescale(Scaler scaler) {
		sX = scaler.sX(X);
		sY = scaler.sY(Y);
	}

	@Override
	public void changeTime(long time) {
		Point p = movement.positionAtTime(time);
		X = p.x;
		Y = p.y;
	}

}
