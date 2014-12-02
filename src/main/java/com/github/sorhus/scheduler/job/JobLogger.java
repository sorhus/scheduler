package com.github.sorhus.scheduler.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author: anton.sorhus@gmail.com
 */
public class JobLogger implements Runnable {

    private final Logger log;
    private final InputStream logStream;
    private final AtomicBoolean keepRunning;

    public JobLogger(InputStream logStream) {
        this.log = LoggerFactory.getLogger("JobLogger");
        this.logStream = logStream;
        this.keepRunning = new AtomicBoolean(true);
    }

    public void shutDown() {
        keepRunning.set(false);
    }

    @Override
    public void run() {
        while(keepRunning.get() && null != logStream) {
            try {
                int len = logStream.available();
                while(len > 0) {
                    byte[] buffer = new byte[len];
                    logStream.read(buffer);
                    log.info(new String(buffer));
                    len = logStream.available();
                }
            } catch (IOException e) {
                log.warn("Unable to log, {}", log.getName(), e);
            }
        }
    }
}
