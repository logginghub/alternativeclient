package com.logginghub.connector.common;

import com.logginghub.utils.CpuLogger;
import com.logginghub.utils.GCWatcher;
import com.logginghub.utils.HeapLogger;


public interface AppenderHelperCustomisationInterface {
    HeapLogger createHeapLogger();
    CpuLogger createCPULogger();
    GCWatcher createGCWatcher();
}
