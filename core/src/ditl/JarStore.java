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
import java.lang.reflect.Method;
import java.net.*;
import java.util.Enumeration;
import java.util.jar.*;

public class JarStore extends Store {
	
	private static final Class<?>[] parameters = new Class[]{URL.class};
	
	public JarStore() {}
	
	public JarStore(File jarFile) throws IOException {
		add( jarFile );
	}
	
	public void add(File file) throws IOException {
		URL url = file.toURI().toURL();
		
		URLClassLoader sysloader = (URLClassLoader)ClassLoader.getSystemClassLoader();
        Class<?> sysclass = URLClassLoader.class;
        try {
            Method method = sysclass.getDeclaredMethod("addURL",parameters);
            method.setAccessible(true);
            method.invoke(sysloader,new Object[]{ url }); 
        } catch (Throwable t) {
            t.printStackTrace();
            throw new IOException("Error, could not add URL to system classloader");
        }
		
		JarFile jarFile = new JarFile(file);
		Enumeration<JarEntry> entries = jarFile.entries();
		while ( entries.hasMoreElements() ){
			JarEntry entry = entries.nextElement();
			String path = entry.getName();
			if ( path.endsWith(infoFile) ){
				String parent = path.substring(0, path.length()-infoFile.length()-1);
				loadTrace(parent);
			}
		}
	}

	public InputStream getInputStream(String name) throws IOException {
		return getClass().getClassLoader().getResourceAsStream(name);
	}
	
	
	@Override
	public Trace getTrace(String name) {
		Trace trace = super.getTrace(name);
		if ( trace != null )
			return trace;
		try {
			return loadTrace(name);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public boolean hasTrace(String name){
		if ( super.hasTrace(name) )
			return true;
		return hasFile(infoFile(name));
	}

	@Override
	public boolean hasFile(String name) {
		return ( getClass().getClassLoader().getResource(name) != null );
	}
}
