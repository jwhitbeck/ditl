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



public class ONEMovement {

	public static void fromONE(StatefulWriter<MovementEvent,Movement> movementWriter,
			InputStream in, Long maxTime, final double timeMul, long offset) throws IOException {
		
		TreeMap<Long,List<Movement>> buffer = new TreeMap<Long,List<Movement>>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(in) );
		String line;
		Bounds bounds = new Bounds();
		reader.readLine(); // burn first line
		while ( (line = reader.readLine()) != null ){
			String[] elems = line.split(" ");
			long t = (long)(Double.parseDouble(elems[0])*timeMul)+offset;
			int id = Integer.parseInt(elems[1]);
			double x = Double.parseDouble(elems[2]);
			bounds.updateX(x);
			double y = Double.parseDouble(elems[3]);
			bounds.updateY(y);
			if ( ! buffer.containsKey(t) )
				buffer.put(t, new LinkedList<Movement>());
			buffer.get(t).add(new Movement(id, new Point(x,y)));
		}
		reader.close();
		
		Map<Integer,Point> points = new HashMap<Integer,Point>();
		
		Collection<Movement> first = null;
		long last_time = -Trace.INFINITY;

		while ( ! buffer.isEmpty() ){
			Map.Entry<Long, List<Movement>> e = buffer.pollFirstEntry();
			long time = e.getKey()+offset;
			List<Movement> events = e.getValue();
			double dt = time-last_time;
			if ( first == null ){
				first = events;
			} else if (points.isEmpty()){
				for ( Movement m : events )
					points.put(m.id(), m.from());
				for ( Movement m : first ){
					Point dest = points.get(m.id());
					double s = speed(m.from(), dest, dt);
					m.setNewDestination(last_time, dest, s);
				}
				movementWriter.setInitState(last_time, first);
			} else {
				for ( Movement m : events ){
					Point dest = m.from();
					double s = speed(points.get(m.id()), dest, dt);
					movementWriter.append(last_time, new MovementEvent(m.id(), s, dest));
					points.put(m.id(), dest); // update last point
				}
			}
			last_time = time;
		}
		last_time = (maxTime != null)? maxTime : last_time; 
		movementWriter.setProperty(Trace.maxTimeKey, last_time);
		bounds.writeToTrace(movementWriter);
		movementWriter.close();
	}
	
	public static void toONE(StatefulReader<MovementEvent,Movement> movementReader, 
			OutputStream out, final double timeMul, long interval, Long maxTime) throws IOException { 
		final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
		MovementTrace movement = new MovementTrace(movementReader.trace());
		// write initial ONE line
		writer.write(movement.minTime()*timeMul+" "+movement.maxTime()*timeMul+" ");
		writer.write(movement.minX()+" "+movement.maxX()+" ");
		writer.write(movement.minY()+" "+movement.maxY()+"\n");
		// print all positions every interval
		final MovementUpdater updater = new MovementUpdater();
		Bus<MovementEvent> movementEventBus = new Bus<MovementEvent>();
		Bus<Movement> movementBus = new Bus<Movement>();
		movementEventBus.addListener( new Listener<MovementEvent>(){
			@Override
			public void handle(long time, Collection<MovementEvent> events) {
				for ( MovementEvent event : events )
					updater.handleEvent(time, event);
			}
		});
		movementBus.addListener( new Listener<Movement>(){
			@Override
			public void handle(long time, Collection<Movement> events){
				updater.setState(events);
			}
		});
		movementReader.setStateBus(movementBus);
		movementReader.setBus(movementEventBus);
		
		long max_time = (maxTime != null)? maxTime : movement.maxTime();
		Runner runner = new Runner(interval, movement.minTime(), max_time);
		runner.addGenerator(movementReader);
		runner.add(new Incrementable(){
			long cur_time;
			@Override
			public void incr(long time) throws IOException {
				for ( Movement m : updater.states() ){
					writer.write(m.oneString(cur_time, timeMul));
				}
				cur_time += time;
			}

			@Override
			public void seek(long time) throws IOException {
				cur_time = time;
			}
		});
		runner.run();
		writer.close();
	}
	
	private static double speed(Point o, Point n, double dt){
		double dx = n.x-o.x;
		double dy = n.y-o.y;
		double d = Math.sqrt(dx*dx + dy*dy);
		return d/dt;
	}
}
