package test.tracker.taskManager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;
import tracker.taskManager.FileBackedTaskManager;
import tracker.utils.TaskStatus;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {
    private File tempFile;
    private FileBackedTaskManager manager;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = File.createTempFile("tasks", ".csv");
        manager = new FileBackedTaskManager(tempFile);
    }

    @AfterEach
    void tearDown() {
        tempFile.delete();
    }

    @Test
    void shouldSaveAndLoadEmptyTaskManager() {
        manager.save();
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

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
        Subtask subtask = manager.createSubtask(new Subtask("Subtask 1", "Description 1", epic.getId()));

        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

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

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        Task loadedTask = loadedManager.getTasks().getFirst();

        assertEquals(TaskStatus.IN_PROGRESS, loadedTask.getStatus());
    }

    @Test
    void shouldHandleTaskDeletion() {
        Task task = new Task("Task 1", "Description 1");
        manager.createTask(task);
        manager.deleteTask(task.getId());

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        assertTrue(loadedManager.getTasks().isEmpty());
    }
}