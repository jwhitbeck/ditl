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
import java.util.Set;

import net.sf.json.JSONObject;
import ditl.Filter;
import ditl.Store;
import ditl.Trace;
import ditl.Writer;

@Trace.Type("beacons")
public class BeaconTrace extends Trace<Arc> implements Trace.Filterable<Arc> {

    public final static String beaconningPeriodKey = "beaconning period";

    public BeaconTrace(Store store, String name, JSONObject config) throws IOException {
        super(store, name, config, new Arc.Factory());
    }

    @Override
    public Filter<Arc> eventFilter(Set<Integer> group) {
        return new Arc.InternalGroupFilter(group);
    }

    public long beaconningPeriod() {
        return config.getLong(beaconningPeriodKey);
    }

    @Override
    public void copyOverTraceInfo(Writer<Arc> writer) {
        writer.setProperty(beaconningPeriodKey, config.get(beaconningPeriodKey));
    }
}
