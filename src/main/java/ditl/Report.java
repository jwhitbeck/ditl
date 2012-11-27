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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class Report {

    public final static char commentChar = '#';

    private final BufferedWriter writer;
    private final OutputStream _out;

    public Report(OutputStream out) throws IOException {
        _out = out;
        writer = new BufferedWriter(new OutputStreamWriter(_out));
    }

    public void finish() throws IOException {
        writer.close();
    }

    public void append(Object line) throws IOException {
        writer.write(line + "\n");
    }

    public void appendComment(Object comment) throws IOException {
        writer.write(commentChar + " " + comment + "\n");
    }
}
