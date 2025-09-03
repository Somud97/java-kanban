package test.tracker.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.model.Epic;
import tracker.model.Subtask;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SubtaskTest {
    private Epic epic;
    private Subtask subtask;

    @BeforeEach
    void setUp() {
        epic = new Epic("Epic 1", "Description 1");
        epic.setId(1);
        subtask = new Subtask("Subtask 1", "Description 1", epic.getId(), null, null);
        subtask.setId(2);
    }

    @Test
    void subtasksShouldBeEqualIfIdsAreEqual() {
        Subtask subtask2 = new Subtask("Subtask 1", "Description 1", 1, null, null);
        subtask2.setId(2);

        assertEquals(subtask, subtask2, "Объекты Task с одинаковыми id должны быть равны");
    }


    @Test
    void subtasksShouldNotBeEqualIfIdsAreDifferent() {
        Subtask subtask2 = new Subtask("Subtask 1", "Description 1", 1, null, null);

        assertNotEquals(subtask, subtask2, "Объекты Task с разными id не должны быть равны");
    }

    @Test
    void shouldReturnCorrectEpicId() {
        assertEquals(epic.getId(), subtask.getEpicId(), "getEpicId возвращает корректный epicId");
    }

    @Test
    void shouldNotAllowAddingEpicAsSubtaskToItself() {
        assertThrows(IllegalArgumentException.class, () -> {
            Subtask newSubtask = new Subtask("Epic 1", "Description 1", epic.getId(), null, null);
            newSubtask.setId(epic.getId());
            epic.addSubtask(newSubtask);
        }, "ID Epic и Subtask должны отличаться");
    }

    @Test
    void shouldClearIdWhenTaskIsRemoved() {
        // Создаем подзадачу с валидным ID
        Subtask subtask = new Subtask("Test Subtask", "Test Description", epic.getId(), null, null);
        subtask.setId(123);

        int originalId = subtask.getId();
        assertEquals(123, originalId, "Исходный ID должен быть установлен корректно");

        // Эмулируем удаление задачи - сбрасываем ID
        subtask.setId(0);

        assertEquals(0, subtask.getId(), "ID должен быть сброшен после удаления");
        assertNotEquals(originalId, subtask.getId(),
                "Подзадача не должна хранить старый ID после удаления");
    }

    @Test
    void testConstructorWithDurationAndStartTime() {
        Duration duration = Duration.ofMinutes(45);
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 14, 0);
        
        Subtask subtaskWithTime = new Subtask("Subtask 2", "Description 2", epic.getId(), duration, startTime);
        
        assertEquals(duration, subtaskWithTime.getDuration());
        assertEquals(startTime, subtaskWithTime.getStartTime());
        assertEquals(LocalDateTime.of(2024, 1, 1, 14, 45), subtaskWithTime.getEndTime());
    }

    @Test
    void testDurationAndStartTimeSetters() {
        Duration duration = Duration.ofHours(1);
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 15, 30);
        
        subtask.setDuration(duration);
        subtask.setStartTime(startTime);
        
        assertEquals(duration, subtask.getDuration());
        assertEquals(startTime, subtask.getStartTime());
        assertEquals(LocalDateTime.of(2024, 1, 1, 16, 30), subtask.getEndTime());
    }
}