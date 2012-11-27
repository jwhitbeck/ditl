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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import ditl.Store;
import ditl.Store.LoadTraceException;
import ditl.Store.NoSuchTraceException;
import ditl.Trace;
import ditl.Units;
import ditl.WritableStore.AlreadyExistsException;

public abstract class App {

    protected final static String
            offsetOption = "offset",
            origTimeUnitOption = "orig-time-unit",
            destTimeUnitOption = "dest-time-unit",
            maxTimeOption = "max-time",
            minTimeOption = "min-time",
            intervalOption = "interval",
            traceOption = "trace",
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

    protected abstract void run() throws IOException, NoSuchTraceException, AlreadyExistsException, LoadTraceException;

    protected void init() throws IOException {
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
        } catch (final ParseException e) {
            System.err.println(e);
            printHelp();
        } catch (final ArrayIndexOutOfBoundsException e) {
            printHelp();
        } catch (final NumberFormatException nfe) {
            System.err.println(nfe);
            printHelp();
        } catch (final HelpException he) {
            printHelp();
        }
        return false;
    }

    public void exec() throws IOException {
        init();
        try {
            run();
        } catch (final Store.NoSuchTraceException mte) {
            System.err.println(mte);
            System.exit(1);
        } catch (final AlreadyExistsException e) {
            System.err.println(e);
            System.err.println("Use --" + forceOption + " to overwrite existing traces");
        } catch (final LoadTraceException e) {
            System.err.println(e);
        } catch (final NumberFormatException nfe) {
            System.err.println(nfe);
            System.err.println("Use --" + stringIdsOption + " if nodes have ids that are not integers");
        }
        close();
    }

    protected void printHelp() {
        new HelpFormatter().printHelp(usageString, options);
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
