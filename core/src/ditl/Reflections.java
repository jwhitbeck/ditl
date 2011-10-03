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
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.jar.*;
import java.util.regex.Pattern;

public class Reflections {
	
	Set<File> paths = new HashSet<File>();
	Pattern regex = null;
	
	public Reflections(String filter, File... files) throws IOException {
		if ( filter != null ){
			regex = Pattern.compile(filter);
		}
		for ( File file : files ){
			scan(file);
		}
		if ( files.length == 0 ){
			for ( String elem : System.getProperty("java.class.path").split(":") ){
				File file = new File(elem);
				scan(file);
			}
		}
	}
	
	private void scan(File file) throws IOException {
		if ( file.exists() ){
			if ( file.isDirectory() ){
				recAddFromDir(file);
			} else {
				addFromJarFile(file);
			}
		}
	}
	
	private void add(File file){
		if ( regex != null ){
			if ( regex.matcher(file.getPath()).find() )
				paths.add(file);
		} else {
			paths.add(file);
		}
				
	}
	
	private void recAddFromDir(File dir){
		for ( File file : dir.listFiles() ){
			add(file);
			if ( file.isDirectory() ){
				recAddFromDir(file);
			}
		}
	}
	
	private void addFromJarFile(File file) throws IOException{
		JarFile jarFile = new JarFile(file);
		Enumeration<JarEntry> entries = jarFile.entries();
		while ( entries.hasMoreElements() ){
			JarEntry entry = entries.nextElement();
			add(new File(entry.getName()));
		}
	}
	
	public Set<File> paths(){
		return paths;
	}
	
	public Set<Class<?>> listClasses(String pkgName){
		Set<Class<?>> klasses = new HashSet<Class<?>>();
		for ( File file : paths ){
			String fileName = file.getName();
			if ( fileName.endsWith(".class") && ! fileName.contains("$")){
				String klassName = fileName.replace(".class", "");
				File elem = file.getParentFile();
				while ( elem != null ){
					String elemName = elem.getName();
					klassName = elemName + "." + klassName;
					if ( elemName.equals(pkgName) )
						break;
					elem = elem.getParentFile();
				}
				try {
					Class<?> klass = Class.forName(klassName);
					klasses.add(klass);
				} catch (ClassNotFoundException e) {
					System.err.println(e);
				}
			}
		}
		return klasses;
	}
	
	public static Set<Class<?>> getSubClasses(Class<?> klass, Set<Class<?>> allClasses){
		Set<Class<?>> subClasses = new HashSet<Class<?>>();
		for ( Class<?> k : allClasses ){
			if ( klass.isAssignableFrom(k) ){
				if ( ! Modifier.isAbstract(k.getModifiers())){
					subClasses.add(k);
				}
			}
		}
		return subClasses;
	}
}
