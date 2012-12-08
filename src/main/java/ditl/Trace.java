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
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;

import net.sf.json.JSONObject;

public abstract class Trace<E extends Item> {

    @Target({ ElementType.TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface Type {
        String value();
    }

    final public static String
            nameKey = "name",
            typeKey = "type",
            timeUnitKey = "time unit",
            descriptionKey = "description",
            maxUpdateIntervalKey = "max update interval",
            minTimeKey = "min time",
            maxTimeKey = "max time",
            defaultPriorityKey = "default priority",
            idMapKey = "id map";

    final public static int
            defaultPriority = 100,
            highestPriority = 0,
            lowestPriority = Integer.MAX_VALUE;

    final private String _name;
    final Store _store;
    final protected JSONObject config;

    protected Item.Factory<E> event_factory;

    public interface Copyable<E extends Item> {
        public void copyOverTraceInfo(Writer<E> writer);
    }

    public interface Filterable<E extends Item> extends Copyable<E> {
        public Filter<E> eventFilter(Set<Integer> group);
    }

    public Trace(Store store, String name, JSONObject jsonConfig, Item.Factory<E> itemFactory) throws IOException {
        _store = store;
        _name = name;
        config = jsonConfig;
        event_factory = itemFactory;
    }

    public String name() {
        return _name;
    }

    String indexFile() {
        return _store.indexFile(_name);
    }

    String traceFile() {
        return _store.traceFile(_name);
    }

    String infoFile() {
        return _store.infoFile(_name);
    }

    public String description() {
        return config.getString(descriptionKey);
    }

    public String type() {
        return config.getString(typeKey);
    }

    public long minTime() {
        return config.getLong(minTimeKey);
    }

    public long maxTime() {
        return config.getLong(maxTimeKey);
    }

    public long maxUpdateInterval() {
        return config.getLong(maxUpdateIntervalKey);
    }

    public int defaultPriority() {
        return config.getInt(defaultPriorityKey);
    }

    public long ticsPerSecond() {
        return Units.getTicsPerSecond(timeUnit());
    }

    public String timeUnit() {
        return config.getString(timeUnitKey);
    }

    public IdMap idMap() {
        if (!config.has(idMapKey))
            return null;
        return new IdMap(config.getJSONObject(idMapKey));
    }

    public Reader<E> getReader(int priority, long offset) throws IOException {
        return new Reader<E>(this, priority, offset);
    }

    public Reader<E> getReader(int priority) throws IOException {
        return getReader(priority, 0L);
    }

    public Reader<E> getReader() throws IOException {
        return getReader(defaultPriority(), 0L);
    }

    public Writer<E> getWriter() throws IOException {
        return new Writer<E>(this);
    }

    public Item.Factory<E> factory() {
        return event_factory;
    }

    public void set(String key, Object value) {
        config.accumulate(key, value);
    }

    public void setIfUnset(String key, Object value) {
        if (!config.containsKey(key))
            config.accumulate(key, value);
    }

    @Override
    public boolean equals(Object o) {
        final Trace<?> t = (Trace<?>) o;
        return t._name.equals(_name) && _store == t._store;
    }
}
