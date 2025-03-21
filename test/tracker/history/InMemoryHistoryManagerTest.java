package test.tracker.history;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.history.InMemoryHistoryManager;
import tracker.model.Task;

import java.util.Deque;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    private InMemoryHistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
    }

    @Test
    void shouldAddTaskToHistory() {
        Task task = new Task("Test Task", "Description");

        historyManager.add(task);

        Deque<Task> history = historyManager.getHistory();
        assertNotNull(history, "History should not be null");
        assertEquals(1, history.size(), "History should contain 1 task");
        assertEquals(task, history.getFirst(), "The first task in history should be the added task");
    }

    @Test
    void shouldAddMultipleTasksToHistory() {
        Task task1 = new Task("Task 1", "Description");
        Task task2 = new Task("Task 2", "Description");

        historyManager.add(task1);
        historyManager.add(task2);

        Deque<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "History should contain 2 tasks");
        assertEquals(task1, history.getFirst(), "The first task in history should be Task 1");
        assertEquals(task2, history.getLast(), "The last task in history should be Task 2");
    }

    @Test
    void shouldRemoveOldestTaskWhenHistoryExceedsMaxSize() {
        for (int i = 1; i <= 10; i++) {
            historyManager.add(new Task("Task " + i, "Description " + i));
        }

        Task newTask = new Task("Task 11", "Description 11");
        historyManager.add(newTask);

        Deque<Task> history = historyManager.getHistory();

        assertEquals(10, history.size(), "History size should not exceed MAX_HISTORY_SIZE");
        assertFalse(history.contains(new Task("Task 1", "Description 1")),
                "The oldest task should be removed from history");
        assertEquals(newTask, history.getLast(), "The newest task should be added to the history");
    }

    @Test
    void shouldReturnEmptyHistoryWhenNoTasksAdded() {
        Deque<Task> history = historyManager.getHistory();

        assertNotNull(history, "History should not be null");
        assertTrue(history.isEmpty(), "History should be empty when no tasks are added");
    }

    @Test
    void shouldReturnCopyOfHistory() {
        Task task = new Task("Task 1", "Description");
        historyManager.add(task);

        Deque<Task> returnedHistory = historyManager.getHistory();

        returnedHistory.clear();

        assertEquals(1, historyManager.getHistory().size(),
                "Clearing the returned history should not affect the internal task history");
    }
}