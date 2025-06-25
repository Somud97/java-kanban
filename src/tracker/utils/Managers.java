package tracker.utils;

import tracker.history.HistoryManager;
import tracker.history.InMemoryHistoryManager;
import tracker.taskManager.InMemoryTaskManager;
import tracker.taskManager.TaskManager;

public class Managers {
    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
