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

public class ClassPathStore extends Store {
	
	public ClassPathStore() throws IOException {
		super();
		for ( File f : new Reflections(infoFile).paths() )
			try {
				loadTrace(f.getParentFile().getName());
			} catch (LoadTraceException e) {
				System.err.println(e);
			}
	}
	
	public InputStream getInputStream(String name) throws IOException {
		return getClass().getClassLoader().getResourceAsStream(name);
	}
	
	@Override
	public boolean hasFile(String name) {
		return ( getClass().getClassLoader().getResource(name) != null );
	}
}
