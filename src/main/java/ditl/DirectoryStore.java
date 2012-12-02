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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

public class DirectoryStore extends WritableStore {

    File root;

    public DirectoryStore(File dir) throws IOException {
        root = dir;
        separator = File.separator;
        initDir();
        refresh();
    }

    void initDir() throws IOException {
        if (!root.exists()) {
            if (!root.mkdirs())
                throw new IOException("Could not create directory '" + root.getPath() + "'");
        } else if (!root.isDirectory())
            throw new IOException("Could not create dir " + root.getPath() + ": a file with that name already exists.");
    }

    void refresh() throws IOException {
        for (final File file : listFiles(infoFile))
            try {
                loadTrace(file.getParentFile().getName());
            } catch (final Exception e) {
                System.err.println(e);
            }
    }

    Set<File> listFiles(String filter) {
        final Set<File> paths = new HashSet<File>();
        recAddFromDir(root, paths, filter);
        return paths;
    }

    private static void recAddFromDir(File dir, Set<File> paths, String filter) {
        for (final File file : dir.listFiles()) {
            if (filter == null || file.getName().equals(filter))
                paths.add(file);
            if (file.isDirectory())
                recAddFromDir(file, paths, filter);
        }
    }

    @Override
    public InputStream getInputStream(String name) throws IOException {
        return new FileInputStream(new File(root, name));
    }

    @Override
    public void deleteFile(String name) throws IOException {
        final File file = new File(root, name);
        if (!file.delete())
            throw new IOException("Could not delete '" + file.getPath() + "'");
    }

    @Override
    public void deleteTrace(String name) throws IOException {
        final File traceDir = new File(root, name);
        rec_delete(traceDir);
        traces.remove(name);
    }

    void rec_delete(File file) throws IOException {
        if (file.isDirectory())
            for (final File f : file.listFiles())
                rec_delete(f);
        if (!file.delete())
            throw new IOException("Could not delete '" + file.getPath() + "'");
    }

    @Override
    public OutputStream getOutputStream(String name) throws IOException {
        final File parent = new File(root, name).getParentFile();
        parent.mkdirs();
        return new FileOutputStream(new File(root, name));
    }

    @Override
    public boolean hasFile(String name) {
        return new File(root, name).exists();
    }

    @Override
    public void moveTrace(String origName, String destName, boolean force) throws IOException {
        final File dest = new File(root, destName);
        if (dest.exists())
            if (force)
                deleteTrace(destName);
            else
                throw new IOException("A trace with name '" + destName + "' already exists!");
        new File(root, origName).renameTo(dest);
    }

}
