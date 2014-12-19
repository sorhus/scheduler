package com.github.sorhus.scheduler.pipe;

import com.github.sorhus.scheduler.pipe.control.PipeControl;
import com.github.sorhus.scheduler.pipe.control.SimplePipeControl;

import java.util.Properties;

/**
 * @author: anton.sorhus@gmail.com
 */
public class SimplePipeControlFactory implements PipeControlFactory {
    @Override
    public PipeControl getPipeControl(Properties properties) {
        return new SimplePipeControl(Integer.parseInt(properties.getProperty("pipe.numberOfJobs")));
    }
}
