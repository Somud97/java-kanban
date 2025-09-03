package test.tracker.history;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.history.HistoryManager;
import tracker.history.InMemoryHistoryManager;
import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    private HistoryManager historyManager;
    private int idCounter = 1;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
    }

    private Task createTask(String title, String description) {
        Task task = new Task(title, description);
        task.setId(idCounter++);
        return task;
    }

    private Epic createEpic(String title, String description) {
        Epic epic = new Epic(title, description);
        epic.setId(idCounter++);
        return epic;
    }

    private Subtask createSubtask(String title, String description, Epic epic) {
        Subtask subtask = new Subtask(title, description, epic.getId(), null, null);
        subtask.setId(idCounter++);
        return subtask;
    }

    @Test
    void shouldAddTask() {
        Task task = createTask("Test Task", "Test Description");
        historyManager.add(task);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(task, history.get(0));
    }

    @Test
    void shouldAddMultipleTasks() {
        Task task1 = createTask("Task 1", "Description 1");
        Task task2 = createTask("Task 2", "Description 2");
        Task task3 = createTask("Task 3", "Description 3");

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        List<Task> history = historyManager.getHistory();
        assertEquals(3, history.size());
        assertEquals(task1, history.get(0));
        assertEquals(task2, history.get(1));
        assertEquals(task3, history.get(2));
    }

    @Test
    void shouldHandleEmptyHistory() {
        List<Task> history = historyManager.getHistory();
        assertTrue(history.isEmpty());
    }

    @Test
    void shouldHandleDuplicates() {
        Task task = createTask("Test Task", "Test Description");

        // Добавляем одну и ту же задачу несколько раз
        historyManager.add(task);
        historyManager.add(task);
        historyManager.add(task);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(task, history.get(0));
    }

    @Test
    void shouldMoveTaskToEndWhenReAdded() {
        Task task1 = createTask("Task 1", "Description 1");
        Task task2 = createTask("Task 2", "Description 2");
        Task task3 = createTask("Task 3", "Description 3");

        // Добавляем задачи в определенном порядке
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        // Проверяем начальный порядок
        List<Task> history = historyManager.getHistory();
        assertEquals(task1, history.get(0));
        assertEquals(task2, history.get(1));
        assertEquals(task3, history.get(2));

        // Добавляем task1 снова - он должен переместиться в конец
        historyManager.add(task1);

        history = historyManager.getHistory();
        assertEquals(3, history.size());
        assertEquals(task2, history.get(0));
        assertEquals(task3, history.get(1));
        assertEquals(task1, history.get(2));
    }

    @Test
    void shouldRemoveTaskFromBeginning() {
        Task task1 = createTask("Task 1", "Description 1");
        Task task2 = createTask("Task 2", "Description 2");
        Task task3 = createTask("Task 3", "Description 3");

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        // Удаляем задачу из начала
        historyManager.remove(task1.getId());

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task2, history.get(0));
        assertEquals(task3, history.get(1));
    }

    @Test
    void shouldRemoveTaskFromMiddle() {
        Task task1 = createTask("Task 1", "Description 1");
        Task task2 = createTask("Task 2", "Description 2");
        Task task3 = createTask("Task 3", "Description 3");

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        // Удаляем задачу из середины
        historyManager.remove(task2.getId());

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task1, history.get(0));
        assertEquals(task3, history.get(1));
    }

    @Test
    void shouldRemoveTaskFromEnd() {
        Task task1 = createTask("Task 1", "Description 1");
        Task task2 = createTask("Task 2", "Description 2");
        Task task3 = createTask("Task 3", "Description 3");

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        // Удаляем задачу из конца
        historyManager.remove(task3.getId());

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task1, history.get(0));
        assertEquals(task2, history.get(1));
    }

    @Test
    void shouldRemoveNonExistentTask() {
        Task task = createTask("Test Task", "Test Description");
        historyManager.add(task);

        // Удаляем несуществующую задачу
        historyManager.remove(999);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(task, history.get(0));
    }

    @Test
    void shouldRemoveFromEmptyHistory() {
        historyManager.remove(1);

        List<Task> history = historyManager.getHistory();
        assertTrue(history.isEmpty());
    }

    @Test
    void shouldHandleAllTaskTypes() {
        Task task = createTask("Task", "Description");
        Epic epic = createEpic("Epic", "Description");
        Subtask subtask = createSubtask("Subtask", "Description", epic);

        historyManager.add(task);
        historyManager.add(epic);
        historyManager.add(subtask);

        List<Task> history = historyManager.getHistory();
        assertEquals(3, history.size());
        assertEquals(task, history.get(0));
        assertEquals(epic, history.get(1));
        assertEquals(subtask, history.get(2));
    }

    @Test
    void shouldMaintainOrderAfterMultipleOperations() {
        Task task1 = createTask("Task 1", "Description 1");
        Task task2 = createTask("Task 2", "Description 2");
        Task task3 = createTask("Task 3", "Description 3");
        Task task4 = createTask("Task 4", "Description 4");

        // Добавляем задачи
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        // Проверяем порядок
        List<Task> history = historyManager.getHistory();
        assertEquals(3, history.size());
        assertEquals(task1, history.get(0));
        assertEquals(task2, history.get(1));
        assertEquals(task3, history.get(2));

        // Добавляем новую задачу
        historyManager.add(task4);
        history = historyManager.getHistory();
        assertEquals(4, history.size());
        assertEquals(task1, history.get(0));
        assertEquals(task2, history.get(1));
        assertEquals(task3, history.get(2));
        assertEquals(task4, history.get(3));

        // Перемещаем task2 в конец
        historyManager.add(task2);
        history = historyManager.getHistory();
        assertEquals(4, history.size());
        assertEquals(task1, history.get(0));
        assertEquals(task3, history.get(1));
        assertEquals(task4, history.get(2));
        assertEquals(task2, history.get(3));

        // Удаляем task1
        historyManager.remove(task1.getId());
        history = historyManager.getHistory();
        assertEquals(3, history.size());
        assertEquals(task3, history.get(0));
        assertEquals(task4, history.get(1));
        assertEquals(task2, history.get(2));
    }

    @Test
    void shouldHandleLargeHistory() {
        // Создаем много задач
        for (int i = 1; i <= 100; i++) {
            Task task = createTask("Task " + i, "Description " + i);
            historyManager.add(task);
        }

        List<Task> history = historyManager.getHistory();
        assertEquals(100, history.size());

        // Проверяем порядок
        for (int i = 0; i < 100; i++) {
            assertEquals("Task " + (i + 1), history.get(i).getTitle());
        }
    }

    @Test
    void shouldHandleNullTask() {
        // Тест на обработку null (если такое возможно)
        assertDoesNotThrow(() -> {
            historyManager.add(null);
        });
    }

    @Test
    void shouldHandleTaskWithNullId() {
        Task task = new Task("Test Task", "Test Description");
        task.setId(0); // Устанавливаем недопустимый ID

        historyManager.add(task);
        historyManager.remove(0);

        List<Task> history = historyManager.getHistory();
        assertEquals(0, history.size());
    }

    @Test
    void shouldHandleConsecutiveRemovals() {
        Task task1 = createTask("Task 1", "Description 1");
        Task task2 = createTask("Task 2", "Description 2");
        Task task3 = createTask("Task 3", "Description 3");

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        // Удаляем все задачи подряд
        historyManager.remove(task1.getId());
        historyManager.remove(task2.getId());
        historyManager.remove(task3.getId());

        List<Task> history = historyManager.getHistory();
        assertTrue(history.isEmpty());
    }

    @Test
    void shouldHandleRepeatedAdditionsAndRemovals() {
        Task task = createTask("Test Task", "Test Description");

        // Добавляем и удаляем одну и ту же задачу несколько раз
        historyManager.add(task);
        historyManager.remove(task.getId());
        historyManager.add(task);
        historyManager.remove(task.getId());
        historyManager.add(task);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(task, history.get(0));
    }
}