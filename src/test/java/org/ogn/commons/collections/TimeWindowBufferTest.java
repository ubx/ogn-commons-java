/**
 * Copyright (c) 2015 OGN, All Rights Reserved.
 */

package org.ogn.commons.collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.Test;
import org.ogn.commons.collections.TimeWindowBuffer;
import org.ogn.commons.collections.TimeWindowBufferListener;

public class TimeWindowBufferTest {

    static final long TIME_WINDOW = 500;
    static final int MAX_MSG_LENGTH = 150;

    long t;

    @Test
    public void test() throws Exception {
        final TimeWindowBufferListener listener = new TimeWindowBufferListener() {

            @Override
            public void tick(String msg) {
                long t2 = System.currentTimeMillis();
                System.out.println(t2 + " " + msg.length() + " " + msg);

                assertTrue(msg.length() <= MAX_MSG_LENGTH + "s_t_r_i_n_g99".length());
                if (t > 0) {
                    System.out.println(t2 - t);
                    assertTrue(t2 - t <= TIME_WINDOW + 10); // 10ms difference can be due to threading
                }
                t = t2;
            }
        };

        TimeWindowBuffer<String> buffer = new TimeWindowBuffer<>(MAX_MSG_LENGTH, TIME_WINDOW, listener, "&");

        Random r = new Random(System.currentTimeMillis());
        buffer.add("str0");

        Thread.sleep(150);

        for (int i = 1; i < 101; i++) {
            buffer.add("s_t_r_i_n_g" + i);
            Thread.sleep(5 + r.nextInt(40));
        }

        Thread.sleep(TIME_WINDOW * 2);

        assertEquals(0, buffer.size());

        t = 0;
        buffer.add("s_t_r_i_n_g102");

        Thread.sleep(TIME_WINDOW - 100);
        buffer.add("s_t_r_i_n_g103");
        Thread.sleep(TIME_WINDOW * 2);
        assertEquals(0, buffer.size());
    }

}
