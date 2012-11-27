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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.Deflater;

public class JarDirectoryStore extends DirectoryStore {

    private final File archive;

    public JarDirectoryStore(File file) throws IOException {
        super(new File(file.getPath() + ".tmp"));
        archive = file;
        if (archive.exists()) {
            extract();
            refresh();
        }
    }

    private void extract() throws IOException {
        final JarFile jarFile = new JarFile(archive);
        final Enumeration<?> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            final JarEntry e = (JarEntry) entries.nextElement();
            if (!e.isDirectory())
                copy(jarFile.getInputStream(e), getOutputStream(e.getName()));
        }
        jarFile.close();
    }

    private void compress() throws IOException {
        final JarOutputStream out = new JarOutputStream(new FileOutputStream(archive));
        out.setLevel(Deflater.BEST_SPEED);
        final byte[] buffer = new byte[18024];
        for (final File file : listFiles(null))
            if (!file.isDirectory()) {
                final String path = file.getAbsolutePath().replace(root.getAbsolutePath() + "/", "");
                final JarEntry entry = new JarEntry(path);
                out.putNextEntry(entry);
                if (new File(root, path).isFile()) {
                    final InputStream in = getInputStream(path);
                    int len;
                    while ((len = in.read(buffer)) > 0)
                        out.write(buffer, 0, len);
                    in.close();
                }
                out.closeEntry();
            }
        out.close();
    }

    @Override
    public void close() throws IOException {
        super.close();
        compress();
        rec_delete(root);
    }
}
