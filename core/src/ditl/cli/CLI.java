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
package ditl.cli;

import java.io.*;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.jar.*;


public class CLI {
	
	public final static String default_package = "DEFAULT_PACKAGE";
	public final static String apps_ext = "apps";
	
	private Map<String, String> alias_map = new HashMap<String, String>();
	private Map<String, Set<String> > pkg_aliases = new HashMap<String, Set<String>>();
	private Map<String,List<CommandMap>> pkg_apps = new HashMap<String,List<CommandMap>>(); 
	
	private CLI() throws IOException {
		findPackages();
	}
		
	private void parseArgs(String[] args){
		if ( args.length > 0){
			String pkg = args[0];
			String[] pkg_args = Arrays.copyOfRange(args,1,args.length);
			String app, app_path;
			
			// check if pkg isn't really an app in the default package
			app_path = getAppPath(default_package, pkg);
			if ( app_path != null ){
				startApp(app_path, pkg_args);
				return;
			}
			
			// otherwise proceed normally
			pkg = alias_map.get(pkg);
			if ( pkg_args.length > 0 ){
				app = pkg_args[0];
				app_path = getAppPath(pkg, app);
				if ( app_path != null ){
					String[] app_args = Arrays.copyOfRange(pkg_args,1,pkg_args.length);
					startApp(app_path, app_args);
					return;
				}
			}
			
		}
		printHelp();
		System.exit(1);
		
	}
	
	private String getAppPath(String pkg, String app){
		List<CommandMap> maps = pkg_apps.get(pkg);
		if ( maps != null ){
			for ( CommandMap map : maps ){
				String path = map.target(app);
				if ( path != null )
					return path;
			}
		}
		return null;
	}
	
	private void printHelp(){
		StringBuffer buffer = new StringBuffer();
		buffer.append("Usage: [PACKAGE] CMD [CMD_OPTIONS]\n\n");
		buffer.append("Where PACKAGE is one of: \n\n");
		
		buffer.append("default package:\n");
		appendPackageHelp(buffer, default_package);
		for ( String pkg : pkg_apps.keySet() ){
			if ( ! pkg.equals(default_package) ){
				buffer.append("Package: ");
				appendAliased(buffer, pkg, pkg_aliases.get(pkg) );
				buffer.append("\n");
				appendPackageHelp(buffer, pkg);
			}
		}
		buffer.append("\n");
		buffer.append("To get help on a particular command, run CMD --help");
		System.out.println(buffer.toString());
	}
	
	private void appendPackageHelp(StringBuffer buffer, String pkg_name){
		for ( CommandMap cmd_map : pkg_apps.get(pkg_name) ){
			for ( String app : cmd_map.commands() ){
				buffer.append("    ");
				appendAliased(buffer, app, cmd_map.aliases(app));
				buffer.append("\n");
			}
		}
		buffer.append("\n");
	}
	
	private String appendAliased(StringBuffer buffer, String app, Collection<String> aliases){
		buffer.append(app);
		if ( ! aliases.isEmpty() ){
			buffer.append(" (");
			for ( Iterator<String> i=aliases.iterator(); i.hasNext(); ){
				String alias = i.next();
				buffer.append(alias);
				if ( i.hasNext() )
					buffer.append(", ");
			}
			buffer.append(")");
		}
		return buffer.toString();
	}
	
	private void startApp(String path, String[] args){
		try {
			Class<?> klass = Class.forName(path, true, getClass().getClassLoader());
			Class<? extends App> appClass = klass.asSubclass(App.class);
			Constructor<? extends App> ctor = appClass.getConstructor(new Class[]{String[].class});
			App app = ctor.newInstance((Object)args);
			app.exec();
		} catch (Throwable e) {
			e.printStackTrace();
			System.err.println(e);
			System.exit(1);
		}
	}
	
	private void findPackages() throws IOException{
		for ( String elem : System.getProperty("java.class.path").split(":") ){
			File path = new File(elem); 
			if ( path.isFile() )
				findPackagesFromJar(path);
			else if ( path.isDirectory() )
				findPackagesFromDir(path);
		}
	}
	
	private void findPackagesFromJar(File file) throws IOException {
		JarFile jarFile = new JarFile(file);
		Enumeration<JarEntry> entries = jarFile.entries();
		while ( entries.hasMoreElements() ){
			JarEntry entry = entries.nextElement();
			String path = entry.getName();
			if ( path.endsWith(apps_ext) ){
				CommandMap map = new CommandMap(jarFile.getInputStream(entry));
				addCommandMap(map);
			}
		}
	}
	
	private void findPackagesFromDir(File dir) throws IOException {
		for ( File file : dir.listFiles() ){
			if ( file.isDirectory() ) {
				findPackagesFromDir(file);
			} else if ( file.isFile() && file.getPath().endsWith(apps_ext) ){
				CommandMap map = new CommandMap(new FileInputStream(file));
				addCommandMap(map);
			}
		}
	}
	
	private void addCommandMap(CommandMap cmd_map){
		String pkg_name = cmd_map.pkgName();
		if ( pkg_name == null )
			pkg_name = default_package;
		Set<String> aliases = cmd_map.pkgAliases();
		if ( ! pkg_apps.containsKey(pkg_name) )
			pkg_apps.put(pkg_name, new LinkedList<CommandMap>());
		pkg_apps.get(pkg_name).add(cmd_map);
		if ( ! pkg_aliases.containsKey(pkg_name) )
			pkg_aliases.put(pkg_name, new LinkedHashSet<String>());
		pkg_aliases.get(pkg_name).addAll(aliases);
		for ( String alias : aliases )
			alias_map.put(alias, pkg_name);
		alias_map.put(pkg_name, pkg_name);
	}
	
	public static void main(String args[]) throws IOException{
		new CLI().parseArgs(args);
	}
}
