package test.tracker.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.model.Task;
import tracker.utils.CustomLinkedList;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CustomLinkedListTest {
    private CustomLinkedList<Task> list;
    private Task task1;
    private Task task2;
    private Task task3;

    @BeforeEach
    void setUp() {
        list = new CustomLinkedList<>();
        task1 = new Task("Task 1", "Description 1");
        task1.setId(1);
        task2 = new Task("Task 2", "Description 2");
        task2.setId(2);
        task3 = new Task("Task 3", "Description 3");
        task3.setId(3);
    }

    @Test
    void shouldAddTaskToEmptyList() {
        list.linkLast(task1);

        assertEquals(1, list.size());
        assertEquals(List.of(task1), list.getTasks());
        assertFalse(list.isEmpty());
    }

    @Test
    void shouldAddMultipleTasks() {
        list.linkLast(task1);
        list.linkLast(task2);
        list.linkLast(task3);

        assertEquals(3, list.size());
        assertEquals(List.of(task1, task2, task3), list.getTasks());
    }

    @Test
    void shouldRemoveTaskFromMiddle() {
        list.linkLast(task1);
        list.linkLast(task2);
        list.linkLast(task3);

        list.removeNode(task2.getId());

        assertEquals(2, list.size());
        assertEquals(List.of(task1, task3), list.getTasks());
    }

    @Test
    void shouldRemoveTaskFromBeginning() {
        list.linkLast(task1);
        list.linkLast(task2);
        list.linkLast(task3);

        list.removeNode(task1.getId());

        assertEquals(2, list.size());
        assertEquals(List.of(task2, task3), list.getTasks());
    }

    @Test
    void shouldRemoveTaskFromEnd() {
        list.linkLast(task1);
        list.linkLast(task2);
        list.linkLast(task3);

        list.removeNode(task3.getId());

        assertEquals(2, list.size());
        assertEquals(List.of(task1, task2), list.getTasks());
    }

    @Test
    void shouldNotFailWhenRemovingNonExistentTask() {
        list.linkLast(task1);
        list.linkLast(task2);

        list.removeNode(999); // Несуществующий ID

        assertEquals(2, list.size());
        assertEquals(List.of(task1, task2), list.getTasks());
    }

    @Test
    void shouldHandleRemovalFromEmptyList() {
        list.removeNode(task1.getId());

        assertEquals(0, list.size());
        assertTrue(list.isEmpty());
    }

    @Test
    void shouldClearList() {
        list.linkLast(task1);
        list.linkLast(task2);
        list.linkLast(task3);

        list.clear();

        assertEquals(0, list.size());
        assertTrue(list.isEmpty());
        assertEquals(List.of(), list.getTasks());
    }

    @Test
    void shouldMaintainOrderAfterMultipleOperations() {
        // Добавляем задачи
        list.linkLast(task1);
        list.linkLast(task2);
        list.linkLast(task3);

        // Удаляем из середины
        list.removeNode(task2.getId());

        // Добавляем новую задачу
        Task task4 = new Task("Task 4", "Description 4");
        task4.setId(4);
        list.linkLast(task4);

        // Удаляем первую задачу
        list.removeNode(task1.getId());

        // Проверяем итоговый порядок
        assertEquals(2, list.size());
        assertEquals(List.of(task3, task4), list.getTasks());
    }
}