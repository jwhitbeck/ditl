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
import java.util.Collection;
import java.util.Set;

import net.sf.json.JSONObject;
import ditl.Filter;
import ditl.Listener;
import ditl.StateUpdater;
import ditl.StateUpdaterFactory;
import ditl.StatefulTrace;
import ditl.Store;
import ditl.Trace;
import ditl.Writer;

@Trace.Type("edges")
public class EdgeTrace extends StatefulTrace<EdgeEvent, Edge>
        implements StatefulTrace.Filterable<EdgeEvent, Edge> {

    public final static class Updater implements StateUpdater<EdgeEvent, Edge> {
        private final Set<Edge> edges = new AdjacencySet.Edges();

        @Override
        public void setState(Collection<Edge> contactsState) {
            edges.clear();
            for (final Edge e : contactsState)
                edges.add(e);
        }

        @Override
        public Set<Edge> states() {
            return edges;
        }

        @Override
        public void handleEvent(long time, EdgeEvent event) {
            if (event.isUp())
                edges.add(event.edge());
            else
                edges.remove(event.edge());
        }
    }

    public interface Handler {
        public Listener<Edge> edgeListener();

        public Listener<EdgeEvent> edgeEventListener();
    }

    public EdgeTrace(Store store, String name, JSONObject config) throws IOException {
        super(store, name, config, new EdgeEvent.Factory(), new Edge.Factory(),
                new StateUpdaterFactory<EdgeEvent, Edge>() {
                    @Override
                    public StateUpdater<EdgeEvent, Edge> getNew() {
                        return new EdgeTrace.Updater();
                    }
                });
    }

    @Override
    public Filter<Edge> stateFilter(Set<Integer> group) {
        return new Edge.InternalGroupFilter(group);
    }

    @Override
    public Filter<EdgeEvent> eventFilter(Set<Integer> group) {
        return new EdgeEvent.InternalGroupFilter(group);
    }

    @Override
    public void copyOverTraceInfo(Writer<EdgeEvent> writer) {
    }
}
