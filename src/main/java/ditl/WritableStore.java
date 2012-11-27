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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public abstract class WritableStore extends Store {

    private final Map<String, Writer<?>> openWriters = new HashMap<String, Writer<?>>();

    @SuppressWarnings("serial")
    public static class AlreadyExistsException extends Exception {
        private final String trace_name;

        public AlreadyExistsException(String traceName) {
            trace_name = traceName;
        }

        @Override
        public String toString() {
            return "Error! A trace named '" + trace_name + "' already exists.";
        }
    }

    public WritableStore() throws IOException {
        super();
    }

    public abstract void deleteFile(String name) throws IOException;

    public abstract void deleteTrace(String name) throws IOException;

    public abstract OutputStream getOutputStream(String name) throws IOException;

    public abstract void moveTrace(String origName, String destName, boolean force) throws AlreadyExistsException, IOException;

    public void putFile(File file, String name) throws IOException {
        copy(new FileInputStream(file), getOutputStream(name));
    }

    public void copy(InputStream ins, OutputStream outs) throws IOException {
        final BufferedOutputStream out = new BufferedOutputStream(outs);
        final BufferedInputStream in = new BufferedInputStream(ins);
        int c;
        while ((c = in.read()) != -1)
            out.write(c);
        in.close();
        out.close();
    }

    void notifyClose(String name) throws IOException {
        openWriters.remove(name);
        try {
            loadTrace(name);
        } catch (final LoadTraceException e) {
            System.err.println(e);
        }
    }

    void notifyOpen(String name, Writer<?> writer) throws IOException {
        openWriters.put(name, writer);
    }

    boolean isAlreadyWriting(String name) {
        return openWriters.containsKey(name);
    }

    @Override
    public void close() throws IOException {
        super.close();
        for (final Writer<?> writer : openWriters.values())
            writer.close();
    }

    public static WritableStore open(File file) throws IOException {
        if (file.exists()) {
            if (file.isDirectory())
                return new DirectoryStore(file);
            return new JarDirectoryStore(file);
        } else {
            if (file.getName().endsWith(".jar"))
                return new JarDirectoryStore(file);
            return new DirectoryStore(file);
        }
    }

    public void copyTrace(Store store, Trace<?> trace) throws IOException {
        final String[] files = new String[] {
                infoFile(trace.name()),
                (trace.isStateful()) ? snapshotsFile(trace.name()) : null,
                traceFile(trace.name()) };
        for (final String file : files)
            if (file != null) {
                final InputStream in = store.getInputStream(file);
                final OutputStream out = getOutputStream(file);
                copy(in, out);
            }
    }

    public Trace<?> newTrace(String name, String type, boolean force) throws AlreadyExistsException, LoadTraceException {
        return newTrace(name, getTraceClass(type), force);
    }

    public Trace<?> newTrace(String name, Class<? extends Trace<?>> klass, boolean force) throws AlreadyExistsException, LoadTraceException {
        if (traces.containsKey(name) && !force)
            throw new AlreadyExistsException(name);
        final Trace<?> trace = buildTrace(name, new PersistentMap(), klass);
        return trace;
    }

}
