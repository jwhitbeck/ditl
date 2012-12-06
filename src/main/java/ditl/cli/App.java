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
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import ditl.Trace;
import ditl.Units;

public abstract class App {

    @Target({ ElementType.TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Cli {

        public final static String default_package = "DEFAULT_PACKAGE";

        String pkg() default default_package;

        String cmd();

        String alias() default "";
    }

    protected final static String
            offsetOption = "offset",
            origTimeUnitOption = "orig-time-unit",
            destTimeUnitOption = "dest-time-unit",
            maxTimeOption = "max-time",
            minTimeOption = "min-time",
            intervalOption = "interval",
            outputOption = "output",
            storeOutputOption = "out-store",
            forceOption = "force",
            typeOption = "type",
            stringIdsOption = "string-ids",
            minIdOption = "min-id";

    protected Options options = new Options();
    protected String usageString;
    protected boolean showHelp = false;
    protected String _name;

    protected void initOptions() {
    }

    protected abstract String getUsageString();

    protected abstract void parseArgs(CommandLine cli, String[] args)
            throws ParseException, ArrayIndexOutOfBoundsException, HelpException;

    @SuppressWarnings("serial")
    public static class HelpException extends Exception {
    }

    protected abstract void run() throws Exception;

    protected void init() throws Exception {
    }

    protected void close() throws IOException {
    };

    public boolean ready(String name, String[] args) {
        _name = name;
        options.addOption(new Option("h", "help", false, "Print help"));
        initOptions();
        usageString = _name + " " + getUsageString();
        try {
            final CommandLine cli = new PosixParser().parse(options, args);
            if (cli.hasOption("help"))
                throw new HelpException();
            parseArgs(cli, cli.getArgs());
            return true;
        } catch (final HelpException e) {
            printHelp();
        } catch (final Exception e) {
            System.err.println(e);
            printHelp();
        }
        return false;
    }

    public void exec() throws Exception {
        init();
        run();
        close();
    }

    protected String getHelpHeader() {
        return null;
    }

    protected String getHelpFooter() {
        return null;
    }

    protected void printHelp() {
        new HelpFormatter().printHelp(usageString, getHelpHeader(), options, getHelpFooter());
        System.exit(1);
    }

    protected Double getTimeMul(Long otps, Long dtps) {
        if (otps != null && dtps != null)
            return dtps.doubleValue() / otps.doubleValue();
        return null;
    }

    protected long getTicsPerSecond(String unitString) throws ParseException {
        final Long tps = Units.getTicsPerSecond(unitString);
        if (tps == null)
            throw new ParseException("Error parsing time unit '" + unitString + "'");
        return tps;
    }

    protected String getDefaultName(Class<? extends Trace<?>> klass) {
        return klass.getAnnotation(Trace.Type.class).value();
    }
}
