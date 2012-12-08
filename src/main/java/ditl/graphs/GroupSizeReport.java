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

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;

import ditl.Listener;
import ditl.ReportFactory;
import ditl.StateTimeReport;
import ditl.StatefulListener;

public final class GroupSizeReport extends StateTimeReport implements GroupTrace.Handler {

    private final GroupTrace.Updater updater = new GroupTrace.Updater();

    public GroupSizeReport(OutputStream out) throws IOException {
        super(out);
        appendComment("time | duration | group size distribution");
    }

    public static final class Factory implements ReportFactory<GroupSizeReport> {
        @Override
        public GroupSizeReport getNew(OutputStream out) throws IOException {
            return new GroupSizeReport(out);
        }
    }

    private void update(long time) throws IOException {
        final StringBuffer buffer = new StringBuffer();
        for (final Group g : updater.states())
            buffer.append(g.size() + " ");
        append(time, buffer.toString());
    }

    @Override
    public Listener<GroupEvent> groupEventListener() {
        return new Listener<GroupEvent>() {
            @Override
            public void handle(long time, Collection<GroupEvent> events)
                    throws IOException {
                for (final GroupEvent gev : events)
                    updater.handleEvent(time, gev);
                update(time);
            }
        };
    }

    @Override
    public Listener<Group> groupListener() {
        return new StatefulListener<Group>() {
            @Override
            public void reset() {
                updater.setState(Collections.<Group> emptySet());
            }

            @Override
            public void handle(long time, Collection<Group> events)
                    throws IOException {
                updater.setState(events);
                update(time);
            }
        };
    }

}
