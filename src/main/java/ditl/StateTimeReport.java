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

import java.io.IOException;
import java.io.OutputStream;

public class StateTimeReport extends Report {

    protected long prev_time;
    protected Object prev_state;

    public StateTimeReport(OutputStream out) throws IOException {
        super(out);
    }

    public void append(long time, Object s) throws IOException {
        if (prev_state != null)
            append(prev_time + " " + (time - prev_time) + " " + prev_state);
        prev_time = time;
        prev_state = s;
    }

    public void finish(long time) throws IOException {
        append(prev_time + " " + (time - prev_time) + " " + prev_state);
        finish();
    }

}
