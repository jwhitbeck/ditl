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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONObject;
import ditl.Filter;
import ditl.IdMap;
import ditl.Listener;
import ditl.StateUpdater;
import ditl.StateUpdaterFactory;
import ditl.StatefulReader;
import ditl.StatefulTrace;
import ditl.Store;
import ditl.Trace;
import ditl.Writer;

@Trace.Type("groups")
public class GroupTrace extends StatefulTrace<GroupEvent, Group>
        implements StatefulTrace.Filterable<GroupEvent, Group> {

    final public static String labelsKey = "labels";

    private IdMap labels = null;

    public IdMap getGroupIdMap() {
        return labels;
    }

    public Set<Group> staticGroups() throws IOException {
        return staticGroups(minTime());
    }

    public Set<Group> staticGroups(long time) throws IOException {
        final StatefulReader<GroupEvent, Group> reader = getReader();
        reader.seek(time);
        final Set<Group> groups = reader.referenceState();
        reader.close();
        return groups;
    }

    public final static class Updater implements StateUpdater<GroupEvent, Group> {

        private final Map<Integer, Group> group_map = new HashMap<Integer, Group>();
        private final Set<Group> groups = new HashSet<Group>();

        @Override
        public void setState(Collection<Group> groupState) {
            groups.clear();
            group_map.clear();
            for (final Group g : groupState) {
                groups.add(g);
                group_map.put(g._gid, g);
            }
        }

        @Override
        public Set<Group> states() {
            return groups;
        }

        @Override
        public void handleEvent(long time, GroupEvent event) {
            Group g;
            final Integer gid = event._gid;
            switch (event._type) {
                case NEW:
                    g = new Group(gid);
                    groups.add(g);
                    group_map.put(gid, g);
                    break;
                case JOIN:
                    g = group_map.get(gid);
                    g.handleEvent(event);
                    break;
                case LEAVE:
                    g = group_map.get(gid);
                    g.handleEvent(event);
                    break;
                case DELETE:
                    g = group_map.get(gid);
                    groups.remove(g);
                    group_map.remove(gid);
            }
        }
    }

    public interface Handler {
        public Listener<Group> groupListener();

        public Listener<GroupEvent> groupEventListener();
    }

    public GroupTrace(Store store, String name, JSONObject config) throws IOException {
        super(store, name, config, new GroupEvent.Factory(), new Group.Factory(),
                new StateUpdaterFactory<GroupEvent, Group>() {
                    @Override
                    public StateUpdater<GroupEvent, Group> getNew() {
                        return new GroupTrace.Updater();
                    }
                });
        if (config.has(labelsKey))
            labels = new IdMap(config.getJSONObject(labelsKey));
    }

    @Override
    public Filter<GroupEvent> eventFilter(Set<Integer> group) {
        return new GroupEvent.GroupFilter(group);
    }

    @Override
    public Filter<Group> stateFilter(Set<Integer> group) {
        return new Group.GroupFilter(group);
    }

    @Override
    public void copyOverTraceInfo(Writer<GroupEvent> writer) {
        if (labels != null)
            writer.setProperty(labelsKey, labels);
    }
}
