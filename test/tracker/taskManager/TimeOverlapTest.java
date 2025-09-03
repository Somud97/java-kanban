package test.tracker.taskManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;
import tracker.taskManager.InMemoryTaskManager;
import tracker.taskManager.TaskManager;
import tracker.utils.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TimeOverlapTest {
    private TaskManager manager;

    @BeforeEach
    void setUp() {
        manager = new InMemoryTaskManager();
    }

    @Test
    void shouldDetectFullOverlap() {
        Task task1 = new Task("Task 1", "Description",
                Duration.ofHours(2), LocalDateTime.of(2024, 1, 1, 10, 0));
        task1.setId(1);
        Task task2 = new Task("Task 2", "Description",
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 10, 30));
        task2.setId(2);

        // task1: 10:00-12:00, task2: 10:30-11:30 (полное пересечение)
        assertTrue(manager.isTasksOverlapping(task1, task2));
        assertTrue(manager.isTasksOverlapping(task2, task1));
    }

    @Test
    void shouldDetectPartialOverlap() {
        Task task1 = new Task("Task 1", "Description",
                Duration.ofHours(2), LocalDateTime.of(2024, 1, 1, 10, 0));
        task1.setId(1);
        Task task2 = new Task("Task 2", "Description",
                Duration.ofHours(2), LocalDateTime.of(2024, 1, 1, 11, 0));
        task2.setId(2);

        // task1: 10:00-12:00, task2: 11:00-13:00 (частичное пересечение)
        assertTrue(manager.isTasksOverlapping(task1, task2));
        assertTrue(manager.isTasksOverlapping(task2, task1));
    }

    @Test
    void shouldDetectNoOverlap() {
        Task task1 = new Task("Task 1", "Description",
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 10, 0));
        task1.setId(1);
        Task task2 = new Task("Task 2", "Description",
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 11, 30));
        task2.setId(2);

        // task1: 10:00-11:00, task2: 11:30-12:30 (нет пересечения)
        assertFalse(manager.isTasksOverlapping(task1, task2));
        assertFalse(manager.isTasksOverlapping(task2, task1));
    }

    @Test
    void shouldDetectTouchingTasks() {
        Task task1 = new Task("Task 1", "Description",
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 10, 0));
        task1.setId(1);
        Task task2 = new Task("Task 2", "Description",
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 11, 0));
        task2.setId(2);

        // task1: 10:00-11:00, task2: 11:00-12:00 (касаются в точке)
        assertFalse(manager.isTasksOverlapping(task1, task2));
        assertFalse(manager.isTasksOverlapping(task2, task1));
    }

    @Test
    void shouldHandleTasksWithoutTime() {
        Task task1 = new Task("Task 1", "Description");
        task1.setId(1);
        Task task2 = new Task("Task 2", "Description");
        task2.setId(2);

        // Задачи без времени не пересекаются
        assertFalse(manager.isTasksOverlapping(task1, task2));
    }

    @Test
    void shouldHandleTaskWithoutDuration() {
        Task task1 = new Task("Task 1", "Description",
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 10, 0));
        task1.setId(1);
        Task task2 = new Task("Task 2", "Description");
        task2.setId(2);
        task2.setStartTime(LocalDateTime.of(2024, 1, 1, 11, 0));

        // Задача без продолжительности не пересекается
        assertFalse(manager.isTasksOverlapping(task1, task2));
        assertFalse(manager.isTasksOverlapping(task2, task1));
    }

    @Test
    void shouldHandleTaskWithoutStartTime() {
        Task task1 = new Task("Task 1", "Description",
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 10, 0));
        task1.setId(1);
        Task task2 = new Task("Task 2", "Description");
        task2.setId(2);
        task2.setDuration(Duration.ofHours(1));

        // Задача без времени начала не пересекается
        assertFalse(manager.isTasksOverlapping(task1, task2));
        assertFalse(manager.isTasksOverlapping(task2, task1));
    }

    @Test
    void shouldHandleSameTask() {
        Task task = new Task("Task", "Description",
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 10, 0));
        task.setId(1);

        // Задача не пересекается сама с собой
        assertFalse(manager.isTasksOverlapping(task, task));
    }

    @Test
    void shouldHandleTasksWithSameStartTime() {
        Task task1 = new Task("Task 1", "Description",
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 10, 0));
        task1.setId(1);
        Task task2 = new Task("Task 2", "Description",
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 10, 0));
        task2.setId(2);

        // Задачи с одинаковым временем начала пересекаются
        assertTrue(manager.isTasksOverlapping(task1, task2));
        assertTrue(manager.isTasksOverlapping(task2, task1));
    }

    @Test
    void shouldHandleTasksWithSameEndTime() {
        Task task1 = new Task("Task 1", "Description",
                Duration.ofHours(2), LocalDateTime.of(2024, 1, 1, 10, 0));
        task1.setId(1);
        Task task2 = new Task("Task 2", "Description",
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 11, 0));
        task2.setId(2);

        // task1: 10:00-12:00, task2: 11:00-12:00 (одинаковое время окончания)
        assertTrue(manager.isTasksOverlapping(task1, task2));
        assertTrue(manager.isTasksOverlapping(task2, task1));
    }

    @Test
    void shouldHandleVeryShortTasks() {
        Task task1 = new Task("Task 1", "Description",
                Duration.ofMinutes(5), LocalDateTime.of(2024, 1, 1, 10, 0));
        task1.setId(1);
        Task task2 = new Task("Task 2", "Description",
                Duration.ofMinutes(5), LocalDateTime.of(2024, 1, 1, 10, 2));
        task2.setId(2);

        // task1: 10:00-10:05, task2: 10:02-10:07 (пересекаются)
        assertTrue(manager.isTasksOverlapping(task1, task2));
        assertTrue(manager.isTasksOverlapping(task2, task1));
    }

    @Test
    void shouldHandleVeryLongTasks() {
        Task task1 = new Task("Task 1", "Description",
                Duration.ofDays(1), LocalDateTime.of(2024, 1, 1, 10, 0));
        task1.setId(1);
        Task task2 = new Task("Task 2", "Description",
                Duration.ofHours(2), LocalDateTime.of(2024, 1, 1, 15, 0));
        task2.setId(2);

        // task1: 10:00-10:00 (следующий день), task2: 15:00-17:00 (пересекаются)
        assertTrue(manager.isTasksOverlapping(task1, task2));
        assertTrue(manager.isTasksOverlapping(task2, task1));
    }

    @Test
    void shouldHandleEdgeCaseWithZeroDuration() {
        Task task1 = new Task("Task 1", "Description",
                Duration.ZERO, LocalDateTime.of(2024, 1, 1, 10, 0));
        task1.setId(1);
        Task task2 = new Task("Task 2", "Description",
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 10, 0));
        task2.setId(2);

        // Задача с нулевой продолжительностью не пересекается
        assertFalse(manager.isTasksOverlapping(task1, task2));
        assertFalse(manager.isTasksOverlapping(task2, task1));
    }

    @Test
    void shouldHandleSubtasksOverlap() {
        Epic epic = new Epic("Epic", "Description");
        epic.setId(1);
        Subtask subtask1 = new Subtask("Subtask 1", "Description", epic.getId(),
                Duration.ofHours(2), LocalDateTime.of(2024, 1, 1, 10, 0));
        subtask1.setId(2);
        Subtask subtask2 = new Subtask("Subtask 2", "Description", epic.getId(),
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 11, 0));
        subtask2.setId(3);

        // subtask1: 10:00-12:00, subtask2: 11:00-12:00 (пересекаются)
        assertTrue(manager.isTasksOverlapping(subtask1, subtask2));
        assertTrue(manager.isTasksOverlapping(subtask2, subtask1));
    }

    @Test
    void shouldHandleTaskAndSubtaskOverlap() {
        Task task = new Task("Task", "Description",
                Duration.ofHours(2), LocalDateTime.of(2024, 1, 1, 10, 0));
        task.setId(1);
        Epic epic = new Epic("Epic", "Description");
        epic.setId(2);
        Subtask subtask = new Subtask("Subtask", "Description", epic.getId(),
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 11, 0));
        subtask.setId(3);

        // task: 10:00-12:00, subtask: 11:00-12:00 (пересекаются)
        assertTrue(manager.isTasksOverlapping(task, subtask));
        assertTrue(manager.isTasksOverlapping(subtask, task));
    }

    @Test
    void shouldPreventCreatingOverlappingTasks() {
        Task existingTask = new Task("Existing Task", "Description",
                Duration.ofHours(2), LocalDateTime.of(2024, 1, 1, 10, 0));
        existingTask.setId(1);
        manager.createTask(existingTask);

        Task overlappingTask = new Task("Overlapping Task", "Description",
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 11, 0));
        overlappingTask.setId(2);

        assertThrows(IllegalArgumentException.class, () -> {
            manager.createTask(overlappingTask);
        });
    }

    @Test
    void shouldPreventCreatingOverlappingSubtasks() {
        Epic epic = new Epic("Epic", "Description");
        epic.setId(1);
        manager.createEpic(epic);

        Subtask existingSubtask = new Subtask("Existing Subtask", "Description", epic.getId(),
                Duration.ofHours(2), LocalDateTime.of(2024, 1, 1, 10, 0));
        existingSubtask.setId(2);
        manager.createSubtask(existingSubtask);

        Subtask overlappingSubtask = new Subtask("Overlapping Subtask", "Description", epic.getId(),
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 11, 0));
        overlappingSubtask.setId(3);

        assertThrows(IllegalArgumentException.class, () -> {
            manager.createSubtask(overlappingSubtask);
        });
    }

    @Test
    void shouldAllowCreatingNonOverlappingTasks() {
        Task existingTask = new Task("Existing Task", "Description",
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 10, 0));
        existingTask.setId(1);
        manager.createTask(existingTask);

        Task nonOverlappingTask = new Task("Non-overlapping Task", "Description",
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 11, 30));
        nonOverlappingTask.setId(2);

        assertDoesNotThrow(() -> {
            manager.createTask(nonOverlappingTask);
        });
    }

    @Test
    void shouldPreventUpdatingTaskWithOverlap() {
        Task task1 = new Task("Task 1", "Description",
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 10, 0));
        task1.setId(1);
        manager.createTask(task1);

        Task task2 = new Task("Task 2", "Description",
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 12, 0));
        task2.setId(2);
        manager.createTask(task2);

        // Пытаемся обновить task2 так, чтобы он пересекался с task1
        task2.setStartTime(LocalDateTime.of(2024, 1, 1, 10, 30));

        assertThrows(IllegalArgumentException.class, () -> {
            manager.updateTask(task2);
        });
    }

    @Test
    void shouldAllowUpdatingTaskWithoutOverlap() {
        Task task1 = new Task("Task 1", "Description",
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 10, 0));
        task1.setId(1);
        manager.createTask(task1);

        Task task2 = new Task("Task 2", "Description",
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 12, 0));
        task2.setId(2);
        manager.createTask(task2);

        // Обновляем task2 без пересечений
        task2.setStartTime(LocalDateTime.of(2024, 1, 1, 11, 30));

        assertDoesNotThrow(() -> {
            manager.updateTask(task2);
        });
    }

    @Test
    void shouldHandleMultipleOverlaps() {
        Task task1 = new Task("Task 1", "Description",
                Duration.ofHours(2), LocalDateTime.of(2024, 1, 1, 10, 0));
        task1.setId(1);
        manager.createTask(task1);

        Task task2 = new Task("Task 2", "Description",
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 13, 0));
        task2.setId(2);
        manager.createTask(task2);

        Task overlappingTask = new Task("Overlapping Task", "Description",
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 11, 0));
        overlappingTask.setId(3);

        // Новая задача пересекается с первой существующей
        assertTrue(manager.hasTaskOverlaps(overlappingTask));
    }

    @Test
    void shouldHandleNoOverlapsWithMultipleTasks() {
        Task task1 = new Task("Task 1", "Description",
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 10, 0));
        task1.setId(1);
        manager.createTask(task1);

        Task task2 = new Task("Task 2", "Description",
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 12, 0));
        task2.setId(2);
        manager.createTask(task2);

        Task nonOverlappingTask = new Task("Non-overlapping Task", "Description",
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 13, 30));
        nonOverlappingTask.setId(3);

        // Новая задача не пересекается ни с одной существующей
        assertFalse(manager.hasTaskOverlaps(nonOverlappingTask));
    }
}