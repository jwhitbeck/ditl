/*******************************************************************************
 * This file is part of DITL.                                                  *
 *                                                                             *
 * Copyright (C) 2011 John Whitbeck <john@whitbeck.fr>                         *
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

import java.io.*;
import java.util.*;

import org.apache.commons.cli.*;

import ditl.*;
import ditl.Reader;
import ditl.cli.App;
import ditl.transfers.*;

public class AnalyzeTransfers extends App {
	
	final static String broadcastDeliveryOption = "broadcast-delivery";
	final static String messageTransferOption = "message-transfers";
	
	final static String messagesOption = "messages";
	final static String buffersOption = "buffers";
	final static String transfersOption = "transfers";
	
	private File storeFile;
	private String outputFile;
	private String messagesName;
	private String buffersName;
	private String transfersName;
	private Long maxTime;
	private Long minTime;
	
	private ReportFactory<?> factory;
	
	public AnalyzeTransfers(String[] args) {
		super(args);
	}

	@Override
	protected void initOptions() {
		options.addOption(null, messagesOption, true, "name of messages trace");
		options.addOption(null, buffersOption, true, "name of buffer trace");
		options.addOption(null, transfersOption, true, "name of transfers trace");
		options.addOption(null, outputOption, true, "name of file to write output to");
		options.addOption(null, minTimeOption, true, "time to begin analysis");
		options.addOption(null, maxTimeOption, true, "time to end analysis");
		OptionGroup reportGroup = new OptionGroup();
		reportGroup.addOption(new Option(null, broadcastDeliveryOption, false, "broadcast delivery report") );
		reportGroup.addOption(new Option(null, messageTransferOption, false, "message transfers report") );
		reportGroup.setRequired(true);
		options.addOptionGroup(reportGroup);
	}

	@Override
	protected void parseArgs(CommandLine cli, String[] args)
			throws ParseException, ArrayIndexOutOfBoundsException,
			HelpException {
		
		messagesName = cli.getOptionValue(messagesOption, MessageStore.defaultMessagesName);
		transfersName = cli.getOptionValue(transfersOption, MessageStore.defaultTransfersName);
		buffersName = cli.getOptionValue(buffersOption, MessageStore.defaultBuffersName);
		storeFile = new File(args[0]);
		outputFile = cli.getOptionValue(outputOption);
		if ( cli.hasOption(minTimeOption) )
			minTime = Long.parseLong(cli.getOptionValue(minTimeOption));
		if ( cli.hasOption(maxTimeOption) )
			maxTime = Long.parseLong(cli.getOptionValue(maxTimeOption));
		
		if ( cli.hasOption(broadcastDeliveryOption) ){
			factory = BroadcastDeliveryReport.factory();
		} else if ( cli.hasOption(messageTransferOption) ){
			factory = MessageTransferReport.factory();
		}
	}
	
	
	@Override
	protected void run() throws IOException, MissingTraceException {
		Store store = Store.open(storeFile);
		OutputStream out = System.out;
		if ( outputFile != null )
			out = new FileOutputStream( outputFile );
		
		MessageStore mStore = new MessageStore(store);
		Report report = factory.getNew(out);
		
		Long min_time=null, max_time=null, incr_time=null;
		List<Reader<?>> readers = new LinkedList<Reader<?>>();
		
		if ( report instanceof MessageHandler ){
			Trace messages = getTrace(store,messagesName);
			StatefulReader<MessageEvent,Message> msgReader = mStore.getMessageReader(messages);
			
			Bus<MessageEvent> msgEventBus = new Bus<MessageEvent>();
			Bus<Message> msgBus = new Bus<Message>();
			msgReader.setBus(msgEventBus);
			msgReader.setStateBus(msgBus);
			
			MessageHandler mh = (MessageHandler)report;
			msgBus.addListener(mh.messageListener());
			msgEventBus.addListener(mh.messageEventListener());
			
			readers.add(msgReader);
			
			min_time = messages.minTime();
			max_time = messages.maxTime();
			incr_time = messages.maxUpdateInterval();
		}
		
		if ( report instanceof BufferHandler ){
			Trace buffers = getTrace(store,buffersName);			
			StatefulReader<BufferEvent,Buffer> bufferReader = mStore.getBufferReader(buffers);
			
			Bus<BufferEvent> bufferEventBus = new Bus<BufferEvent>();
			Bus<Buffer> bufferBus = new Bus<Buffer>();
			bufferReader.setBus(bufferEventBus);
			bufferReader.setStateBus(bufferBus);

			BufferHandler bh = (BufferHandler)report;
			bufferBus.addListener(bh.bufferListener());
			bufferEventBus.addListener(bh.bufferEventListener());
			
			readers.add(bufferReader);
			
			if ( min_time == null ) min_time = buffers.minTime();
			if ( max_time == null ) max_time = buffers.maxTime();
			incr_time = buffers.maxUpdateInterval();
		}
		
		if ( report instanceof TransferHandler ){
			Trace transfers = getTrace(store,transfersName);			
			StatefulReader<TransferEvent,Transfer> transferReader = mStore.getTransferReader(transfers);
			
			Bus<TransferEvent> transferEventBus = new Bus<TransferEvent>();
			Bus<Transfer> transferBus = new Bus<Transfer>();
			transferReader.setBus(transferEventBus);
			transferReader.setStateBus(transferBus);

			TransferHandler th = (TransferHandler)report;
			transferBus.addListener(th.transferListener());
			transferEventBus.addListener(th.transferEventListener());
			
			readers.add(transferReader);
			
			if ( min_time == null ) min_time = transfers.minTime();
			if ( max_time == null ) max_time = transfers.maxTime();
			incr_time = transfers.maxUpdateInterval();
		}
		
		if ( minTime != null )
			min_time = minTime;
		if ( maxTime != null )
			max_time = maxTime;

		Runner runner = new Runner(incr_time, min_time, max_time);
		for ( Reader<?> reader : readers )
			runner.addGenerator(reader);
		runner.run();
		
		report.finish();
		
		store.close();
	}

	@Override
	protected void setUsageString() {
		usageString = "Analyze [OPTIONS] STORE";
	}
	
}
