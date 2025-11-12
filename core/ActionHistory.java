package core;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Tracks all ADB actions performed during the session.
 */
public class ActionHistory {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    public static class Entry {
        private final LocalDateTime timestamp;
        private final String action;
        private final String details;
        private final boolean success;
        
        public Entry(String action, String details, boolean success) {
            this.timestamp = LocalDateTime.now();
            this.action = action;
            this.details = details;
            this.success = success;
        }
        
        public String getTime() {
            return timestamp.format(TIME_FORMAT);
        }
        
        public String getAction() {
            return action;
        }
        
        public String getDetails() {
            return details;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getStatus() {
            return success ? "✓" : "✗";
        }
    }
    
    private final ObservableList<Entry> entries = FXCollections.observableArrayList();
    
    public void log(String action, String details, boolean success) {
        entries.add(new Entry(action, details, success));
    }

    public List<Entry> getEntries() {
        return new ArrayList<>(entries);
    }

    public ObservableList<Entry> getObservableEntries() {
        return entries;
    }

    public void clear() {
        entries.clear();
    }

    public String exportToText() {
        StringBuilder sb = new StringBuilder();
        sb.append("ADB Debloater - Action History\n");
        sb.append("=".repeat(80)).append("\n\n");
        
        for (Entry e : entries) {
            sb.append(String.format("[%s] %s %s - %s\n", 
                e.getTime(), 
                e.getStatus(),
                e.getAction(), 
                e.getDetails()));
        }
        
        return sb.toString();
    }
}
