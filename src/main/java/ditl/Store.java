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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONObject;

import org.reflections.Reflections;
import org.reflections.scanners.TypeAnnotationsScanner;

public abstract class Store {

    final protected static String snapshotsFile = "snapshots";
    final protected static String infoFile = "info";
    final protected static String traceFile = "trace";
    final protected static String indexFile = "index";

    protected String separator = "/";

    final Map<String, Trace<?>> traces = new HashMap<String, Trace<?>>();
    final static Map<String, Class<? extends Trace<?>>> type_class_map = buildTypeClassMap();

    private final Set<Reader<?>> openReaders = new HashSet<Reader<?>>();
    private boolean closing = false;

    @SuppressWarnings("unchecked")
    private static Map<String, Class<? extends Trace<?>>> buildTypeClassMap() {
        final Map<String, Class<? extends Trace<?>>> type_class = new HashMap<String, Class<? extends Trace<?>>>();
        if (type_class.isEmpty()) {
            final Set<Class<?>> traceClasses = new Reflections("ditl", new TypeAnnotationsScanner()).
                    getTypesAnnotatedWith(Trace.Type.class);
            for (final Class<?> klass : traceClasses)
                if (Trace.class.isAssignableFrom(klass))
                    type_class.put(klass.getAnnotation(Trace.Type.class).value(),
                            (Class<? extends Trace<?>>) klass);
        }
        return type_class;
    }

    String traceFile(String name) {
        return name + separator + traceFile;
    }

    String infoFile(String name) {
        return name + separator + infoFile;
    }

    String indexFile(String name) {
        return name + separator + indexFile;
    }

    public Collection<Trace<?>> listTraces() {
        return traces.values();
    }

    @SuppressWarnings("unchecked")
    public <T extends Trace<?>> List<T> listTraces(Class<T> klass) {
        final List<T> list = new LinkedList<T>();
        for (final Trace<?> trace : traces.values())
            if (klass.equals(trace.getClass()))
                list.add((T) trace);
        return list;
    }

    public abstract boolean hasFile(String name);

    public boolean hasTrace(String name) {
        return traces.containsKey(name);
    }

    @SuppressWarnings("unchecked")
    public <T extends Trace<?>> T getTrace(String name) throws IOException {
        final T trace = (T) traces.get(name);
        if (trace == null)
            throw new IOException("No such trace '" + name + "'");
        return trace;
    }

    public abstract InputStream getInputStream(String name) throws IOException;

    public String getTraceResource(Trace<?> trace, String resource) throws IOException {
        return trace.name() + separator + resource;
    }

    private String getFileAsString(String path) throws IOException {
        StringWriter sw = new StringWriter();
        BufferedReader bis = new BufferedReader(new InputStreamReader(getInputStream(path)));
        String line = null;
        while ((line = bis.readLine()) != null)
            sw.write(line);
        return sw.toString();
    }

    public static Store open(File... files) throws IOException, ClassNotFoundException {
        switch (files.length) {
            case 0:
                return new ClassPathStore();
            case 1:
                if (files[0].isDirectory())
                    return new DirectoryStore(files[0]);
                return new JarStore(files[0]);
            default:
                return new MultiStore(files);
        }
    }

    void notifyClose(Reader<?> reader) {
        if (!closing)
            openReaders.remove(reader);
    }

    void notifyOpen(Reader<?> reader) {
        openReaders.add(reader);
    }

    public void close() throws IOException {
        closing = true;
        for (final Reader<?> reader : openReaders)
            reader.close();
        openReaders.clear();
        closing = false;
    }

    public Class<? extends Trace<?>> getTraceClass(String type) throws ClassNotFoundException {
        if (!type_class_map.containsKey(type))
            throw new ClassNotFoundException("No trace class found for type '" + type + "'");
        return type_class_map.get(type);
    }

    public void loadTrace(String name) throws IOException, ClassNotFoundException {
        final JSONObject config = JSONObject.fromObject(getFileAsString(infoFile(name)));
        final Trace<?> trace = buildTrace(name, config, type_class_map.get(config.get(Trace.typeKey)));
        traces.put(name, trace);
    }

    @SuppressWarnings("unchecked")
    <T extends Trace<?>> T buildTrace(String name, JSONObject config, Class<T> klass) throws ClassNotFoundException {
        try {
            final Constructor<?> ctor = klass.getConstructor(new Class[] { Store.class, String.class, JSONObject.class });
            config.put(Trace.typeKey, klass.getAnnotation(Trace.Type.class).value());
            return (T) ctor.newInstance(this, name, config);
        } catch (final Exception e) {
            throw new ClassNotFoundException("Failed to instantiate trace class for '" + name + "'");
        }
    }
}
