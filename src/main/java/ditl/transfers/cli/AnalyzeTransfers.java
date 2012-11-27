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
package ditl.transfers.cli;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.ParseException;

import ditl.Reader;
import ditl.Report;
import ditl.ReportFactory;
import ditl.Runner;
import ditl.StatefulReader;
import ditl.Store.NoSuchTraceException;
import ditl.cli.Command;
import ditl.cli.ExportApp;
import ditl.transfers.BroadcastDeliveryReport;
import ditl.transfers.Buffer;
import ditl.transfers.BufferEvent;
import ditl.transfers.BufferTrace;
import ditl.transfers.Message;
import ditl.transfers.MessageEvent;
import ditl.transfers.MessageTrace;
import ditl.transfers.MessageTransferReport;
import ditl.transfers.Transfer;
import ditl.transfers.TransferEvent;
import ditl.transfers.TransferTrace;

@Command(pkg = "transfers", cmd = "analyze", alias = "a")
public class AnalyzeTransfers extends ExportApp {

    final static String
            broadcastDeliveryOption = "broadcast-delivery",
            messageTransferOption = "message-transfers",
            messagesOption = "messages",
            buffersOption = "buffers",
            transfersOption = "transfers";

    private String messagesName;
    private String buffersName;
    private String transfersName;
    private Long maxTime;
    private Long minTime;

    private ReportFactory<?> factory;

    @Override
    protected void initOptions() {
        super.initOptions();
        options.addOption(null, messagesOption, true, "name of messages trace");
        options.addOption(null, buffersOption, true, "name of buffer trace");
        options.addOption(null, transfersOption, true, "name of transfers trace");
        options.addOption(null, minTimeOption, true, "time to begin analysis");
        options.addOption(null, maxTimeOption, true, "time to end analysis");
        final OptionGroup reportGroup = new OptionGroup();
        reportGroup.addOption(new Option(null, broadcastDeliveryOption, false, "broadcast delivery report"));
        reportGroup.addOption(new Option(null, messageTransferOption, false, "message transfers report"));
        reportGroup.setRequired(true);
        options.addOptionGroup(reportGroup);
    }

    @Override
    protected void parseArgs(CommandLine cli, String[] args)
            throws ParseException, ArrayIndexOutOfBoundsException,
            HelpException {
        super.parseArgs(cli, args);
        messagesName = cli.getOptionValue(messagesOption, getDefaultName(MessageTrace.class));
        transfersName = cli.getOptionValue(transfersOption, getDefaultName(TransferTrace.class));
        buffersName = cli.getOptionValue(buffersOption, getDefaultName(BufferTrace.class));
        if (cli.hasOption(minTimeOption))
            minTime = Long.parseLong(cli.getOptionValue(minTimeOption));
        if (cli.hasOption(maxTimeOption))
            maxTime = Long.parseLong(cli.getOptionValue(maxTimeOption));

        if (cli.hasOption(broadcastDeliveryOption))
            factory = new BroadcastDeliveryReport.Factory();
        else if (cli.hasOption(messageTransferOption))
            factory = new MessageTransferReport.Factory();
    }

    @Override
    protected void run() throws IOException, NoSuchTraceException {
        final Report report = factory.getNew(_out);

        Long min_time = null, max_time = null, incr_time = null;
        final List<Reader<?>> readers = new LinkedList<Reader<?>>();

        if (report instanceof MessageTrace.Handler) {
            final MessageTrace messages = (MessageTrace) _store.getTrace(messagesName);
            final StatefulReader<MessageEvent, Message> msgReader = messages.getReader();

            final MessageTrace.Handler mh = (MessageTrace.Handler) report;
            msgReader.stateBus().addListener(mh.messageListener());
            msgReader.bus().addListener(mh.messageEventListener());

            readers.add(msgReader);

            min_time = messages.minTime();
            max_time = messages.maxTime();
            incr_time = messages.maxUpdateInterval();
        }

        if (report instanceof BufferTrace.Handler) {
            final BufferTrace buffers = (BufferTrace) _store.getTrace(buffersName);
            final StatefulReader<BufferEvent, Buffer> bufferReader = buffers.getReader();

            final BufferTrace.Handler bh = (BufferTrace.Handler) report;
            bufferReader.stateBus().addListener(bh.bufferListener());
            bufferReader.bus().addListener(bh.bufferEventListener());

            readers.add(bufferReader);

            if (min_time == null)
                min_time = buffers.minTime();
            if (max_time == null)
                max_time = buffers.maxTime();
            incr_time = buffers.maxUpdateInterval();
        }

        if (report instanceof TransferTrace.Handler) {
            final TransferTrace transfers = (TransferTrace) _store.getTrace(transfersName);
            final StatefulReader<TransferEvent, Transfer> transferReader = transfers.getReader();

            final TransferTrace.Handler th = (TransferTrace.Handler) report;
            transferReader.stateBus().addListener(th.transferListener());
            transferReader.bus().addListener(th.transferEventListener());

            readers.add(transferReader);

            if (min_time == null)
                min_time = transfers.minTime();
            if (max_time == null)
                max_time = transfers.maxTime();
            incr_time = transfers.maxUpdateInterval();
        }

        if (minTime != null)
            min_time = minTime;
        if (maxTime != null)
            max_time = maxTime;

        final Runner runner = new Runner(incr_time, min_time, max_time);
        for (final Reader<?> reader : readers)
            runner.addGenerator(reader);
        runner.run();

        report.finish();
    }
}
