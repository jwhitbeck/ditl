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
package ditl.graphs;

import java.io.*;
import java.util.*;

import ditl.*;



public class CRAWDADEdges {
	
	public static void fromCRAWDAD(EdgeTrace edges,
			InputStream in, double timeMul, long ticsPerSecond,
			long offset, long snapInterval) throws IOException{
		
		StatefulWriter<EdgeEvent,Edge> edgeWriter = edges.getWriter(snapInterval);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String line;
		
		while ( (line=br.readLine()) != null ){
			String[] elems = line.split("[ \t]+");
			Integer id1 = Integer.parseInt(elems[0]);
			Integer id2 = Integer.parseInt(elems[1]);
			long begin = (long)(Long.parseLong(elems[2])*timeMul)+offset;
			long end = (long)(Long.parseLong(elems[3])*timeMul)+offset;
			edgeWriter.queue(begin, new EdgeEvent(id1,id2,EdgeEvent.UP));
			edgeWriter.queue(end, new EdgeEvent(id1,id2,EdgeEvent.DOWN));
		}
		edgeWriter.flush();
		edgeWriter.setProperty(Trace.ticsPerSecondKey, ticsPerSecond);
		edgeWriter.close();
		br.close();
	}
	
	public static void toCRAWDAD(EdgeTrace edges, 
			OutputStream out, double timeMul) throws IOException {
	
		StatefulReader<EdgeEvent,Edge> edgeReader = edges.getReader();
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
		Map<Edge,Long> activeEdges = new HashMap<Edge,Long>();
		
		edgeReader.seek(edges.minTime());
		for ( Edge e : edgeReader.referenceState() )
			activeEdges.put(e, edges.minTime());
		while ( edgeReader.hasNext() ){
			for ( EdgeEvent eev : edgeReader.next() ){
				Edge e = eev.edge();
				if ( eev.isUp() ){
					activeEdges.put(e, edgeReader.time());
				} else {
					double beg = activeEdges.get(e)*timeMul;
					double end = edgeReader.time()*timeMul;
					activeEdges.remove(e);
					bw.write(e.from()+"\t"+e.to()+"\t"+beg+"\t"+end+"\n");
				}
			}
		}
		
		bw.close();
		edgeReader.close();
	}
	
}
