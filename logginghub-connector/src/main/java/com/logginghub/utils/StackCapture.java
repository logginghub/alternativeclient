package com.logginghub.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class StackCapture {

    private TimeProvider timeProvider = new SystemTimeProvider();

    public void setTimeProvider(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    public StackSnapshot capture(String environment, String host, String instanceType, int instanceNumber) {

        long time = timeProvider.getTime();
        Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();
        Set<Entry<Thread, StackTraceElement[]>> entrySet = allStackTraces.entrySet();

        int traceIndex = 0;
//        StackTrace[] traces = new StackTrace[entrySet.size()];
        List<StackTrace> traces = new ArrayList<StackTrace>(entrySet.size());
        
        for (Entry<Thread, StackTraceElement[]> entry : entrySet) {

            Thread thread = entry.getKey();

            String name = thread.getName();
            if (!name.equals("LoggingHub-strobeExecutor")) {
                String state = thread.getState().toString();
                long id = thread.getId();

                StackTraceElement[] value = entry.getValue();
                StackTraceItem[] items = new StackTraceItem[value.length];
                int index = 0;
                for (StackTraceElement stackTraceElement : value) {

                    String className = stackTraceElement.getClassName();
                    String fileName = stackTraceElement.getFileName();
                    int lineNumber = stackTraceElement.getLineNumber();
                    String methodName = stackTraceElement.getMethodName();

                    items[index++] = new StackTraceItem(className, methodName, fileName, lineNumber);
                }

                traces.add(new StackTrace(name, state, id, items));
            }

        }

        Collections.sort(traces, new Comparator<StackTrace>() {
            public int compare(StackTrace o1, StackTrace o2) {
                return o1.getThreadName().compareTo(o2.getThreadName());
            }
        });

        StackSnapshot snapshot = new StackSnapshot(environment, host, instanceType, instanceNumber, time, traces.toArray(new StackTrace[traces.size()]));
        return snapshot;
    }
}
