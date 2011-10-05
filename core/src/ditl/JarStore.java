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

import java.io.*;
import java.util.jar.*;

public class JarStore extends Store {
	
	private File _file;
	private JarFile jar_file;
	
	public JarStore(File file) throws IOException {
		_file = file;
		jar_file = new JarFile(_file);
		for ( File f : new Reflections(infoFile, _file).paths() )
			try {
				loadTrace(f.getParentFile().getName());
			} catch (LoadTraceException e) {
				System.err.println(e);
			}
	}

	public InputStream getInputStream(String name) throws IOException {
		JarEntry e = jar_file.getJarEntry(name);
		if ( e == null) throw new IOException();
		return jar_file.getInputStream(e);
	}
	
	@Override
	public boolean hasFile(String name) {
		return ( jar_file.getEntry(name) != null );
	}
}
