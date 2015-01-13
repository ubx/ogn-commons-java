/**
 * Copyright (c) 2015 OGN, All Rights Reserved.
 */

package org.ogn.commons.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
public class TimeWindowBuffer<T> {

    private int strLen;
    private final int maxStrLen;
    private final long timeWindow;
    private final String delimiter;

    private Timer timer;

    private List<T> buffer = new ArrayList<>();

    private long t;

    private TimeWindowBufferListener[] listeners;

    public void add(T obj) {
        synchronized (buffer) {
            buffer.add(obj);

            if (buffer.isEmpty())
                strLen += obj.toString().length();
            else
                strLen += delimiter == null ? obj.toString().length() : obj.toString().length() + delimiter.length();

            if (strLen > maxStrLen) {
                notifyListenersAndClearBuffer();
            }
        }// sync
    }

    private void notifyListenersAndClearBuffer() {
        synchronized (buffer) {
            long ct = System.currentTimeMillis();
            if (t > 0) {

                // skip if buffer is not full and last iteration was less then (timeWindow>>4) ms ago
                if (ct - t < (timeWindow >> 4) && strLen < maxStrLen) {
                    return;
                }
            }
            t = ct;

            String str = toStr(buffer);
            if (str.length() > 0)
                synchronized (listeners) {
                    for (TimeWindowBufferListener l : listeners) {
                        l.tick(str);
                    }
                    // clear the buffer
                    buffer.clear();
                    strLen = 0;
                }// sync
        }// sync
    }

    private String toStr(List<? extends T> list) {
        StringBuilder strBld = new StringBuilder();
        synchronized (list) {
            int index = 0;
            for (T s : list) {
                strBld.append(s);
                if (++index < list.size() && delimiter != null)
                    strBld.append(delimiter);
            }
        }// sync

        return strBld.toString();
    }

    public TimeWindowBuffer(int maxStrLen, long timeWindowMs, final TimeWindowBufferListener listener) {
        this(maxStrLen, timeWindowMs, new TimeWindowBufferListener[] { listener }, null);
    }

    public TimeWindowBuffer(int maxStrLen, long timeWindowMs, final TimeWindowBufferListener listener, String delimiter) {
        this(maxStrLen, timeWindowMs, new TimeWindowBufferListener[] { listener }, delimiter);
    }

    public TimeWindowBuffer(int maxStrLen, long timeWindowMs, final TimeWindowBufferListener[] listeners) {
        this(maxStrLen, timeWindowMs, listeners, null);
    }

    public TimeWindowBuffer(int maxStrLen, long timeWindowMs, final TimeWindowBufferListener[] listeners,
            String delimiter) {
        this.maxStrLen = maxStrLen;
        this.timeWindow = timeWindowMs;
        this.listeners = Arrays.copyOf(listeners, listeners.length);
        this.delimiter = delimiter;

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                notifyListenersAndClearBuffer();
            }
        }, timeWindow, timeWindow);
    }

    public int size() {
        int result = 0;
        synchronized (buffer) {
            result = buffer.size();
        }
        return result;
    }

    public void stop() {
        timer.cancel();
        timer = null;
    }
}