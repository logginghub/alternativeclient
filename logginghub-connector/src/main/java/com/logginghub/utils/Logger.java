package com.logginghub.utils;

import java.io.File;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class Logger {

    private static List<LevelSetter> levelSetters = new ArrayList<LevelSetter>();
    private int level = Logger.deferToRoot;
    private TimeProvider timeProvider = new SystemTimeProvider();
    private List<LoggerStream> streams = null;
    private int indent;
    private static ThreadLocal<String> threadContexts = new ThreadLocal<String>();

    private static List<Logger> allLoggers = new ArrayList<Logger>();

    private static Map<String, Logger> loggersForClass = new FactoryMapDecorator<String, Logger>(new HashMap<String, Logger>()) {
        @Override protected Logger createNewValue(String key) {
            return getNewLoggerFor(key);
        }
    };

    private static Logger root;
    

    public final static int deferToRoot = -1;

    // jshaw - this is a deviation from the java util logging constants
    // public final static int all = 0x80000000;
    public final static int all = 0;

    public final static int finest = 300;
    public final static int finer = 400;
    public final static int fine = 500;
    public final static int trace = 300;
    public final static int debug = 500;
    public final static int config = 700;
    public final static int info = 800;
    public final static int warning = 900;
    public final static int severe = 1000;

    private static final boolean debugFlag = Boolean.getBoolean("vllogging.debug");
    
    private String threadContextOverride = null;
    private String name;

    static {
        root = new Logger("");
        root.addStream(new SystemErrStream());
        root.setLevel(info);
        loggersForClass.put("", root);

        loadProperties();
    }

    public Logger(String name) {
        this.name = name;
    }

    private static void loadProperties() {
        
        String propertyResourceLocation = EnvironmentProperties.getString("vllogging.properties", "vllogging.properties");
        if (debugFlag) {
            Out.out("[VLLogging] Loading properties from '{}'", propertyResourceLocation);
        }

        loadPropertiesFromResource(propertyResourceLocation);

        String userhome = System.getProperty("user.home");
        final File potentialGlobalSettings = new File(userhome, ".vertexlabs/vllogging.properties");
        if (potentialGlobalSettings.exists()) {
            WorkerThread.everyNowDaemon("LoggingHub-LoggerConfigReader", 1, TimeUnit.SECONDS, new Runnable() {
                long time = 0;

                public void run() {
                    long fileTime = potentialGlobalSettings.lastModified();
                    if (time != fileTime) {
                        if (debugFlag) {
                            Out.out("[VLLogging] Loading properties from '{}'", potentialGlobalSettings.getAbsolutePath());
                        }

                        resetLevels();
                        loadPropertiesFromResource(potentialGlobalSettings.getAbsolutePath());
                        time = fileTime;
                    }
                }
            });
        }
    }

    protected static void resetLevels() {
        synchronized (loggersForClass) {
            Collection<Logger> values = loggersForClass.values();
            for (Logger logger : values) {
                if (logger != root) {
                    if(debugFlag) {
                        Out.out("Reseting {} to defer to root", logger.getName());
                    }
                    logger.setLevel(deferToRoot);
                }
            }
        }
    }

    private static void loadPropertiesFromResource(String propertyResourceLocation) {
        String read = ResourceUtils.readOrNull(propertyResourceLocation);
        if (read != null) {
            List<String> splitIntoLineList = StringUtils.splitIntoLineList(read);
            for (String line : splitIntoLineList) {

                String[] split = line.split("=");
                if (split.length == 2) {
                    String key = split[0];
                    String value = split[1];

                    if (key.trim().startsWith("#")) {
                        // Commented out
                    }
                    else {

                        int level = parseLevel(value);

                        if (debugFlag) {
                            Out.out("[VLLogging] Setting level '{}' = {}", key, level);
                        }
                        setLevel(key, level);
                    }
                }

            }
        }
        else {
            if (debugFlag) {
                Out.out("[VLLogging] No file or resource found for '{}', skipping", propertyResourceLocation);
            }
        }
    }

    public String getName() {
        return name;
    }

    public static Logger root() {
        return root;
    }

    public static void setLevel(int level, Class<?>... classes) {
        for (Class<?> clazz : classes) {
            setLevel(clazz, level);
        }
    }

    public static void setLevel(Class<?> clazz, int level) {
        getLoggerFor(clazz).setLevel(level);
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    @Override public String toString() {
        return "Logger [" + name + "]";

    }

    public static Object format(final int value) {
        return new Object() {
            @Override public String toString() {
                return NumberFormat.getInstance().format(value);
            }
        };
    }

    public static Object format(final double value) {
        return new Object() {
            @Override public String toString() {
                return NumberFormat.getInstance().format(value);
            }
        };
    }

    public static Object toIdentity(final Object object) {
        return new Object() {
            @Override public String toString() {
                return "0x" + System.identityHashCode(object);
            }
        };
    }

    public static Object toDateString(final long date) {
        return new Object() {
            @Override public String toString() {
                DateFormat dateThenTimeWithMillis = DateFormatFactory.getDateThenTimeWithMillis(DateFormatFactory.utc);
                dateThenTimeWithMillis.setTimeZone(TimeZone.getTimeZone("UTC"));
                return dateThenTimeWithMillis.format(new Date(date));
            }
        };
    }

    public synchronized void addStream(LoggerStream stream) {
        if (streams == null) {
            streams = new CopyOnWriteArrayList<LoggerStream>();
        }
        streams.add(stream);
    }

    public void severe(String format, Object... objects) {
        if ((level < 0 && root.level <= severe) || (level >= 0 && level <= warning)) {
            log(severe, format, objects);
        }
    }

    public void warn(String format, Object... objects) {
        if ((level < 0 && root.level <= warning) || (level >= 0 && level <= warning)) {
            log(warning, format, objects);
        }
    }

    public void warning(String format, Object... objects) {
        if ((level < 0 && root.level <= warning) || (level >= 0 && level <= warning)) {
            log(warning, format, objects);
        }
    }

    public void info(int patternID, String... values) {
        if ((level < 0 && root.level <= info) || (level >= 0 && level <= info)) {
            log(info, patternID, values);
        }
    }

    public void info(String format, Object... objects) {
        if ((level < 0 && root.level <= info) || (level >= 0 && level <= info)) {
            log(info, format, objects);
        }
    }

    public void debug(String format, Object... objects) {
        if ((level < 0 && root.level <= debug) || (level >= 0 && level <= debug)) {
            log(debug, format, objects);
        }
    }

    public void fine(String format, Object... objects) {
        if ((level < 0 && root.level <= fine) || (level >= 0 && level <= fine)) {
            log(Level.FINE.intValue(), format, objects);
        }
    }

    public void finer(String format, Object... objects) {
        if ((level < 0 && root.level <= finer) || (level >= 0 && level <= finer)) {
            log(finer, format, objects);
        }
    }

    public void trace(String format, Object... objects) {
        if ((level < 0 && root.level <= trace) || (level >= 0 && level <= trace)) {
            log(trace, format, objects);
        }
    }

    public boolean willLog(int testLevel) {
        return ((level < 0 && root.level <= testLevel) || (level >= 0 && level <= testLevel));
    }

    public void finest(String format, Object... objects) {
        if ((level < 0 && root.level <= finest) || (level >= 0 && level <= finest)) {
            log(finest, format, objects);
        }
    }

    private void log(int level, String format, Object[] objects) {
        if (threadContextOverride != null) {
            setThreadContext(threadContextOverride);
        }

        LogEvent event = new LogEvent();
        if (format != null) {
            event.setMessage(format(format, objects));
        }
        else {
            event.setMessage("null");
        }
        event.setSourceClassName(StacktraceUtils.getCallingClassName(2));
        event.setSourceMethodName(StacktraceUtils.getCallingMethodName(2));
        event.setLocalCreationTimeMillis(timeProvider.getTime());
        event.setLevel(level);
        event.setThreadName(Thread.currentThread().getName());
        event.setThreadContext(threadContexts.get());
        logEvent(event);
    }

    private void log(int level, int patternID, String[] objects) {
        if (threadContextOverride != null) {
            setThreadContext(threadContextOverride);
        }

        LogEvent event = new LogEvent();
        event.setMessage("null");
        event.setSourceClassName(StacktraceUtils.getCallingClassName(2));
        event.setSourceMethodName(StacktraceUtils.getCallingMethodName(2));
        event.setLocalCreationTimeMillis(timeProvider.getTime());
        event.setLevel(level);
        event.setThreadName(Thread.currentThread().getName());
        event.setThreadContext(threadContexts.get());
        event.setPatternID(patternID);
        event.setParameters(objects);
        logEvent(event);
    }

    private void log(int level, String message) {
        if (threadContextOverride != null) {
            setThreadContext(threadContextOverride);
        }

        LogEvent event = new LogEvent();
        event.setMessage(message);
        event.setSourceClassName(StacktraceUtils.getCallingClassName(2));
        event.setSourceMethodName(StacktraceUtils.getCallingMethodName(2));
        event.setLocalCreationTimeMillis(timeProvider.getTime());
        event.setLevel(level);
        event.setThreadName(Thread.currentThread().getName());
        event.setThreadContext(threadContexts.get());
        logEvent(event);
    }

    private String format(String format, Object[] objects) {
        return StringUtils.repeat("  ", indent) + StringUtils.format(format, objects);
    }

    private void logEvent(LogEvent event) {

        if (streams == null) {
            // TODO : I think the expectation is probably to use the parents streams in order?
            // Although this is faster, and I've never seen anyone create a sensible tree of
            // appenders!
            root.logEvent(event);
        }
        else {
            for (LoggerStream loggerStream : streams) {
                loggerStream.onNewLogEvent(event);
            }
        }
    }

    public static Logger getNewLoggerFor(String key) {
        Logger logger = new Logger(key);
        allLoggers.add(logger);
        applyLevelSetters(logger);
        return logger;
    }

    public static Logger getNewLoggerFor(Class key) {
        return getNewLoggerFor(key.getName());
    }

    /**
     * Creates a new logger that is deemed "safe" as it is detached from its parent streams so can't
     * be re-entrant
     * 
     * @param clazz
     * @return
     */
    public static Logger getSafeLoggerFor(Class<?> clazz) {
        Logger safeLogger = getNewLoggerFor(clazz.getName());
        safeLogger.addStream(new SystemErrStream());
        return safeLogger;
    }

    public static synchronized Logger getLoggerFor(Class<?> c) {
        return loggersForClass.get(c.getName());
    }

    public static synchronized Logger getLoggerFor(String classname) {
        return loggersForClass.get(classname);
    }

    public static void setRootLevel(int level) {
        root.setLevel(level);
    }

    public LogEvent getCallerContext() {
        // These should be 2, but for some reason I'd changed them to 3, so
        // something is wrong somewhere...
        LogEvent event = new LogEvent();
        event.setSourceClassName(StacktraceUtils.getCallingClassName(2));
        event.setSourceMethodName(StacktraceUtils.getCallingMethodName(2));
        event.setThreadName(Thread.currentThread().getName());
        event.setThreadContext(threadContexts.get());
        return event;

    }

    public void logFromContext(LogEvent context, int level, String format, Object... objects) {
        LogEvent event = new LogEvent();
        event.setMessage(StringUtils.format(format, objects));
        event.setSourceClassName(context.getSourceClassName());
        event.setSourceMethodName(context.getSourceMethodName());
        event.setLocalCreationTimeMillis(timeProvider.getTime());
        event.setLevel(level);
        event.setThreadName(context.getThreadName());
        event.setThreadContext(threadContexts.get());
        logEvent(event);
    }

    public void warning(Throwable t) {
        LogEvent event = new LogEvent();
        event.setMessage(t.getMessage());
        event.setSourceClassName(StacktraceUtils.getCallingClassName(1));
        event.setSourceMethodName(StacktraceUtils.getCallingMethodName(1));
        event.setLocalCreationTimeMillis(timeProvider.getTime());
        event.setLevel(warning);
        event.setThreadName(Thread.currentThread().getName());
        event.setThrowable(t);
        event.setThreadContext(threadContexts.get());
        logEvent(event);
    }

    public void trace(Throwable t) {
        LogEvent event = new LogEvent();
        event.setMessage(t.getMessage());
        // Brought these guys down to 2 levels as we've not go the log call to
        // detail with
        event.setSourceClassName(StacktraceUtils.getCallingClassName(2));
        event.setSourceMethodName(StacktraceUtils.getCallingMethodName(2));
        event.setLocalCreationTimeMillis(timeProvider.getTime());
        event.setLevel(trace);
        event.setThreadName(Thread.currentThread().getName());
        event.setThrowable(t);
        event.setThreadContext(threadContexts.get());
        logEvent(event);
    }

    public void info(Throwable t) {
        LogEvent event = new LogEvent();
        event.setMessage(t.getMessage());
        // Brought these guys down to 2 levels as we've not go the log call to
        // detail with
        event.setSourceClassName(StacktraceUtils.getCallingClassName(2));
        event.setSourceMethodName(StacktraceUtils.getCallingMethodName(2));
        event.setLocalCreationTimeMillis(timeProvider.getTime());
        event.setLevel(info);
        event.setThreadName(Thread.currentThread().getName());
        event.setThrowable(t);
        event.setThreadContext(threadContexts.get());
        logEvent(event);
    }

    public void debug(Stopwatch stopwatch) {
        if ((level < 0 && root.level <= debug) || (level >= 0 && level <= debug)) {
            log(debug, stopwatch.stopAndFormat());
        }
    }

    public void finer(Stopwatch stopwatch) {
        if ((level < 0 && root.level <= finer) || (level >= 0 && level <= finer)) {
            log(finer, stopwatch.stopAndFormat());
        }
    }

    public void finest(Stopwatch stopwatch) {
        if ((level < 0 && root.level <= finest) || (level >= 0 && level <= finest)) {
            log(finest, stopwatch.stopAndFormat());
        }
    }

    public void info(Stopwatch stopwatch) {
        if ((level < 0 && root.level <= info) || (level >= 0 && level <= info)) {
            log(info, stopwatch.stopAndFormat());
        }
    }

    public void fine(Stopwatch stopwatch) {
        if ((level < 0 && root.level <= fine) || (level >= 0 && level <= fine)) {
            log(fine, stopwatch.stopAndFormat());
        }
    }

    public void log(int level, Throwable t, String format, Object... objects) {
        LogEvent event = new LogEvent();
        event.setMessage(StringUtils.format(format, objects));
        event.setSourceClassName(StacktraceUtils.getCallingClassName(3));
        event.setSourceMethodName(StacktraceUtils.getCallingMethodName(3));
        event.setLocalCreationTimeMillis(timeProvider.getTime());
        event.setLevel(level);
        event.setThreadName(Thread.currentThread().getName());
        event.setThrowable(t);
        event.setThreadContext(threadContexts.get());
        logEvent(event);
    }

    public void debug(Throwable t, String format, Object... objects) {
        LogEvent event = new LogEvent();
        event.setMessage(StringUtils.format(format, objects));
        event.setSourceClassName(StacktraceUtils.getCallingClassName(3));
        event.setSourceMethodName(StacktraceUtils.getCallingMethodName(3));
        event.setLocalCreationTimeMillis(timeProvider.getTime());
        event.setLevel(debug);
        event.setThreadName(Thread.currentThread().getName());
        event.setThrowable(t);
        event.setThreadContext(threadContexts.get());
        logEvent(event);
    }

    public void warning(Throwable t, String format, Object... objects) {
        LogEvent event = new LogEvent();
        event.setMessage(StringUtils.format(format, objects));
        event.setSourceClassName(StacktraceUtils.getCallingClassName(3));
        event.setSourceMethodName(StacktraceUtils.getCallingMethodName(3));
        event.setLocalCreationTimeMillis(timeProvider.getTime());
        event.setLevel(warning);
        event.setThreadName(Thread.currentThread().getName());
        event.setThrowable(t);
        event.setThreadContext(threadContexts.get());
        logEvent(event);
    }

    public void severe(Throwable t, String format, Object... objects) {
        if ((level < 0 && root.level <= severe) || (level >= 0 && level <= severe)) {
            LogEvent event = new LogEvent();
            event.setMessage(StringUtils.format(format, objects));
            event.setSourceClassName(StacktraceUtils.getCallingClassName(3));
            event.setSourceMethodName(StacktraceUtils.getCallingMethodName(3));
            event.setLocalCreationTimeMillis(timeProvider.getTime());
            event.setLevel(severe);
            event.setThreadName(Thread.currentThread().getName());
            event.setThrowable(t);
            event.setThreadContext(threadContexts.get());
            logEvent(event);
        }
    }

    public void warn(Throwable t, String format, Object... objects) {
        LogEvent event = new LogEvent();
        event.setMessage(StringUtils.format(format, objects));
        event.setSourceClassName(StacktraceUtils.getCallingClassName(3));
        event.setSourceMethodName(StacktraceUtils.getCallingMethodName(3));
        event.setLocalCreationTimeMillis(timeProvider.getTime());
        event.setLevel(warning);
        event.setThreadName(Thread.currentThread().getName());
        event.setThrowable(t);
        event.setThreadContext(threadContexts.get());
        logEvent(event);
    }

    public void info(Throwable t, String format, Object... objects) {
        if ((level < 0 && root.level <= info) || (level >= 0 && level <= info)) {
            LogEvent event = new LogEvent();
            event.setMessage(StringUtils.format(format, objects));
            event.setSourceClassName(StacktraceUtils.getCallingClassName(3));
            event.setSourceMethodName(StacktraceUtils.getCallingMethodName(3));
            event.setLocalCreationTimeMillis(timeProvider.getTime());
            event.setLevel(info);
            event.setThreadName(Thread.currentThread().getName());
            event.setThrowable(t);
            event.setThreadContext(threadContexts.get());
            logEvent(event);
        }
    }
    
    public void fine(Throwable t, String format, Object... objects) {
        if ((level < 0 && root.level <= fine) || (level >= 0 && level <= fine)) {
            LogEvent event = new LogEvent();
            event.setMessage(StringUtils.format(format, objects));
            event.setSourceClassName(StacktraceUtils.getCallingClassName(3));
            event.setSourceMethodName(StacktraceUtils.getCallingMethodName(3));
            event.setLocalCreationTimeMillis(timeProvider.getTime());
            event.setLevel(fine);
            event.setThreadName(Thread.currentThread().getName());
            event.setThrowable(t);
            event.setThreadContext(threadContexts.get());
            logEvent(event);
        }
    }

    public void moreIndent() {
        indent++;
    }

    public void lessOutdent() {
        indent--;
        if (indent < 0) {
            indent = 0;
        }

    }

    public void divider(int level) {
        if (willLog(level)) {
            log(level,
                "-------------------------------------------------------------------------------------------------------------------------",
                null);
        }
    }

    public void logStack(int level) {
        if (willLog(level)) {
            LogEvent event = new LogEvent();
            event.setMessage("");
            event.setSourceClassName(StacktraceUtils.getCallingClassName(3));
            event.setSourceMethodName(StacktraceUtils.getCallingMethodName(3));
            event.setLocalCreationTimeMillis(timeProvider.getTime());
            event.setLevel(level);
            event.setThreadName(Thread.currentThread().getName());
            event.setThreadContext(threadContexts.get());

            Exception e = new Exception();
            e.fillInStackTrace();
            event.setThrowable(e);
            logEvent(event);
        }
    }

    public void setThreadContext(String name) {
        threadContexts.set(name);
    }

    public void setThreadContextOverride(String threadContextOverride) {
        this.threadContextOverride = threadContextOverride;
    }

    public static synchronized void setLevel(String partialClassname, int level) {
        LevelSetter levelSetter = new LevelSetter();
        levelSetter.setLevel(level);
        levelSetter.setPartialClass(partialClassname);

        levelSetters.add(levelSetter);

        Comparator<LevelSetter> comp = new Comparator<LevelSetter>() {
            public int compare(LevelSetter o1, LevelSetter o2) {
                return CompareUtils.add(o1.getPartialClass(), o2.getPartialClass()).add(o1.getLevel(), o2.getLevel()).compare();
            }
        };
        Collections.sort(levelSetters, comp);
        applyLevelSetters();
    }

    private static void applyLevelSetters() {
        for (Logger logger : allLoggers) {
            applyLevelSetters(logger);
        }
    }

    private static void applyLevelSetters(Logger logger) {
        for (LevelSetter levelSetter : levelSetters) {
            levelSetter.apply(logger);
        }
    }

    public static void trace(Class<?> c) {
        setLevel(c, trace);
    }

    public static void setLevelFromSystemProperty() {
        setRootLevel(Integer.getInteger("vllogging.level", Logger.info));
    }

//    public void setupUDPLogging(String destination) {
//        addStream(new UDPPatternisedLogEventStream(GlobalLoggingParameters.pid, GlobalLoggingParameters.applicationName, destination));
//    }

    public void clearStreams() {
        if (streams != null) {
            streams.clear();
        }
    }

    public static int parseLevel(String level) {

        String lowerCase = level.toLowerCase();
        int levelValue;
        char first = lowerCase.charAt(0);
        switch (first) {
            case 'a': {
                levelValue = Logger.all;
                break;
            }
            case 'd': {
                levelValue = Logger.debug;
                break;
            }
            case 't': {
                levelValue = Logger.trace;
                break;
            }
            case 's': {
                levelValue = Logger.severe;
                break;
            }
            case 'f': {
                if (lowerCase.equals("fatal")) {
                    levelValue = Logger.severe;
                }
                else if (lowerCase.equals("finer")) {
                    levelValue = Logger.finer;
                }
                else if (lowerCase.equals("finest")) {
                    levelValue = Logger.finest;
                }
                else if (lowerCase.equals("fine")) {
                    levelValue = Logger.fine;
                }
                else {
                    // TODO : how to indicate a problem!
                    levelValue = Logger.info;
                }
                break;
            }
            case 'w': {
                levelValue = Logger.warning;
                break;
            }
            case 'i': {
                levelValue = Logger.info;
                break;
            }
            case 'c': {
                // TODO : put this back in
                // levelValue = Logger.config;
                levelValue = Logger.info;
                break;
            }
            default: {
                levelValue = Integer.parseInt(level);
            }
        }

        return levelValue;
    }

}
