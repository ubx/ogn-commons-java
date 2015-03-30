/**
 * Copyright (c) 2015 OGN, All Rights Reserved.
 */

package org.ogn.commons.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This class implements a moving-time-window buffer which holds elements as long as one of the two conditions is met:
 * 1) predefined amount of time (ms) elapses or 2) the total string length of ALL elements concatenated together
 * (including the optional delimiters)exceeds the max. allowed number. In any case listeners are notified with a string
 * representation of the buffer (the concatenated elements) and the buffer is flushed.
 * 
 * @author wbuczak
 * @param <T>
 */
public class TimeWindowBuffer<T> extends ArrayList<T> {

    private static final long serialVersionUID = -8204258873338667741L;
    private int strLen;

    private final int maxSize;
    private final long timeWindow;
    private final String delimiter;

    private Timer timer;

    private transient long t;

    private TimeWindowBufferListener[] listeners;

    @Override
    public boolean add(T obj) {
        synchronized (this) {
            super.add(obj);
            if (this.size() >= maxSize)
                evaluate();
        }// sync

        return true;
    }

    private void evaluate() {
        synchronized (this) {
            long ct = System.currentTimeMillis();

            // skip if buffer is not full and last iteration was less then "timeWindow" ms ago
            if ((ct - t < timeWindow) && this.size() < maxSize) {
                return;
            }

            t = ct;
            String str = this.toStr();
            if (str.length() > 0)
                synchronized (listeners) {
                    for (TimeWindowBufferListener l : listeners) {
                        l.tick(str, this.size());
                    }
                    // clear the buffer
                    this.clear();
                    strLen = 0;
                }// sync
        }// sync
    }

    private String toStr() {
        StringBuilder strBld = new StringBuilder();
        synchronized (this) {
            int index = 0;
            for (T s : this) {
                strBld.append(s);
                if (++index < this.size() && delimiter != null)
                    strBld.append(delimiter);
            }
        }// sync

        return strBld.toString();
    }

    public TimeWindowBuffer(int maxSize, long timeWindowMs, final TimeWindowBufferListener listener) {
        this(maxSize, timeWindowMs, new TimeWindowBufferListener[] { listener }, null);
    }

    public TimeWindowBuffer(int maxSize, long timeWindowMs, final TimeWindowBufferListener listener, String delimiter) {
        this(maxSize, timeWindowMs, new TimeWindowBufferListener[] { listener }, delimiter);
    }

    public TimeWindowBuffer(int maxSize, long timeWindowMs, final TimeWindowBufferListener[] listeners) {
        this(maxSize, timeWindowMs, listeners, null);
    }

    public TimeWindowBuffer(int maxSize, long timeWindowMs, final TimeWindowBufferListener[] listeners, String delimiter) {
        this.maxSize = maxSize;
        this.timeWindow = timeWindowMs;
        this.listeners = Arrays.copyOf(listeners, listeners.length);
        this.delimiter = delimiter;

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                evaluate();
            }
        }, timeWindow, timeWindow);
    }

    @Override
    public int size() {
        int result = 0;
        synchronized (this) {
            result = super.size();
        }
        return result;
    }

    public void stop() {
        timer.cancel();
        timer = null;
    }
}