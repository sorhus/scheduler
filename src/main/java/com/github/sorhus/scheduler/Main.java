package com.github.sorhus.scheduler;

import com.github.sorhus.scheduler.pipe.Pipe;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * @author: anton.sorhus@gmail.com
 */
public class Main {

    public static void main(String[] args) throws Exception {

        List<String> specs = Lists.newArrayList(
            "{\"name\":\"job_a\",\"description\":\"\",\"parameters\":[\"foo\"],\"dependencies\":[]}",
            "{\"name\":\"job_b\",\"description\":\"\",\"parameters\":[\"bar\", \"foo\"],\"dependencies\":[\"job_a\"]}",
            "{\"name\":\"job_c\",\"description\":\"\",\"parameters\":[\"f o o\"],\"dependencies\":[\"job_a\", \"job_b\"]}"
        );

        new Pipe(specs, 2);

    }
}
