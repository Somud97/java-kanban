package test.tracker.taskManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;
import tracker.taskManager.TaskManager;
import tracker.utils.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T manager;
    private int idCounter = 1;

    protected abstract T createTaskManager();

    @BeforeEach
    void setUp() {
        manager = createTaskManager();
    }

    private int getNextId() {
        return idCounter++;
    }

    private Task createTask(String title, String description) {
        Task task = new Task(title, description);
        task.setId(getNextId());
        return task;
    }

    private Task createTaskWithTime(String title, String description, Duration duration, LocalDateTime startTime) {
        Task task = new Task(title, description, duration, startTime);
        task.setId(getNextId());
        return task;
    }

    private Epic createEpic(String title, String description) {
        Epic epic = new Epic(title, description);
        epic.setId(getNextId());
        return epic;
    }

    private Subtask createSubtask(String title, String description, int epicId) {
        Subtask subtask = new Subtask(title, description, epicId, null, null);
        subtask.setId(getNextId());
        return subtask;
    }

    private Subtask createSubtaskWithTime(String title, String description, int epicId,
                                          Duration duration, LocalDateTime startTime) {
        Subtask subtask = new Subtask(title, description, epicId, duration, startTime);
        subtask.setId(getNextId());
        return subtask;
    }

    @Test
    void shouldCreateTask() {
        Task task = createTask("Test Task", "Test Description");
        Task createdTask = manager.createTask(task);

        assertNotNull(createdTask);
        assertEquals("Test Task", createdTask.getTitle());
        assertEquals("Test Description", createdTask.getDescription());
        assertEquals(TaskStatus.NEW, createdTask.getStatus());
        assertTrue(createdTask.getId() > 0);
    }

    @Test
    void shouldCreateEpic() {
        Epic epic = createEpic("Test Epic", "Test Description");
        Epic createdEpic = manager.createEpic(epic);

        assertNotNull(createdEpic);
        assertEquals("Test Epic", createdEpic.getTitle());
        assertEquals("Test Description", createdEpic.getDescription());
        assertEquals(TaskStatus.NEW, createdEpic.getStatus());
        assertTrue(createdEpic.getId() > 0);
    }

    @Test
    void shouldCreateSubtask() {
        Epic epic = manager.createEpic(createEpic("Test Epic", "Test Description"));
        Subtask subtask = createSubtask("Test Subtask", "Test Description", epic.getId());
        Subtask createdSubtask = manager.createSubtask(subtask);

        assertNotNull(createdSubtask);
        assertEquals("Test Subtask", createdSubtask.getTitle());
        assertEquals(epic.getId(), createdSubtask.getEpicId());
        assertTrue(createdSubtask.getId() > 0);
    }

    @Test
    void shouldUpdateTask() {
        Task task = manager.createTask(createTask("Original Title", "Original Description"));
        Task updated = createTask("Updated Title", "Updated Description");
        updated.setId(task.getId());
        updated.setStatus(TaskStatus.IN_PROGRESS);

        manager.updateTask(updated);

        Task updatedTask = manager.getTaskById(task.getId());
        assertEquals("Updated Title", updatedTask.getTitle());
        assertEquals("Updated Description", updatedTask.getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, updatedTask.getStatus());
    }

    @Test
    void shouldUpdateSubtask() {
        Epic epic = manager.createEpic(createEpic("Test Epic", "Test Description"));
        Subtask subtask = manager.createSubtask(createSubtask("Original Title", "Original Description", epic.getId()));
        Subtask updated = createSubtask("Updated Title", "Updated Description", epic.getId());
        updated.setId(subtask.getId());
        updated.setStatus(TaskStatus.IN_PROGRESS);

        manager.updateSubtask(updated);

        Subtask updatedSubtask = manager.getSubtaskById(subtask.getId());
        assertEquals("Updated Title", updatedSubtask.getTitle());
        assertEquals("Updated Description", updatedSubtask.getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, updatedSubtask.getStatus());
    }

    @Test
    void shouldDeleteTask() {
        Task task = manager.createTask(createTask("Test Task", "Test Description"));
        int taskId = task.getId();

        manager.deleteTask(taskId);

        assertNull(manager.getTaskById(taskId));
        assertTrue(manager.getTasks().isEmpty());
    }

    @Test
    void shouldDeleteEpic() {
        Epic epic = manager.createEpic(createEpic("Test Epic", "Test Description"));
        int epicId = epic.getId();

        manager.deleteEpic(epicId);

        assertNull(manager.getEpicById(epicId));
        assertTrue(manager.getEpics().isEmpty());
    }

    @Test
    void shouldDeleteSubtask() {
        Epic epic = manager.createEpic(createEpic("Test Epic", "Test Description"));
        Subtask subtask = manager.createSubtask(createSubtask("Test Subtask", "Test Description", epic.getId()));
        int subtaskId = subtask.getId();

        manager.deleteSubtask(subtaskId);

        assertNull(manager.getSubtaskById(subtaskId));
        assertTrue(manager.getSubtasks().isEmpty());
    }

    @Test
    void shouldGetAllTasks() {
        Task task1 = manager.createTask(createTask("Task 1", "Description 1"));
        Task task2 = manager.createTask(createTask("Task 2", "Description 2"));

        List<Task> tasks = manager.getTasks();

        assertEquals(2, tasks.size());
        assertTrue(tasks.contains(task1));
        assertTrue(tasks.contains(task2));
    }

    @Test
    void shouldGetAllEpics() {
        Epic epic1 = manager.createEpic(createEpic("Epic 1", "Description 1"));
        Epic epic2 = manager.createEpic(createEpic("Epic 2", "Description 2"));

        List<Epic> epics = manager.getEpics();

        assertEquals(2, epics.size());
        assertTrue(epics.contains(epic1));
        assertTrue(epics.contains(epic2));
    }

    @Test
    void shouldGetAllSubtasks() {
        Epic epic = manager.createEpic(createEpic("Test Epic", "Test Description"));
        Subtask subtask1 = manager.createSubtask(createSubtask("Subtask 1", "Description 1", epic.getId()));
        Subtask subtask2 = manager.createSubtask(createSubtask("Subtask 2", "Description 2", epic.getId()));

        List<Subtask> subtasks = manager.getSubtasks();

        assertEquals(2, subtasks.size());
        assertTrue(subtasks.contains(subtask1));
        assertTrue(subtasks.contains(subtask2));
    }

    @Test
    void shouldGetTaskById() {
        Task task = manager.createTask(createTask("Test Task", "Test Description"));
        Task retrievedTask = manager.getTaskById(task.getId());

        assertNotNull(retrievedTask);
        assertEquals(task, retrievedTask);
    }

    @Test
    void shouldGetEpicById() {
        Epic epic = manager.createEpic(createEpic("Test Epic", "Test Description"));
        Epic retrievedEpic = manager.getEpicById(epic.getId());

        assertNotNull(retrievedEpic);
        assertEquals(epic, retrievedEpic);
    }

    @Test
    void shouldGetSubtaskById() {
        Epic epic = manager.createEpic(createEpic("Test Epic", "Test Description"));
        Subtask subtask = manager.createSubtask(createSubtask("Test Subtask", "Test Description", epic.getId()));
        Subtask retrievedSubtask = manager.getSubtaskById(subtask.getId());

        assertNotNull(retrievedSubtask);
        assertEquals(subtask, retrievedSubtask);
    }

    @Test
    void shouldReturnNullForNonExistentTask() {
        assertNull(manager.getTaskById(999));
    }

    @Test
    void shouldReturnNullForNonExistentEpic() {
        assertNull(manager.getEpicById(999));
    }

    @Test
    void shouldReturnNullForNonExistentSubtask() {
        assertNull(manager.getSubtaskById(999));
    }

    @Test
    void shouldGetHistory() {
        Task task1 = manager.createTask(createTask("Task 1", "Description 1"));
        Task task2 = manager.createTask(createTask("Task 2", "Description 2"));

        manager.getTaskById(task1.getId());
        manager.getTaskById(task2.getId());

        List<Task> history = manager.getHistory();
        assertEquals(2, history.size());
        assertTrue(history.contains(task1));
        assertTrue(history.contains(task2));
    }

    @Test
    void shouldGetPrioritizedTasks() {
        Task task1 = manager.createTask(
                createTaskWithTime("Task 1", "Description 1",
                        Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 10, 0))
        );
        Task task2 = manager.createTask(
                createTaskWithTime("Task 2", "Description 2",
                        Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 9, 0))
        );

        List<Task> prioritizedTasks = manager.getPrioritizedTasks();

        assertEquals(2, prioritizedTasks.size());
        assertEquals(task2, prioritizedTasks.get(0)); // 9:00
        assertEquals(task1, prioritizedTasks.get(1)); // 10:00
    }

    @Test
    void shouldCheckTaskOverlapping() {
        Task task1 = manager.createTask(
                createTaskWithTime("Task 1", "Description 1",
                        Duration.ofHours(2), LocalDateTime.of(2024, 1, 1, 10, 0))
        );
        Task task2 = createTaskWithTime("Task 2", "Description 2",
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 11, 0));

        assertTrue(manager.isTasksOverlapping(task1, task2));
        assertTrue(manager.isTasksOverlapping(task2, task1));
    }

    @Test
    void shouldCheckNoTaskOverlapping() {
        Task task1 = manager.createTask(
                createTaskWithTime("Task 1", "Description 1",
                        Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 10, 0))
        );
        Task task2 = createTaskWithTime("Task 2", "Description 2",
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 11, 30));

        assertFalse(manager.isTasksOverlapping(task1, task2));
        assertFalse(manager.isTasksOverlapping(task2, task1));
    }

    @Test
    void shouldCheckHasTaskOverlaps() {
        Task existingTask = manager.createTask(
                createTaskWithTime("Existing Task", "Description",
                        Duration.ofHours(2), LocalDateTime.of(2024, 1, 1, 10, 0))
        );

        Task overlappingTask = createTaskWithTime("Overlapping Task", "Description",
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 11, 0));

        assertTrue(manager.hasTaskOverlaps(overlappingTask));
    }

    @Test
    void shouldCheckNoTaskOverlaps() {
        Task existingTask = manager.createTask(
                createTaskWithTime("Existing Task", "Description",
                        Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 10, 0))
        );

        Task nonOverlappingTask = createTaskWithTime("Non-overlapping Task", "Description",
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 11, 30));

        assertFalse(manager.hasTaskOverlaps(nonOverlappingTask));
    }

    @Test
    void shouldPreventCreatingOverlappingTask() {
        manager.createTask(
                createTaskWithTime("Existing Task", "Description",
                        Duration.ofHours(2), LocalDateTime.of(2024, 1, 1, 10, 0))
        );

        Task overlappingTask = createTaskWithTime("Overlapping Task", "Description",
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 11, 0));

        assertThrows(IllegalArgumentException.class, () -> manager.createTask(overlappingTask));
    }

    @Test
    void shouldPreventCreatingOverlappingSubtask() {
        Epic epic = manager.createEpic(createEpic("Epic", "Description"));

        manager.createSubtask(
                createSubtaskWithTime("Existing Subtask", "Description", epic.getId(),
                        Duration.ofHours(2), LocalDateTime.of(2024, 1, 1, 10, 0))
        );

        Subtask overlappingSubtask = createSubtaskWithTime("Overlapping Subtask", "Description", epic.getId(),
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 11, 0));

        assertThrows(IllegalArgumentException.class, () -> manager.createSubtask(overlappingSubtask));
    }

    @Test
    void shouldDeleteAllTasks() {
        Task task1 = manager.createTask(createTask("Task 1", "Description 1"));
        Task task2 = manager.createTask(createTask("Task 2", "Description 2"));

        manager.deleteAllTasks();

        assertTrue(manager.getTasks().isEmpty());
        assertNull(manager.getTaskById(task1.getId()));
        assertNull(manager.getTaskById(task2.getId()));
    }

    @Test
    void shouldDeleteAllEpics() {
        Epic epic1 = manager.createEpic(createEpic("Epic 1", "Description 1"));
        Epic epic2 = manager.createEpic(createEpic("Epic 2", "Description 2"));

        manager.deleteAllEpics();

        assertTrue(manager.getEpics().isEmpty());
        assertNull(manager.getEpicById(epic1.getId()));
        assertNull(manager.getEpicById(epic2.getId()));
    }

    @Test
    void shouldDeleteAllSubtasks() {
        Epic epic = manager.createEpic(createEpic("Test Epic", "Test Description"));
        Subtask subtask1 = manager.createSubtask(createSubtask("Subtask 1", "Description 1", epic.getId()));
        Subtask subtask2 = manager.createSubtask(createSubtask("Subtask 2", "Description 2", epic.getId()));

        manager.deleteAllSubtasks();

        assertTrue(manager.getSubtasks().isEmpty());
        assertNull(manager.getSubtaskById(subtask1.getId()));
        assertNull(manager.getSubtaskById(subtask2.getId()));
    }
}
