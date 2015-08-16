package com.server.impl.processor;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.asl.utils.Constants;

public class ProcessorPerfMeasurer extends Thread {
	private static final Logger LOGGER = Logger
			.getLogger(ProcessorPerfMeasurer.class.getCanonicalName());

	private static final int MAX_RECORDS = 1024;
	private static final ProcessorPerfMeasurer measurer = new ProcessorPerfMeasurer();

	private volatile boolean stop = false;
	private BlockingQueue<PerfRecord> records = new ArrayBlockingQueue<>(
			MAX_RECORDS);

	private ProcessorPerfMeasurer() {
		start();
	}

	public static ProcessorPerfMeasurer getPerfRecorder() {
		return measurer;
	}

	public void terminate() {
		stop = true;
	}

	@Override
	public void run() {
		try (PrintStream ps = new PrintStream(Constants.USER_HOME
				+ Constants.PATH_SEPERATOR+Constants.TEST_RUN_DIR
				+ Constants.PATH_SEPERATOR + Constants.LOGS_DIR
				+ Constants.PATH_SEPERATOR + Constants.PERF_RUNS_DIR
				+ Constants.PATH_SEPERATOR + Constants.PERF_DB_MEASURES
				+ System.currentTimeMillis() + ".txt")) {
			ps.println("test Date::"+new Date());
			ps.println(Constants.QUERY_TYPE_LABEL+";"+Constants.CLIENT_ID_LABEL+";"+Constants.STATUS+";"+Constants.TIME_TAKEN+";"+Constants.QUEUE_ID_LABEL);
			while (!stop) {
				PerfRecord r = records.take();
				ps.println(r.getType().toString()+";"+r.getClientId()+";"+r.getStatus()+";"+r.getDuration()+";"+r.getQueues());
			}
		} catch (InterruptedException e) {
			LOGGER.log(Level.SEVERE, "Couldn't retrieve PerfRecord from queue "
					+ e.getMessage(), e);
		} catch (FileNotFoundException e) {
			LOGGER.log(Level.SEVERE,
					"Couldn't write to file " + e.getMessage(), e);
		} finally {
			terminate();
		}
	}

	public void add(PerfRecord r) {
		if (stop)
			return;
		try {
			records.put(r);
		} catch (InterruptedException e) {
			LOGGER.log(Level.SEVERE, "Couldn't enqueue PerfRecord " + r + ": "
					+ e.getMessage(), e);
		}
	}
}
