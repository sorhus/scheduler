package com.github.sorhus.scheduler.pipe.control;

import java.util.Properties;

/**
 * @author: anton.sorhus@gmail.com
 */
public interface PipeControlFactory {
    PipeControl getPipeControl(Properties properties);
}
