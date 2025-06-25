package test.tracker.history;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.history.InMemoryHistoryManager;
import tracker.model.Task;

import java.util.List;

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

        List<Task> history = historyManager.getHistory();
        assertNotNull(history, "History should not be null");
        assertEquals(1, history.size(), "History should contain 1 task");
        assertEquals(task, history.getFirst(), "The first task in history should be the added task");
    }

    @Test
    void shouldAddMultipleTasksToHistory() {
        Task task1 = new Task("Task 1", "Description");
        task1.setId(1);
        Task task2 = new Task("Task 2", "Description");
        task2.setId(2);

        historyManager.add(task1);
        historyManager.add(task2);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "History should contain 2 tasks");
        assertEquals(task1, history.getFirst(), "The first task in history should be Task 1");
        assertEquals(task2, history.getLast(), "The last task in history should be Task 2");
    }

    @Test
    void shouldReturnEmptyHistoryWhenNoTasksAdded() {
        List<Task> history = historyManager.getHistory();

        assertNotNull(history, "History should not be null");
        assertTrue(history.isEmpty(), "History should be empty when no tasks are added");
    }

    @Test
    void shouldReturnCopyOfHistory() {
        Task task = new Task("Task 1", "Description");
        historyManager.add(task);

        List<Task> returnedHistory = historyManager.getHistory();

        returnedHistory.clear();

        assertEquals(1, historyManager.getHistory().size(),
                "Clearing the returned history should not affect the internal task history");
    }

    @Test
    void shouldRemoveTaskFromHistoryById() {
        Task task1 = new Task("Task 1", "Description");
        task1.setId(1);
        Task task2 = new Task("Task 2", "Description");
        task2.setId(2);
        Task task3 = new Task("Task 3", "Description");
        task3.setId(3);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        // Удаляем задачу из середины
        historyManager.remove(2);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "History should contain 2 tasks after removal");
        assertTrue(history.contains(task1), "History should contain task1");
        assertFalse(history.contains(task2), "History should not contain removed task2");
        assertTrue(history.contains(task3), "History should contain task3");
    }
}