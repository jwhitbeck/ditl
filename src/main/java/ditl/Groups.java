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
package ditl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONSerializer;

public class Groups {

    /*
     * the json string [ [1,3], 5, [8-11] ] corresponds to group
     * {1,2,3,5,8,10,11}
     */
    public static Set<Integer> parse(JSONArray json) {
        return parse(json, null);
    }

    public static Set<Integer> parse(JSONArray json, IdMap idMap) {
        final Set<Integer> group = new HashSet<Integer>();
        for (Object obj : json) {
            if (obj instanceof JSONArray)
                for (int i = ((JSONArray) obj).getInt(0); i <= ((JSONArray) obj).getInt(1); ++i)
                    group.add(i);
            else if (idMap != null && obj instanceof String)
                group.add(idMap.getInternalId(obj.toString()));
            else
                group.add((Integer) obj);

        }
        return group;
    }

    public static JSONArray toJSON(Set<Integer> group) {
        JSONArray json = new JSONArray();
        final ArrayList<Integer> list = new ArrayList<Integer>(group);
        Collections.sort(list);
        final Iterator<Integer> i = list.iterator();
        Integer prev = null;
        Integer b = null;
        while (i.hasNext()) {
            final Integer n = i.next();
            if (b == null)
                b = n;
            if (prev != null)
                if (n - prev > 1) { // left range
                    if (prev.equals(b))
                        json.add(prev);
                    else
                        json.add(JSONSerializer.toJSON(new Integer[] { b, prev }));
                    b = n;
                }
            if (!i.hasNext())
                if (n.equals(b))
                    json.add(n);
                else
                    json.add(JSONSerializer.toJSON(new Integer[] { b, n }));
            prev = n;
        }
        return json;
    }
}
