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
package ditl.viz;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public abstract class Scene extends JPanel implements TimeChangeListener, Scaler {

	protected double scale = 1.0;
	protected int dX = 0;
	protected int dY = 0;
	protected int odX = 0;
	protected int odY = 0;
	protected int cX, cY;
	protected double fact = Math.sqrt(2);
	protected boolean dragging = false;
	protected Set<ScaleListener> scaleListeners = new HashSet<ScaleListener>();
	protected Object VALUE_ANTIALIAS = RenderingHints.VALUE_ANTIALIAS_ON;
	
	public Scene()
	{
		super();
		this.setBackground(Color.WHITE);
        
        MouseAdapter mouseAdapter = new MouseAdapter(){
        	@Override
			public void mousePressed(MouseEvent e){
        		dragging = true;
        		odX = dX;
        		odY = dY;
        		cX = e.getX();
        		cY = e.getY();
        	}
        	
        	@Override
			public void mouseReleased(MouseEvent e){
        		dragging = false;
        	}
        	
        	@Override
        	public void mouseDragged(MouseEvent e){
        		dX = odX + (e.getX()-cX);
        		dY = odY + (e.getY()-cY);
        		rescale();
        		repaint();
        	}
        	
        	@Override
        	public void mouseWheelMoved(MouseWheelEvent e){
        		int mx = e.getX();
        		int my = e.getY();
        		double os = scale;
        		if ( e.getUnitsToScroll() > 0 ){
        			scaleDown();
        		} else {
        			scaleUp();
        		}
        		double f = (1-scale/os);
        		dX += (mx-dX) * f;
        		dY += (my-dY) * f;
        		rescale();
        		repaint();
        	}
        };
        addMouseWheelListener(mouseAdapter);
        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
        
	}
	
	public void setOffset(int dx, int dy){
		dX = dx; dY = dy; 
	}
	
	public void scaleUp(){
		scale *= fact;
	}
	
	public void scaleDown(){
		scale /= fact;
	}
	
	@Override
	public double rX(int sx){
		return (sx-dX)/scale; 
	}
	
	@Override
	public double rY(int sy){
		return (sy-dY)/scale; 
	}
	
	@Override
	public int sX(double x){
		return (int)(x*scale + dX);
	}
	
	@Override
	public int sY(double y){
		return (int)(y*scale + dY);
	}

	@Override
	public void changeTime(long time) {
		repaint();
	}


	@Override
	public void addScaleListener(ScaleListener listener) {
		scaleListeners.add(listener);
	}


	@Override
	public void removeScaleListener(ScaleListener listener) {
		scaleListeners.remove(listener);
	}
	
	protected void rescale(){
		for ( ScaleListener listener : scaleListeners )
			listener.rescale(this);
	}
	
	public void setAntialising(boolean on){
		if ( on )
			VALUE_ANTIALIAS = RenderingHints.VALUE_ANTIALIAS_ON;
		else
			VALUE_ANTIALIAS = RenderingHints.VALUE_ANTIALIAS_OFF;
	}
	
	public boolean isAntialiasingOn(){
		if ( VALUE_ANTIALIAS == RenderingHints.VALUE_ANTIALIAS_ON )
			return true;
		return false;
	}
	
	@Override
	public void paint(Graphics g){
		super.paint(g);
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, VALUE_ANTIALIAS);
		paint2D(g2);
	}
	
	protected abstract void paint2D(Graphics2D g2);
	
	public void updateSize(double minX, double minY, double maxX, double maxY){
		double rW = maxX-minX;
		double rH = maxY-minY;
		double xScale = getWidth()/rW;
		double yScale = getHeight()/rH;
		scale = Math.min( xScale, yScale);
		dX = - (int)(minX*scale);
		dY = - (int)(minY*scale);
	}
	
}
