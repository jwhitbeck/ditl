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



public class CRAWDADContacts {
	
	public static void fromCRAWDAD(LinkTrace links,
			InputStream in, double timeMul, long ticsPerSecond,
			long offset, long snapInterval, IdGenerator idGen) throws IOException{
		
		StatefulWriter<LinkEvent,Link> linkWriter = links.getWriter(snapInterval);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String line;
		
		while ( (line=br.readLine()) != null ){
			String[] elems = line.split("[ \t]+");
			Integer id1 = idGen.getInternalId(elems[0]);
			Integer id2 = idGen.getInternalId(elems[1]);
			long begin = (long)(Long.parseLong(elems[2])*timeMul)+offset;
			long end = (long)(Long.parseLong(elems[3])*timeMul)+offset;
			linkWriter.queue(begin, new LinkEvent(id1,id2,LinkEvent.UP));
			linkWriter.queue(end, new LinkEvent(id1,id2,LinkEvent.DOWN));
		}
		linkWriter.flush();
		linkWriter.setProperty(Trace.timeUnitKey, Units.toTimeUnit(ticsPerSecond));
		idGen.writeTraceInfo(linkWriter);
		linkWriter.close();
		br.close();
	}
	
	public static void toCRAWDAD(LinkTrace links, 
			OutputStream out, double timeMul) throws IOException {
	
		StatefulReader<LinkEvent,Link> linkReader = links.getReader();
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
		Map<Link,Long> activeContacts = new AdjacencyMap.Links<Long>();
		
		linkReader.seek(links.minTime());
		for ( Link l : linkReader.referenceState() )
			activeContacts.put(l, links.minTime());
		while ( linkReader.hasNext() ){
			for ( LinkEvent lev : linkReader.next() ){
				Link l = lev.link();
				if ( lev.isUp() ){
					activeContacts.put(l, linkReader.time());
				} else {
					double b = activeContacts.get(l)*timeMul;
					double e = linkReader.time()*timeMul;
					activeContacts.remove(l);
					bw.write(l.id1()+"\t"+l.id2()+"\t"+b+"\t"+e+"\n");
				}
			}
		}
		linkReader.close();
		bw.close();
	}
	
}
