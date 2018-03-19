package towel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An error reporter that logs the messages it receives
 */
public class FileAwareLoggingErrorReporter implements ErrorReporter {

    private Map<String, List<LogEntry>> notices = new HashMap<>();
    private Map<String, List<LogEntry>> errors = new HashMap<>();

    public static final String DEFAULT_LOG_NAME = "default";

    private String file = DEFAULT_LOG_NAME;

    public FileAwareLoggingErrorReporter() {
        notices.put(DEFAULT_LOG_NAME, new ArrayList<>());
        errors.put(DEFAULT_LOG_NAME, new ArrayList<>());
    }

    private List<LogEntry> getLogFor(Type type) {

        Map<String, List<LogEntry>> map = notices;

        if (type == Type.ERROR) {
            map = errors;
        }

        if (!map.containsKey(file)) {
            map.put(file, new ArrayList<LogEntry>());
        }

        return map.get(file);
    }

    private void addForCurrentFile(Type type, String message) {
        getLogFor(type).add(new LogEntry(type, message));
    }

    private void addForCurrentFile(Type type, String message, int line, int character) {
        getLogFor(type).add(new LogEntry(type, message, line, character));
    }

    public void setCurrentFile(String file) {
        this.file = file;
    }

    @Override
    public void notice(String message) {
        addForCurrentFile(Type.NOTICE, message);
    }

    @Override
    public void error(String message) {
        addForCurrentFile(Type.ERROR, message);
    }

    @Override
    public void notice(String message, int line, int character) {
        addForCurrentFile(Type.NOTICE, message, line, character);
    }

    @Override
    public void error(String message, int line, int character) {
        addForCurrentFile(Type.ERROR, message, line, character);
    }

    public boolean hasNotices() {
        for (Map.Entry<String, List<FileAwareLoggingErrorReporter.LogEntry>> errorsInFile : notices.entrySet()) {
            if (!errorsInFile.getValue().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasErrors() {
        for (Map.Entry<String, List<FileAwareLoggingErrorReporter.LogEntry>> errorsInFile : errors.entrySet()) {
            if (!errorsInFile.getValue().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    Map<String, List<LogEntry>> getNotices() {
        return notices;
    }

    Map<String, List<LogEntry>> getErrors() {
        return errors;
    }

    public enum Type {
        ERROR, NOTICE
    }

    public class LogEntry {
        public final String message;
        public final int line;
        public final int character;
        public final Type type;

        private LogEntry(Type type, String message) {
            this(type, message, -1, -1);
        }

        private LogEntry(Type type, String message, int line, int character) {
            this.message = message;
            this.line = line;
            this.character = character;
            this.type = type;
        }

        @Override
        public String toString() {
            return String.format("%s at line %d character %d.\n%s", type == Type.ERROR ? "Error" : "Notice", line, character, message);
        }
    }
}
