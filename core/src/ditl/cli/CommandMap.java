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
import java.util.*;

import ditl.PersistentMap;

class CommandMap {
	
	public final static String PKG_NAME = "PKG_NAME";
	public final static String PKG_ALIASES = "PKG_ALIASES";
	
	private Map<String,String> cmd_map = new HashMap<String,String>();
	private Map<String,Set<String>> alias_map = new HashMap<String,Set<String>>();
	
	private String pkg_name = null;
	private Set<String> pkg_aliases = new LinkedHashSet<String>();
	
	CommandMap(InputStream in) throws IOException{
		PersistentMap map = new PersistentMap();
		map.read(in);
		for ( Map.Entry<String, String> e : map.entrySet() ){
			String key = e.getKey();
			String target = e.getValue();
			if ( key.equals(PKG_NAME) ){
				pkg_name = target;
			} else if ( key.equals(PKG_ALIASES) ){
				String[] aliases = target.split("[ \t]*,[ \t]*");
				for ( String alias : aliases ){
					pkg_aliases.add(alias);
				}
			} else {
				String[] cmds = key.split("[ \t]*,[ \t]*");
				String cmd = cmds[0];
				for ( String s : cmds )
					cmd_map.put(s, target);
				Set<String> aliases = new LinkedHashSet<String>();
				int i=1;
				while ( i<cmds.length ){
					aliases.add(cmds[i]);
					++i;
				}
				alias_map.put(cmd, aliases);
			}
		}
	}
	
	Set<String> commands(){
		return alias_map.keySet();
	}
	
	String target(String cmd){
		return cmd_map.get(cmd);
	}
	
	Set<String> pkgAliases(){
		return pkg_aliases;
	}
	
	String pkgName(){
		return pkg_name;
	}
	
	Set<String> aliases(String cmd){
		return alias_map.get(cmd);
	}
	
	Set<String> targets(){
		return new HashSet<String>(cmd_map.values());
	}

}
