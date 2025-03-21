package tracker.model.tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.model.Epic;
import tracker.model.Subtask;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SubtaskTest {
    private Epic epic;
    private Subtask subtask;

    @BeforeEach
    void setUp() {
        epic = new Epic("Epic 1", "Description 1", 1);
        subtask = new Subtask("Subtask 1", "Description 1", 2, epic.getId());
    }

    @Test
    void subtasksShouldBeEqualIfIdsAreEqual() {
        Subtask subtask2 = new Subtask("Subtask 1", "Description 1", 2, 1);

        assertEquals(subtask, subtask2, "Объекты Task с одинаковыми id должны быть равны");
    }


    @Test
    void subtasksShouldNotBeEqualIfIdsAreDifferent() {
        Subtask subtask2 = new Subtask("Subtask 1", "Description 1", 3, 1);

        assertNotEquals(subtask, subtask2, "Объекты Task с разными id не должны быть равны");
    }

    @Test
    void shouldReturnCorrectEpicId() {
        assertEquals(epic.getId(), subtask.getEpicId(), "getEpicId возвращает корректный epicId");
    }

    @Test
    void shouldNotAllowAddingEpicAsSubtaskToItself() {
        assertThrows(IllegalArgumentException.class, () -> {
            epic.addSubtask(new Subtask("Epic 1", "Description 1", epic.getId(), epic.getId()));
        }, "ID Epic и Subtask должны отличаться");
    }


}