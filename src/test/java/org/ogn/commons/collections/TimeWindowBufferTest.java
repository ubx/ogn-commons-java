/**
 * Copyright (c) 2015 OGN, All Rights Reserved.
 */

package org.ogn.commons.collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeWindowBufferTest {

	static final long TIME_WINDOW = 150;
	static final int MAX_MSG_LENGTH = 20;

	private static Logger LOG = LoggerFactory.getLogger(TimeWindowBufferTest.class);

	long t;
	int total = 0;

	@Test
	public void test() throws Exception {
		final TimeWindowBufferListener listener = new TimeWindowBufferListener() {

			@Override
			public void tick(String msg, int elements) {
				long t2 = System.currentTimeMillis();

				LOG.debug("{} {} {}", t2, String.format("%03d", msg.length()), msg);

				if (t > 0) {
					long diff = t2 - t;
					LOG.debug("time diff: {} elements: {}", diff, elements);
				}

				total += elements;
				t = t2;
			}
		};

		TimeWindowBuffer<String> buffer = new TimeWindowBuffer<>(MAX_MSG_LENGTH, TIME_WINDOW, listener, "&");

		Random r = new Random(System.currentTimeMillis());

		final int LOOP_1 = 5;
		for (int i = 0; i < LOOP_1; i++) {
			buffer.add("a");
		}

		Thread.sleep(150);

		final int LOOP_2 = 205;
		for (int i = 0; i < LOOP_2; i++) {
			buffer.add("a");
			Thread.sleep(5 + r.nextInt(20));
		}

		Thread.sleep(TIME_WINDOW * 3);

		// make sure all elements have been processed
		assertEquals(LOOP_1 + LOOP_2, total);

		// no more elements expected in the buffer after processing
		assertEquals(0, buffer.size());

		buffer.stop();
		buffer.add("a");
		Thread.sleep(TIME_WINDOW * 2);

		// since the buffer is now stopped the messages
		// are not expected to be digested
		assertTrue(buffer.size() > 0);
	}
}