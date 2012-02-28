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
package ditl;

import java.io.IOException;
import java.util.Set;

public abstract class Trace<E> {
	
	final public static long INFINITY = Long.MAX_VALUE / 2; // divided by 2 to avoid edge effects on signs of longs
	
	final public static String nameKey = "name";
	final public static String typeKey = "type";
	final public static String ticsPerSecondKey = "tics per second"; // deprecated. Will be remove in future versions
	final public static String timeUnitKey = "time unit";
	final public static String descriptionKey = "description";
	final public static String minUpdateIntervalKey = "min update interval";
	final public static String maxUpdateIntervalKey = "max update interval";
	final public static String stateMinUpdateIntervalKey = "snapshots min update interval";
	final public static String stateMaxUpdateIntervalKey = "snapshots max update interval";
	final public static String lastSnapTimeKey = "last snapshot time";
	final public static String minTimeKey = "min time";
	final public static String maxTimeKey = "max time";
	final public static String defaultPriorityKey = "default priority";
	final public static String snapshotIntervalKey = "snapshot interval"; // deprecated. Will be removed in future versions.
	final public static String idMapKey = "id map";
	
	final public static int defaultPriority = 100;
	final public static int highestPriority = 0;
	final public static int lowestPriority = Integer.MAX_VALUE;
	
	protected String _name;
	protected Store _store;
	protected PersistentMap _info;
	
	protected ItemFactory<E> event_factory;
	
	public interface Filterable<E> {
		public Filter<E> eventFilter(Set<Integer> group);
		public void fillFilteredTraceInfo(Writer<E> writer);
	}
	
	public Trace(Store store, String name, PersistentMap info, ItemFactory<E> itemFactory) throws IOException {
		_store = store;
		_name = name;
		_info = info;
		event_factory = itemFactory;
	}
	
	public String getValue(String key){
		return _info.get(key);
	}
	
	public String name(){
		return _name;
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
	
	public long lastSnapTime(){
		String str = getValue(lastSnapTimeKey);
		if ( str == null ){ // backwards compatibility. Will be removed
			return Trace.INFINITY;
		}
		return Long.parseLong(str);
	}
	
	public long stateMaxUpdateInterval(){
		String str = getValue(stateMaxUpdateIntervalKey);
		if ( str == null ) // backwards compatibility. Will be removed
			return snapshotInterval();
		return Long.parseLong(str);
	}
	
	public long stateMinUpdateInterval(){
		String str = getValue(stateMinUpdateIntervalKey);
		if ( str == null ) // backwards compatibility. Will be removed
			return snapshotInterval();
		return Long.parseLong(str);
	}
	
	@Deprecated
	public long snapshotInterval(){ // deprecated. Will be removed in future versions
		return Long.parseLong(getValue(snapshotIntervalKey));
	}
	
	public int defaultPriority(){
		return Integer.parseInt(getValue(defaultPriorityKey));
	}
	
	public long ticsPerSecond(){
		String time_unit = getValue(timeUnitKey);
		if ( time_unit == null ){
			return Long.parseLong(getValue(ticsPerSecondKey)); // support for legacy info format. Will be removed in future versions
		}
		return Units.getTicsPerSecond(time_unit);
	}
	
	public String timeUnit(){
		String time_unit = getValue(timeUnitKey);
		if ( time_unit == null ){ // support for legacy info file format. Will be removed in future
			time_unit = Units.toTimeUnit( ticsPerSecond());
		}
		return time_unit;
	}
	
	public IdMap idMap(){
		String id_map_str = getValue ( idMapKey );
		if ( id_map_str == null )
			return null;
		return new IdMap( id_map_str );
	}
	
	public Reader<E> getReader(int priority, long offset) throws IOException {
		return new Reader<E>(_store, _store.getStreamOpener(_store.traceFile(_name)), 
				maxUpdateInterval(), event_factory, priority, offset);
	}

	public Reader<E> getReader(int priority) throws IOException{
		return getReader(priority,0L);
	}

	public Reader<E> getReader() throws IOException{
		return getReader(defaultPriority(), 0L);
	}

	public Writer<E> getWriter() throws IOException {
		return new Writer<E>(_store, _name, _info);
	}
	
	public ItemFactory<E> factory(){
		return event_factory;
	}
	
	public boolean isStateful(){
		return false;
	}

	@Override
	public boolean equals(Object o){
		Trace<?> t = (Trace<?>)o; 
		return t._name.equals(_name) && _store == t._store;
	}
}
