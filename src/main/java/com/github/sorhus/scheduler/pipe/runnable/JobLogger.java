package com.github.sorhus.scheduler.pipe.runnable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author: anton.sorhus@gmail.com
 */
public class JobLogger implements Runnable {

    private final Logger log;
    private final InputStream logStream;
    private boolean keepRunning;
    private final int sleep;

    public JobLogger(InputStream logStream, int sleep) {
        this.log = LoggerFactory.getLogger("JobLogger");
        this.logStream = logStream;
        this.keepRunning = true;
        this.sleep = sleep;
    }

    public void shutDown() {
        keepRunning = false;
    }

    // TODO: print line breaks
    @Override
    public void run() {
        while(keepRunning && null != logStream) {
            try {
                int len = logStream.available();
                while(len > 0) {
                    byte[] buffer = new byte[len];
                    logStream.read(buffer);
                    log.info(new String(buffer));
                    len = logStream.available();
                }
                Thread.sleep(sleep);
            } catch (IOException e) {
                log.warn("Unable to log, {}", log.getName(), e);
            } catch (InterruptedException e) {}
        }
    }
}
