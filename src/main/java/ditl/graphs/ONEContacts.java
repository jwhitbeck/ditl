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

import ditl.*;


public class ONEContacts {
	
	public static void fromONE(EdgeTrace edges, 
			InputStream in, double timeMul, long ticsPerSecond,
			long offset, IdGenerator idGen ) throws IOException {
		StatefulWriter<EdgeEvent,Edge> edgeWriter = edges.getWriter();
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String line;
		while ( (line=br.readLine()) != null ){
			String[] elems = line.split("[ \t]+");
			long time = (long)(Double.parseDouble(elems[0])*timeMul)+offset;
			Integer id1 = idGen.getInternalId(elems[2]);
			Integer id2 = idGen.getInternalId(elems[3]);
			String action = elems[4].toUpperCase();
			if ( action.equals("UP") )
				edgeWriter.append(time, new EdgeEvent(id1, id2, EdgeEvent.UP));
			else
				edgeWriter.append(time, new EdgeEvent(id1, id2, EdgeEvent.DOWN));
		}
		edgeWriter.setProperty(Trace.timeUnitKey, Units.toTimeUnit(ticsPerSecond));
		edgeWriter.close();
		br.close();
	}
	
	public static void toONE(EdgeTrace edges,
			OutputStream out, double timeMul) throws IOException {
		
		StatefulReader<EdgeEvent,Edge> edgeReader = edges.getReader();
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
		
		edgeReader.seek(edges.minTime());
		for ( Edge edge : edgeReader.referenceState() )
			bw.write(edges.maxTime()*timeMul+" CONN "+edge+" UP\n");
		while ( edgeReader.hasNext() )
			for ( EdgeEvent ev : edgeReader.next() )
				bw.write(edgeReader.time()*timeMul+" CONN "+ev+"\n");
			
		bw.close();
		edgeReader.close();
	}
}
