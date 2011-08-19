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
package ditl;

public abstract class Trace {
	
	final public static long INFINITY = Long.MAX_VALUE / 2; // divided by 2 to avoid edge effects on signs of longs
	
	final public static String nameKey = "name";
	final public static String typeKey = "type";
	final public static String ticsPerSecondKey = "tics per second";
	final public static String descriptionKey = "description";
	final public static String minUpdateIntervalKey = "min update interval";
	final public static String maxUpdateIntervalKey = "max update interval";
	final public static String minTimeKey = "min time";
	final public static String maxTimeKey = "max time";
	final public static String defaultPriorityKey = "default priority";
	final public static String snapshotIntervalKey = "snapshot interval";
	
	public abstract String getValue(String key);
	
	public String name(){
		return getValue(nameKey);
	}
	
	public String description(){
		return getValue(descriptionKey);
	}
	
	public String type(){
		return getValue(typeKey);
	}
	
	public long minTime(){
		return Long.parseLong(getValue(minTimeKey));
	}
	
	public long maxTime(){
		return Long.parseLong(getValue(maxTimeKey));
	}
	
	public long maxUpdateInterval(){
		return Long.parseLong(getValue(maxUpdateIntervalKey));
	}
	
	public long minUpdateInterval(){
		return Long.parseLong(getValue(minUpdateIntervalKey));
	}
	
	public long snapshotInterval(){
		return Long.parseLong(getValue(snapshotIntervalKey));
	}
	
	public int defaultPriority(){
		return Integer.parseInt(getValue(defaultPriorityKey));
	}
	
	public long ticsPerSecond(){
		return Long.parseLong(getValue(ticsPerSecondKey));
	}
	
	public boolean isStateful(){
		return (getValue(snapshotIntervalKey) != null);
	}
	
	@Override
	public boolean equals(Object o){
		Trace t = (Trace)o; 
		return t.name().equals(name());
	}
}
