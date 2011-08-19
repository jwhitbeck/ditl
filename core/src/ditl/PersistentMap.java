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

import java.util.*;
import java.io.*;

@SuppressWarnings("serial")
public class PersistentMap extends LinkedHashMap<String,String> {
	
	public void read ( InputStream in ) throws IOException {
		BufferedReader br = new BufferedReader ( new InputStreamReader( in ) );
		String line;
		while ( (line = br.readLine()) != null ){
			line = line.trim();
			if ( ! line.isEmpty() && line.charAt(0) != '#' && line.charAt(0) != ';'){ // not a comment
				String[] elems = line.split(":",2);
				if ( elems.length == 2 ){ // key, value pair
					put(elems[0].trim(), elems[1].trim() );
				}
			}
		}
		br.close();
	}
	
	
	public void save (OutputStream out) throws IOException {
		BufferedWriter bw = new BufferedWriter ( new OutputStreamWriter( out ) );
		for ( Map.Entry<String, String> e : entrySet() ){
			bw.write(e.getKey()+": "+e.getValue()+"\n");
		}
		bw.close();
	}
}
