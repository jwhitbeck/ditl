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

import java.util.*;

public class IdMap {

	private final static String id_sep = ",";
	private final static String pair_sep = ":";
	
	private Map<Integer,String> iid_map = new HashMap<Integer,String>();
	private Map<String,Integer> eid_map = new HashMap<String,Integer>();
	
	public IdMap( String idMapString ){
		String[] pairs = idMapString.split(id_sep);
		for ( String pair : pairs ){
			String[] elems = pair.split(pair_sep);
			Integer iid = Integer.parseInt(elems[0]);
			String eid = elems[1];
			iid_map.put(iid, eid);
			eid_map.put(eid, iid);
		}
	}
	
	public String getExternalId(Integer internalId){
		String eid = iid_map.get(internalId);
		if ( eid == null )
			return internalId.toString();
		return eid;
	}
	
	public Integer getInternalId(String externalId){
		return eid_map.get(externalId);
	}
	
	public static class Writer implements IdGenerator {
		
		private Map<String,Integer> eid_map = new LinkedHashMap<String,Integer>();
		private Integer next = 0;
		
		public Writer(Integer minId){
			next = minId;
		}
		
		public static Writer filter( IdMap idMap, Set<Integer> group ){
			Writer writer = new Writer(0);
			Iterator<Map.Entry<Integer, String>> i = idMap.iid_map.entrySet().iterator();
			while ( i.hasNext() ){
				Map.Entry<Integer, String> e = i.next();
				if ( group.contains ( e.getKey() ) )
					writer.eid_map.put(e.getValue(), e.getKey());
			}
			return writer;
		}
		
		
		public void merge ( IdMap idMap ){
			for ( Map.Entry<Integer, String> e : idMap.iid_map.entrySet() ){
				Integer iid = e.getKey();
				if ( next <= iid )
					next = iid+1;
				eid_map.put(e.getValue(), iid);
			}
		}
		
		@Override
		public Integer getInternalId(String externalId){
			Integer iid = eid_map.get(externalId);
			if ( iid == null ){
				iid = next;
				eid_map.put(externalId, iid);
				next++;
			}
			return iid;
		}

		@Override
		public void writeTraceInfo(ditl.Writer<?> writer) {
			StringBuffer buffer = new StringBuffer();
			Iterator<Map.Entry<String, Integer>> i = eid_map.entrySet().iterator();
			while ( i.hasNext() ){
				Map.Entry<String, Integer> e = i.next();
				buffer.append(e.getValue()+pair_sep+e.getKey());
				if ( i.hasNext() )
					buffer.append(id_sep);
			}
			writer.setProperty(Trace.idMapKey, buffer.toString());
		}
	}
}
