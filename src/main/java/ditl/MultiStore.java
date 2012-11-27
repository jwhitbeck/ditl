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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class MultiStore extends Store {

    final private Store[] stores;

    public MultiStore(File... files) throws IOException {
        super();
        stores = new Store[files.length];
        for (int i = 0; i < files.length; ++i) {
            stores[i] = Store.open(files[i]);
            for (final Trace<?> trace : stores[i].listTraces()) {
                final String name = trace.name();
                if (hasTrace(name))
                    System.err.println("Warning: trace '" + name + "' exists in multiple stores. Only keeping first occurence.");
                else
                    traces.put(name, trace);
            }
        }
    }

    @Override
    public InputStream getInputStream(String name) throws IOException {
        for (final Store store : stores)
            if (store.hasFile(name))
                return store.getInputStream(name);
        throw new IOException();
    }

    @Override
    public boolean hasFile(String name) {
        for (final Store store : stores)
            if (store.hasFile(name))
                return true;
        return false;
    }
}
