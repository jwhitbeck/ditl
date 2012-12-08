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

import net.sf.json.JSONObject;
import ditl.Store;
import ditl.Trace;
import ditl.Writer;

@Trace.Type("reachability")
public class ReachabilityTrace extends ArcTrace {

    final public static String
            tauKey = "tau",
            etaKey = "eta",
            delayKey = "delay";

    public long tau() {
        return config.getLong(tauKey);
    }

    public long eta() {
        return config.getLong(etaKey);
    }

    public long delay() {
        return config.getLong(delayKey);
    }

    public static String defaultName(String prefix, long tau, long delay) {
        return prefix + "_t" + tau + "_d" + delay;
    }

    public ReachabilityTrace(Store store, String name, JSONObject config) throws IOException {
        super(store, name, config);
    }

    @Override
    public void copyOverTraceInfo(Writer<ArcEvent> writer) {
        writer.setProperty(tauKey, tau());
        writer.setProperty(etaKey, eta());
        writer.setProperty(delayKey, delay());
    }

}
