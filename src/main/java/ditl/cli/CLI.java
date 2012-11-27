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
package ditl.cli;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.TypeAnnotationsScanner;

public class CLI {

    private final Map<String, CommandMap> cmd_maps = new HashMap<String, CommandMap>();

    private class CommandMap {
        Map<String, String> alias_map = new HashMap<String, String>();
        Map<String, String> rev_alias_map = new HashMap<String, String>();
        Map<String, Class<?>> cmd_map = new HashMap<String, Class<?>>();

        Class<?> getClass(String cmd) {
            if (alias_map.containsKey(cmd))
                return cmd_map.get(alias_map.get(cmd));
            return cmd_map.get(cmd);
        }

        void add(String cmd_name, String cmd_alias, Class<?> klass) {
            if (!cmd_alias.isEmpty()) {
                alias_map.put(cmd_alias, cmd_name);
                rev_alias_map.put(cmd_name, cmd_alias);
            }
            cmd_map.put(cmd_name, klass);
        }
    }

    private CLI() throws IOException {
        findPackages();
    }

    private void parseArgs(String[] args) {
        if (args.length > 0) {
            final String pkg = args[0];
            final String[] pkg_args = Arrays.copyOfRange(args, 1, args.length);
            String app;
            Class<?> klass;

            // check if pkg isn't really an app in the default package
            klass = cmd_maps.get(Command.default_package).getClass(pkg);
            if (klass != null) {
                startApp(pkg, klass, pkg_args);
                return;
            }

            // otherwise proceed normally
            if (pkg_args.length > 0) {
                app = pkg_args[0];
                if (cmd_maps.containsKey(pkg)) {
                    klass = cmd_maps.get(pkg).getClass(app);
                    if (klass != null) {
                        final String[] app_args = Arrays.copyOfRange(pkg_args, 1, pkg_args.length);
                        startApp(app, klass, app_args);
                        return;
                    }
                }
            }

        }
        printHelp();
        System.exit(1);

    }

    private void printHelp() {
        final StringBuffer buffer = new StringBuffer();
        buffer.append("Usage: [PACKAGE] CMD [CMD_OPTIONS]\n\n");
        buffer.append("Where PACKAGE is one of: \n\n");

        buffer.append("default package:\n");
        if (cmd_maps.containsKey(Command.default_package))
            appendPackageHelp(buffer, Command.default_package);
        for (final String pkg : cmd_maps.keySet())
            if (!pkg.equals(Command.default_package)) {
                buffer.append("Package: " + pkg + "\n");
                appendPackageHelp(buffer, pkg);
            }
        buffer.append("\n");
        buffer.append("To get help on a particular command, run CMD --help");
        System.out.println(buffer.toString());
    }

    private void appendPackageHelp(StringBuffer buffer, String pkg_name) {
        final CommandMap cmd_map = cmd_maps.get(pkg_name);
        for (final String cmd_name : cmd_map.cmd_map.keySet()) {
            buffer.append("    ");
            buffer.append(cmd_name);
            final String alias = cmd_map.rev_alias_map.get(cmd_name);
            if (alias != null)
                buffer.append(" (" + alias + ")");
            buffer.append("\n");
        }
        buffer.append("\n");
    }

    private void startApp(String name, Class<?> klass, String[] args) {
        try {
            final Class<? extends App> appClass = klass.asSubclass(App.class);
            final Constructor<? extends App> ctor = appClass.getConstructor();
            final App app = ctor.newInstance();
            if (app.ready(name, args))
                app.exec();
        } catch (final Throwable e) {
            e.printStackTrace();
            System.err.println(e);
            System.exit(1);
        }
    }

    private void findPackages() throws IOException {
        final Set<Class<?>> appKlasses = new Reflections("ditl", new TypeAnnotationsScanner()).
                getTypesAnnotatedWith(Command.class);
        for (final Class<?> klass : appKlasses) {
            final String pkg_name = klass.getAnnotation(Command.class).pkg();
            final String cmd_name = klass.getAnnotation(Command.class).cmd();
            final String cmd_alias = klass.getAnnotation(Command.class).alias();
            if (cmd_name != null) {
                final CommandMap cmd_map = get_cmd_map(pkg_name);
                cmd_map.add(cmd_name, cmd_alias, klass);
            }
        }
    }

    private CommandMap get_cmd_map(String pkg_name) {
        if (!cmd_maps.containsKey(pkg_name))
            cmd_maps.put(pkg_name, new CommandMap());
        return cmd_maps.get(pkg_name);
    }

    public static void main(String args[]) throws IOException {
        new CLI().parseArgs(args);
    }
}
