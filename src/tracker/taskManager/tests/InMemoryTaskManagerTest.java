package tracker.taskManager.tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;
import tracker.taskManager.InMemoryTaskManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    private InMemoryTaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = new InMemoryTaskManager();
    }

    @Test
    void shouldCreateTask() {
        Task task = taskManager.createTask("Test Task", "Description");

        assertTrue(taskManager.getTasks().contains(task), "Task list should contain the created task");
    }

    @Test
    void shouldReturnTaskById() {
        Task task = taskManager.createTask("Test Task", "Description");

        Task result = taskManager.getTaskById(task.getId());

        assertNotNull(result, "Subtask should not be null");
        assertEquals(task, result, "Should return the correct task by ID");
    }

    @Test
    void shouldReturnSubtaskById() {
        Epic epic = taskManager.createEpic("Epic 1", "Description");
        Subtask subtask = taskManager.createSubtask("Subtask 1", "Description", epic.getId());

        Subtask result = taskManager.getSubtaskById(subtask.getId());

        assertNotNull(result, "Subtask should not be null");
        assertEquals(subtask, result, "Should return the correct subtask by ID");
    }

    @Test
    void shouldReturnEpicById() {
        Epic epic = taskManager.createEpic("Epic 1", "Description");

        Epic result = taskManager.getEpicById(epic.getId());

        assertNotNull(result, "Subtask should not be null");
        assertEquals(epic, result, "Should return the correct epic by ID");
    }


    @Test
    void shouldUpdateTask() {
        Task task = taskManager.createTask("Test Task", "Description");
        Task updatedTask = new Task("Updated Task", "Updated Description", task.getId());

        taskManager.updateTask(updatedTask);

        Task result = taskManager.getTaskById(task.getId());
        assertEquals("Updated Task", result.getTitle(), "Task title should be updated");
        assertEquals("Updated Description", result.getDescription(), "Task description should be updated");
    }

    @Test
    void shouldDeleteTask() {
        Task task = taskManager.createTask("Test Task", "Description");

        taskManager.deleteTask(task.getId());

        assertNull(taskManager.getTaskById(task.getId()), "Task should be null after deletion");
        assertFalse(taskManager.getTasks().contains(task), "Task list should not contain the deleted task");
    }

    @Test
    void shouldDeleteAllTasks() {
        taskManager.createTask("Task 1", "Description 1");
        taskManager.createTask("Task 2", "Description 2");

        taskManager.deleteAllTasks();

        assertTrue(taskManager.getTasks().isEmpty(), "All tasks should be removed");
    }

    @Test
    void shouldCreateEpic() {
        var epic = taskManager.createEpic("Test Epic", "Epic Description");

        assertNotNull(epic, "Epic should not be null after creation");
        assertEquals("Test Epic", epic.getTitle(), "Epic title should match");
        assertTrue(taskManager.getEpics().contains(epic), "Epic list should contain the created epic");
    }

    @Test
    void shouldCreateSubtaskLinkedToEpic() {
        var epic = taskManager.createEpic("Test Epic", "Epic Description");

        var subtask = taskManager.createSubtask("Subtask 1", "Description", epic.getId());

        assertNotNull(subtask, "Subtask should not be null");
        assertTrue(taskManager.getSubtasks().contains(subtask), "Subtask list should contain the created subtask");
        assertEquals(epic.getId(), subtask.getEpicId(), "Subtask should be linked to the correct epic");
    }

    @Test
    void shouldDeleteEpicAndSubtasks() {
        var epic = taskManager.createEpic("Test Epic", "Epic Description");
        var subtask = taskManager.createSubtask("Subtask 1", "Description", epic.getId());

        taskManager.deleteEpic(epic.getId());

        assertNull(taskManager.getEpicById(epic.getId()), "Epic should be null after deletion");
        assertTrue(taskManager.getSubtasks().isEmpty(), "All subtasks related to the epic should be removed");
    }

    @Test
    void shouldReturnHistory() {
        Task task1 = taskManager.createTask("Task 1", "Description 1");
        Task task2 = taskManager.createTask("Task 2", "Description 2");
        
        taskManager.getTaskById(task1.getId());
        taskManager.getTaskById(task2.getId());

        var history = taskManager.getHistory();
        assertEquals(2, history.size(), "History should contain 2 tasks");
        assertTrue(history.contains(task1), "History should contain Task 1");
        assertTrue(history.contains(task2), "History should contain Task 2");
    }
}