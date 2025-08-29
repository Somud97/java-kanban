package test.tracker.taskManager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;
import tracker.taskManager.FileBackendTaskManager;
import tracker.utils.TaskStatus;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackendTaskManagerTest extends TaskManagerTest<FileBackendTaskManager> {
    private File tempFile;

    @Override
    protected FileBackendTaskManager createTaskManager() {
        try {
            tempFile = File.createTempFile("test_tasks", ".csv");
            return new FileBackendTaskManager(tempFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create temp file", e);
        }
    }

    @AfterEach
    void tearDown() {
        if (tempFile != null && tempFile.exists()) {
            tempFile.delete();
        }
    }

    @Test
    void shouldSaveAndLoadEmptyTaskManager() {
        manager.save();
        FileBackendTaskManager loadedManager = FileBackendTaskManager.loadFromFile(tempFile);

        assertTrue(loadedManager.getTasks().isEmpty());
        assertTrue(loadedManager.getEpics().isEmpty());
        assertTrue(loadedManager.getSubtasks().isEmpty());
    }

    @Test
    void shouldSaveAndLoadTasksEpicsAndSubtasks() {
        // Создаем задачу
        Task task = manager.createTask(new Task("Task 1", "Description 1"));

        // Создаем Эпик
        Epic epic = manager.createEpic(new Epic("Epic 1", "Description 1"));

        // Создаём сабтаск
        Subtask subtask = manager.createSubtask(new Subtask("Subtask 1", "Description 1", epic.getId(), null, null));

        // Загружаем из файла
        FileBackendTaskManager loadedManager = FileBackendTaskManager.loadFromFile(tempFile);

        // Проверяем
        List<Task> loadedTasks = loadedManager.getTasks();
        assertEquals(1, loadedTasks.size());
        assertEquals(task.getTitle(), loadedTasks.getFirst().getTitle());
        assertEquals(task.getDescription(), loadedTasks.getFirst().getDescription());

        List<Epic> loadedEpics = loadedManager.getEpics();
        assertEquals(1, loadedEpics.size());
        assertEquals(epic.getTitle(), loadedEpics.getFirst().getTitle());
        assertEquals(epic.getDescription(), loadedEpics.getFirst().getDescription());

        List<Subtask> loadedSubtask = loadedManager.getSubtasks();
        assertEquals(1, loadedSubtask.size());
        assertEquals(subtask.getTitle(), loadedSubtask.getFirst().getTitle());
        assertEquals(subtask.getDescription(), loadedSubtask.getFirst().getDescription());
    }

    @Test
    void shouldSaveAndLoadTasksWithDifferentStatuses() {
        Task task = new Task("Task 1", "Description 1");
        manager.createTask(task);
        task.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateTask(task);

        FileBackendTaskManager loadedManager = FileBackendTaskManager.loadFromFile(tempFile);
        Task loadedTask = loadedManager.getTasks().getFirst();

        assertEquals(TaskStatus.IN_PROGRESS, loadedTask.getStatus());
    }

    @Test
    void shouldHandleTaskDeletion() {
        Task task = new Task("Task 1", "Description 1");
        manager.createTask(task);
        manager.deleteTask(task.getId());

        FileBackendTaskManager loadedManager = FileBackendTaskManager.loadFromFile(tempFile);
        assertTrue(loadedManager.getTasks().isEmpty());
    }

    @Test
    void shouldSaveAndLoadTasksWithDurationAndStartTime() {
        Duration duration = Duration.ofHours(2);
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        
        Task task = new Task("Task 1", "Description 1", duration, startTime);
        manager.createTask(task);

        FileBackendTaskManager loadedManager = FileBackendTaskManager.loadFromFile(tempFile);
        Task loadedTask = loadedManager.getTasks().getFirst();

        assertEquals(duration, loadedTask.getDuration());
        assertEquals(startTime, loadedTask.getStartTime());
        assertEquals(LocalDateTime.of(2024, 1, 1, 12, 0), loadedTask.getEndTime());
    }

    @Test
    void shouldSaveAndLoadEpicsWithDurationAndStartTime() {
        Duration duration = Duration.ofHours(5);
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 9, 0);
        
        Epic epic = new Epic("Epic 1", "Description 1", duration, startTime);
        manager.createEpic(epic);

        FileBackendTaskManager loadedManager = FileBackendTaskManager.loadFromFile(tempFile);
        Epic loadedEpic = loadedManager.getEpics().getFirst();

        assertEquals(duration, loadedEpic.getDuration());
        assertEquals(startTime, loadedEpic.getStartTime());
        assertEquals(LocalDateTime.of(2024, 1, 1, 14, 0), loadedEpic.getEndTime());
    }

    @Test
    void shouldSaveAndLoadSubtasksWithDurationAndStartTime() {
        Duration duration = Duration.ofMinutes(45);
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 14, 0);
        
        Epic epic = manager.createEpic(new Epic("Epic 1", "Description 1"));
        Subtask subtask = new Subtask("Subtask 1", "Description 1", epic.getId(), duration, startTime);
        manager.createSubtask(subtask);

        FileBackendTaskManager loadedManager = FileBackendTaskManager.loadFromFile(tempFile);
        Subtask loadedSubtask = loadedManager.getSubtasks().getFirst();

        assertEquals(duration, loadedSubtask.getDuration());
        assertEquals(startTime, loadedSubtask.getStartTime());
        assertEquals(LocalDateTime.of(2024, 1, 1, 14, 45), loadedSubtask.getEndTime());
    }

    @Test
    void shouldSaveAndLoadTasksWithNullDurationAndStartTime() {
        Task task = new Task("Task 1", "Description 1");
        manager.createTask(task);

        FileBackendTaskManager loadedManager = FileBackendTaskManager.loadFromFile(tempFile);
        Task loadedTask = loadedManager.getTasks().getFirst();

        assertNull(loadedTask.getDuration());
        assertNull(loadedTask.getStartTime());
        assertNull(loadedTask.getEndTime());
    }

    @Test
    void shouldSaveAndLoadEpicWithCalculatedTimeFromSubtasks() {
        Epic epic = manager.createEpic(new Epic("Epic 1", "Description 1"));
        
        Duration duration1 = Duration.ofHours(2);
        Duration duration2 = Duration.ofHours(1);
        LocalDateTime startTime1 = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime startTime2 = LocalDateTime.of(2024, 1, 1, 12, 0);
        
        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", epic.getId(), duration1, startTime1);
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", epic.getId(), duration2, startTime2);
        
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        FileBackendTaskManager loadedManager = FileBackendTaskManager.loadFromFile(tempFile);
        Epic loadedEpic = loadedManager.getEpics().getFirst();

        // Проверяем, что время эпика рассчитывается корректно
        assertEquals(Duration.ofHours(3), loadedEpic.getDuration());
        assertEquals(startTime1, loadedEpic.getStartTime());
        assertEquals(LocalDateTime.of(2024, 1, 1, 13, 0), loadedEpic.getEndTime());
    }

    @Test
    void shouldThrowExceptionWhenFileCannotBeRead() {
        File nonExistentFile = new File("non_existent_file.csv");
        FileBackendTaskManager manager = new FileBackendTaskManager(nonExistentFile);

        assertThrows(Exception.class, () -> {
            FileBackendTaskManager.loadFromFile(nonExistentFile);
        });
    }

    @Test
    void shouldAutoSaveOnCreate() {
        Task task = new Task("Auto Save Task", "Description");
        manager.createTask(task);

        // Проверяем, что файл был сохранен автоматически
        FileBackendTaskManager loadedManager = FileBackendTaskManager.loadFromFile(tempFile);
        assertEquals(1, loadedManager.getTasks().size());
        assertEquals("Auto Save Task", loadedManager.getTasks().getFirst().getTitle());
    }

    @Test
    void shouldAutoSaveOnUpdate() {
        Task original = manager.createTask(new Task("Original Title", "Description"));
        Task updated = new Task("Updated Title", "Description");
        updated.setId(original.getId());
        manager.updateTask(updated);

        FileBackendTaskManager loadedManager = FileBackendTaskManager.loadFromFile(tempFile);
        assertEquals("Updated Title", loadedManager.getTasks().getFirst().getTitle());
    }

    @Test
    void shouldAutoSaveOnDelete() {
        Task task = manager.createTask(new Task("Task to Delete", "Description"));
        manager.deleteTask(task.getId());

        FileBackendTaskManager loadedManager = FileBackendTaskManager.loadFromFile(tempFile);
        assertTrue(loadedManager.getTasks().isEmpty());
    }
}
