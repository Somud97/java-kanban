package test.tracker.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.model.Task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class TaskTest {
    private Task task;

    @BeforeEach
    void setUp() {
        task = new Task("Task 1", "Description 1");
        task.setId(1);
    }

    @Test
    void tasksShouldBeEqualIfIdsAreEqual() {
        Task task2 = new Task("Task 1", "Description 1");
        task2.setId(1);
        assertEquals(task, task2, "Объекты Task с одинаковыми id должны быть равны");
    }

    @Test
    void tasksShouldNotBeEqualIfIdsAreDifferent() {
        Task task2 = new Task("Task 1", "Description 1");
        task2.setId(2);
        assertNotEquals(task, task2, "Объекты Task с разными id не должны быть равны");
    }
}
