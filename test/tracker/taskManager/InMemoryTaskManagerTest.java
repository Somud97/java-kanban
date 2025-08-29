package test.tracker.taskManager;

import org.junit.jupiter.api.Test;
import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;
import tracker.taskManager.InMemoryTaskManager;
import tracker.taskManager.TaskManager;
import tracker.utils.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @Override
    protected InMemoryTaskManager createTaskManager() {
        return new InMemoryTaskManager();
    }

    @Test
    void shouldHandleTasksWithSameStartTime() {
        LocalDateTime sameTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        
        Task task1 = manager.createTask(
            new Task("Task 1", "Description 1", 
                    Duration.ofHours(1), sameTime)
        );
        Task task2 = manager.createTask(
            new Task("Task 2", "Description 2", 
                    Duration.ZERO, sameTime)
        );

        List<Task> prioritizedTasks = manager.getPrioritizedTasks();

        assertEquals(2, prioritizedTasks.size());
        // При одинаковом времени сортировка по ID
        if (task1.getId() < task2.getId()) {
            assertEquals(task1, prioritizedTasks.get(0));
            assertEquals(task2, prioritizedTasks.get(1));
        } else {
            assertEquals(task2, prioritizedTasks.get(0));
            assertEquals(task1, prioritizedTasks.get(1));
        }
    }

    @Test
    void shouldExcludeTasksWithoutStartTime() {
        // Создаем задачи без startTime
        Task taskWithoutTime = manager.createTask(new Task("Task without time", "Description"));
        Epic epicWithoutTime = manager.createEpic(new Epic("Epic without time", "Description"));
        
        // Создаем задачу с startTime
        Task taskWithTime = manager.createTask(
            new Task("Task with time", "Description", 
                    Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 10, 0))
        );

        List<Task> prioritizedTasks = manager.getPrioritizedTasks();

        assertEquals(1, prioritizedTasks.size());
        assertTrue(prioritizedTasks.contains(taskWithTime));
        assertFalse(prioritizedTasks.contains(taskWithoutTime));
        assertFalse(prioritizedTasks.contains(epicWithoutTime));
    }

    @Test
    void shouldUpdatePrioritizedTasksWhenTaskIsUpdated() {
        Task task = manager.createTask(
            new Task("Task", "Description", 
                    Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 10, 0))
        );
        
        // Обновляем время начала
        task.setStartTime(LocalDateTime.of(2024, 1, 1, 8, 0));
        manager.updateTask(task);

        List<Task> prioritizedTasks = manager.getPrioritizedTasks();
        assertEquals(1, prioritizedTasks.size());
        assertEquals(LocalDateTime.of(2024, 1, 1, 8, 0), prioritizedTasks.get(0).getStartTime());
    }

    @Test
    void shouldRemoveFromPrioritizedTasksWhenTaskIsDeleted() {
        Task task = manager.createTask(
            new Task("Task", "Description", 
                    Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 10, 0))
        );
        
        manager.deleteTask(task.getId());

        List<Task> prioritizedTasks = manager.getPrioritizedTasks();
        assertEquals(0, prioritizedTasks.size());
    }

    @Test
    void shouldHandleEdgeCasesForOverlapping() {
        Task task1 = manager.createTask(
            new Task("Task 1", "Description", 
                    Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 10, 0))
        );

        // Задача, которая заканчивается точно когда начинается task1
        Task task2 = new Task("Task 2", "Description",
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 9, 0));

        // Задача, которая начинается точно когда заканчивается task1
        Task task3 = new Task("Task 3", "Description",
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 11, 0));

        // Задачи не пересекаются (касаются в точках)
        assertFalse(manager.isTasksOverlapping(task1, task2));
        assertFalse(manager.isTasksOverlapping(task1, task3));
    }

    @Test
    void shouldPreventUpdatingTaskWithOverlap() {
        Task task1 = manager.createTask(
            new Task("Task 1", "Description", 
                    Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 10, 0))
        );
        
        Task task2 = manager.createTask(
            new Task("Task 2", "Description", 
                    Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 12, 0))
        );

        // Попытка обновить task2 так, чтобы он пересекался с task1
        task2.setStartTime(LocalDateTime.of(2024, 1, 1, 10, 30));

        assertThrows(IllegalArgumentException.class, () -> {
            manager.updateTask(task2);
        });
    }

    @Test
    void shouldAllowUpdatingTaskWithoutOverlap() {
        Task task1 = manager.createTask(
            new Task("Task 1", "Description", 
                    Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 10, 0))
        );
        
        Task task2 = manager.createTask(
            new Task("Task 2", "Description", 
                    Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 12, 0))
        );

        // Обновляем task2 без пересечений
        task2.setStartTime(LocalDateTime.of(2024, 1, 1, 11, 30));

        assertDoesNotThrow(() -> {
            manager.updateTask(task2);
        });
    }
}