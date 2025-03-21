package test.tracker.utils;

import org.junit.jupiter.api.Test;
import tracker.history.HistoryManager;
import tracker.history.InMemoryHistoryManager;
import tracker.taskManager.InMemoryTaskManager;
import tracker.taskManager.TaskManager;
import tracker.utils.Managers;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ManagersTest {

    @Test
    void shouldReturnInMemoryTaskManagerInstance() {
        TaskManager taskManager = Managers.getDefault();

        assertTrue(taskManager instanceof InMemoryTaskManager,
                "Expected instance of InMemoryTaskManager");
    }

    @Test
    void shouldReturnInMemoryHistoryManagerInstance() {
        HistoryManager historyManager = Managers.getDefaultHistory();

        assertTrue(historyManager instanceof InMemoryHistoryManager,
                "Expected instance of InMemoryHistoryManager");
    }

    @Test
    void shouldReturnNonNullTaskManager() {
        TaskManager taskManager = Managers.getDefault();

        assertTrue(taskManager != null, "TaskManager instance should not be null");
    }

    @Test
    void shouldReturnNonNullHistoryManager() {
        HistoryManager historyManager = Managers.getDefaultHistory();

        assertTrue(historyManager != null, "HistoryManager instance should not be null");
    }
}
