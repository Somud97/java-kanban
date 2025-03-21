package tracker.model.tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.utils.TaskStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EpicTest {
    private Epic epic;
    private Subtask subtask1;
    private Subtask subtask2;

    @BeforeEach
    void setUp() {
        epic = new Epic("Epic 1", "Description 1", 1);
        subtask1 = new Subtask("Subtask 1", "Description 1", 2, epic.getId());
        subtask2 = new Subtask("Subtask 2", "Description 2", 3, epic.getId());
    }

    @Test
    void shouldReturnNewStatusWhenNoSubtasks() {
        epic.updateEpicStatus();

        assertEquals(TaskStatus.NEW, epic.getStatus());
    }

    @Test
    void shouldReturnNewStatusWhenAllSubtasksAreNew() {
        epic.addSubtask(subtask1);
        epic.addSubtask(subtask2);

        assertEquals(TaskStatus.NEW, epic.getStatus());
    }

    @Test
    void shouldReturnDoneStatusWhenAllSubtasksAreDone() {
        epic.addSubtask(subtask1);
        epic.addSubtask(subtask2);

        subtask1.setStatus(TaskStatus.DONE);
        subtask2.setStatus(TaskStatus.DONE);

        epic.updateEpicStatus();

        assertEquals(TaskStatus.DONE, epic.getStatus());
    }

    @Test
    void shouldReturnInProgressStatusWhenSubtasksHaveDifferentStatuses() {
        epic.addSubtask(subtask1);
        epic.addSubtask(subtask2);

        subtask2.setStatus(TaskStatus.DONE);

        epic.updateEpicStatus();

        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus());
    }

    @Test
    void shouldUpdateStatusAfterSubtaskRemoval() {
        epic.addSubtask(subtask1);
        epic.addSubtask(subtask2);

        subtask1.setStatus(TaskStatus.DONE);

        epic.removeSubtask(subtask1.getId());
        epic.updateEpicStatus();

        assertEquals(TaskStatus.NEW, epic.getStatus());
    }

    @Test
    void shouldReturnNewStatusAfterAllSubtasksAreRemoved() {
        epic.addSubtask(subtask1);
        epic.addSubtask(subtask2);

        subtask1.setStatus(TaskStatus.DONE);
        subtask2.setStatus(TaskStatus.DONE);

        epic.removeAllSubtasks();
        epic.updateEpicStatus();

        assertEquals(TaskStatus.NEW, epic.getStatus());
    }

    @Test
    void epicsShouldBeEqualIfIdsAreEqual() {
        Epic epic2 = new Epic("Epic 1", "Description 1", 1);

        assertEquals(epic, epic2, "Объекты Task с одинаковыми id должны быть равны");
    }


    @Test
    void epicsShouldNotBeEqualIfIdsAreDifferent() {
        Epic epic2 = new Epic("Epic 1", "Description 1", 2);

        assertNotEquals(epic, epic2, "Объекты Task с разными id не должны быть равны");
    }

    @Test
    void shouldNotAllowAddingEpicAsSubtaskToItself() {
        assertThrows(IllegalArgumentException.class, () -> {
            epic.addSubtask(new Subtask("Epic 1", "Description 1", epic.getId(), epic.getId()));
        }, "ID Epic и Subtask должны отличаться");
    }
}