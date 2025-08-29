package test.tracker.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.model.Task;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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

    @Test
    void testDurationAndStartTime() {
        Duration duration = Duration.ofHours(2);
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        
        task.setDuration(duration);
        task.setStartTime(startTime);
        
        assertEquals(duration, task.getDuration());
        assertEquals(startTime, task.getStartTime());
    }

    @Test
    void testGetEndTime() {
        Duration duration = Duration.ofHours(2);
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime expectedEndTime = LocalDateTime.of(2024, 1, 1, 12, 0);
        
        task.setDuration(duration);
        task.setStartTime(startTime);
        
        assertEquals(expectedEndTime, task.getEndTime());
    }

    @Test
    void testGetEndTimeWithNullValues() {
        assertNull(task.getEndTime());
        
        task.setDuration(Duration.ofHours(1));
        assertNull(task.getEndTime());
        
        task.setDuration(null);
        task.setStartTime(LocalDateTime.now());
        assertNull(task.getEndTime());
    }

    @Test
    void testConstructorWithDurationAndStartTime() {
        Duration duration = Duration.ofMinutes(30);
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 9, 0);
        
        Task taskWithTime = new Task("Task 2", "Description 2", duration, startTime);
        
        assertEquals(duration, taskWithTime.getDuration());
        assertEquals(startTime, taskWithTime.getStartTime());
        assertEquals(LocalDateTime.of(2024, 1, 1, 9, 30), taskWithTime.getEndTime());
    }

    @Test
    void testEqualityWithDurationAndStartTime() {
        Duration duration = Duration.ofHours(1);
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        
        task.setDuration(duration);
        task.setStartTime(startTime);
        
        Task task2 = new Task("Task 1", "Description 1", duration, startTime);
        task2.setId(1);
        
        assertEquals(task, task2);
    }
}
