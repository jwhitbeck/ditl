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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ditl.IdGenerator;
import ditl.Incrementable;
import ditl.Listener;
import ditl.Runner;
import ditl.StatefulReader;
import ditl.StatefulWriter;
import ditl.Trace;
import ditl.Units;

public class ONEMovement {

    public static void fromONE(MovementTrace movement,
            InputStream in, Long maxTime, final double timeMul, long ticsPerSecond,
            long offset, IdGenerator idGen) throws IOException {

        final StatefulWriter<MovementEvent, Movement> movementWriter = movement.getWriter();
        final TreeMap<Long, List<Movement>> buffer = new TreeMap<Long, List<Movement>>();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line;
        reader.readLine(); // burn first line
        while ((line = reader.readLine()) != null) {
            final String[] elems = line.split("[ \t]+");
            final long t = (long) (Double.parseDouble(elems[0]) * timeMul) + offset;
            final Integer id = idGen.getInternalId(elems[1]);
            final double x = Double.parseDouble(elems[2]);
            final double y = Double.parseDouble(elems[3]);
            List<Movement> movementsAtTime = buffer.get(t);
            if (movementsAtTime == null) {
                movementsAtTime = new LinkedList<Movement>();
                buffer.put(t, movementsAtTime);
            }
            movementsAtTime.add(new Movement(id, new Point(x, y)));
        }
        reader.close();

        final Map<Integer, Point> points = new HashMap<Integer, Point>();

        Collection<Movement> first = null;
        long last_time = Long.MIN_VALUE;

        while (!buffer.isEmpty()) {
            final Map.Entry<Long, List<Movement>> e = buffer.pollFirstEntry();
            final long time = e.getKey() + offset;
            final List<Movement> events = e.getValue();
            final double dt = time - last_time;
            if (first == null)
                first = events;
            else if (points.isEmpty()) {
                for (final Movement m : events)
                    points.put(m.id, m.from());
                for (final Movement m : first) {
                    final Point dest = points.get(m.id);
                    final double s = speed(m.from(), dest, dt);
                    m.setNewDestination(last_time, dest, s);
                }
                movementWriter.setInitState(last_time, first);
            } else
                for (final Movement m : events) {
                    final Point dest = m.from();
                    final double s = speed(points.get(m.id), dest, dt);
                    movementWriter.append(last_time, new MovementEvent(m.id, s, dest));
                    points.put(m.id, dest); // update last point
                }
            last_time = time;
        }
        last_time = (maxTime != null) ? maxTime : last_time;
        movementWriter.setProperty(Trace.maxTimeKey, last_time);
        movementWriter.setProperty(Trace.timeUnitKey, Units.toTimeUnit(ticsPerSecond));
        idGen.writeTraceInfo(movementWriter);
        movementWriter.close();
    }

    public static void toONE(MovementTrace movement,
            OutputStream out, final double timeMul, long interval, Long maxTime) throws IOException {
        final StatefulReader<MovementEvent, Movement> movementReader = movement.getReader();
        final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));

        // write initial ONE line
        writer.write(movement.minTime() * timeMul + " " + movement.maxTime() * timeMul + " ");
        writer.write(movement.minX() + " " + movement.maxX() + " ");
        writer.write(movement.minY() + " " + movement.maxY() + "\n");
        // print all positions every interval
        final MovementTrace.Updater updater = new MovementTrace.Updater();
        movementReader.bus().addListener(new Listener<MovementEvent>() {
            @Override
            public void handle(long time, Collection<MovementEvent> events) {
                for (final MovementEvent event : events)
                    updater.handleEvent(time, event);
            }
        });
        movementReader.stateBus().addListener(new Listener<Movement>() {
            @Override
            public void handle(long time, Collection<Movement> events) {
                updater.setState(events);
            }
        });

        final long max_time = (maxTime != null) ? maxTime : movement.maxTime();
        final Runner runner = new Runner(interval, movement.minTime(), max_time);
        runner.addGenerator(movementReader);
        runner.add(new Incrementable() {
            long cur_time;

            @Override
            public void incr(long dt) throws IOException {
                for (final Movement m : updater.states())
                    writer.write(m.oneString(cur_time, timeMul));
                cur_time += dt;
            }

            @Override
            public void seek(long time) throws IOException {
                cur_time = time;
            }
        });
        runner.run();
        writer.close();
        movementReader.close();
    }

    private static double speed(Point o, Point n, double dt) {
        final double dx = n.x - o.x;
        final double dy = n.y - o.y;
        final double d = Math.sqrt(dx * dx + dy * dy);
        return d / dt;
    }
}
