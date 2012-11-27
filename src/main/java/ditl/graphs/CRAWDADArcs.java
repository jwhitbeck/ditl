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
package ditl.graphs;

import java.io.*;
import java.util.*;

import ditl.*;



public class CRAWDADArcs {
	
	public static void fromCRAWDAD(ArcTrace arcs,
			InputStream in, double timeMul, long ticsPerSecond,
			long offset, IdGenerator idGen) throws IOException{
		
		StatefulWriter<ArcEvent,Arc> arcWriter = arcs.getWriter();
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String line;
		
		while ( (line=br.readLine()) != null ){
			String[] elems = line.split("[ \t]+");
			Integer id1 = idGen.getInternalId(elems[0]);
			Integer id2 = idGen.getInternalId(elems[1]);
			long begin = (long)(Long.parseLong(elems[2])*timeMul)+offset;
			long end = (long)(Long.parseLong(elems[3])*timeMul)+offset;
			arcWriter.queue(begin, new ArcEvent(id1,id2,ArcEvent.Type.UP));
			arcWriter.queue(end, new ArcEvent(id1,id2,ArcEvent.Type.DOWN));
		}
		arcWriter.flush();
		arcWriter.setProperty(Trace.timeUnitKey, Units.toTimeUnit(ticsPerSecond));
		idGen.writeTraceInfo(arcWriter);
		arcWriter.close();
		br.close();
	}
	
	public static void toCRAWDAD(ArcTrace arcs, 
			OutputStream out, double timeMul) throws IOException {
	
		StatefulReader<ArcEvent,Arc> arcReader = arcs.getReader();
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
		Map<Arc,Long> activeArcs = new AdjacencyMap.Arcs<Long>();
		
		arcReader.seek(arcs.minTime());
		for ( Arc a : arcReader.referenceState() )
			activeArcs.put(a, arcs.minTime());
		while ( arcReader.hasNext() ){
			for ( ArcEvent aev : arcReader.next() ){
				Arc a = aev.arc();
				if ( aev.isUp() ){
					activeArcs.put(a, arcReader.time());
				} else {
					double beg = activeArcs.get(a)*timeMul;
					double end = arcReader.time()*timeMul;
					activeArcs.remove(a);
					bw.write(a.from()+"\t"+a.to()+"\t"+beg+"\t"+end+"\n");
				}
			}
		}
		
		bw.close();
		arcReader.close();
	}
	
}
