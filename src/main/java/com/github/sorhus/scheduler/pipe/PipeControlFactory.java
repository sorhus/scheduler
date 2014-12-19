package com.github.sorhus.scheduler.pipe;

import com.github.sorhus.scheduler.pipe.control.PipeControl;

import java.util.Properties;

/**
 * @author: anton.sorhus@gmail.com
 */
interface PipeControlFactory {
    PipeControl getPipeControl(Properties properties);
}
