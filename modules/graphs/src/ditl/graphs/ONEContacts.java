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

import ditl.*;


public class ONEContacts {
	
	public static void fromONE(LinkTrace links, 
			InputStream in, double timeMul, long ticsPerSecond,
			long offset, long snapInterval, IdGenerator idGen ) throws IOException {
		StatefulWriter<LinkEvent,Link> linkWriter = links.getWriter(snapInterval);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String line;
		while ( (line=br.readLine()) != null ){
			String[] elems = line.split(" ");
			long time = (long)(Double.parseDouble(elems[0])*timeMul)+offset;
			Integer id1 = idGen.getInternalId(elems[2]);
			Integer id2 = idGen.getInternalId(elems[3]);
			String action = elems[4].toUpperCase();
			if ( action.equals("UP") )
				linkWriter.append(time, new LinkEvent(id1, id2, LinkEvent.UP));
			else
				linkWriter.append(time, new LinkEvent(id1, id2, LinkEvent.DOWN));
		}
		linkWriter.setProperty(Trace.timeUnitKey, Units.toTimeUnit(ticsPerSecond));
		linkWriter.close();
		br.close();
	}
	
	public static void toONE(LinkTrace links,
			OutputStream out, double timeMul) throws IOException {
		
		StatefulReader<LinkEvent,Link> linkReader = links.getReader();
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
		
		linkReader.seek(links.minTime());
		for ( Link link : linkReader.referenceState() )
			bw.write(links.maxTime()*timeMul+" CONN "+link+" UP\n");
		while ( linkReader.hasNext() )
			for ( LinkEvent ev : linkReader.next() )
				bw.write(linkReader.time()*timeMul+" CONN "+ev+"\n");
			
		bw.close();
		linkReader.close();
	}
}
