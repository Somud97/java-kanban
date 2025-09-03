package test.tracker.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.utils.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {
    private Epic epic;

    @BeforeEach
    void setUp() {
        epic = new Epic("Test Epic", "Test Description");
        epic.setId(1);
    }

    @Test
    void shouldCreateEpic() {
        assertEquals("Test Epic", epic.getTitle());
        assertEquals("Test Description", epic.getDescription());
        assertEquals(TaskStatus.NEW, epic.getStatus());
        assertEquals(1, epic.getId());
    }

    @Test
    void shouldCreateEpicWithDurationAndStartTime() {
        Duration duration = Duration.ofHours(5);
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 10, 0);

        Epic epicWithTime = new Epic("Epic with time", "Description", duration, startTime);
        epicWithTime.setId(1);
        Subtask epicSubtask = new Subtask("Epic with time", "Description", epicWithTime.getId(), duration, startTime);
        epicSubtask.setId(2);
        epicWithTime.addSubtask(epicSubtask);

        assertEquals(duration, epicWithTime.getDuration());
        assertEquals(startTime, epicWithTime.getStartTime());
        assertEquals(LocalDateTime.of(2024, 1, 1, 15, 0), epicWithTime.getEndTime());
    }

    @Test
    void shouldAddSubtask() {
        Subtask subtask = new Subtask("Test Subtask", "Description", epic.getId(), null, null);
        subtask.setId(2);
        epic.addSubtask(subtask);

        assertEquals(1, epic.getSubtasks().size());
        assertTrue(epic.getSubtasks().containsValue(subtask));
    }

    @Test
    void shouldRemoveSubtask() {
        Subtask subtask = new Subtask("Test Subtask", "Description", epic.getId(), null, null);
        subtask.setId(2);
        epic.addSubtask(subtask);
        epic.removeSubtask(subtask.getId());

        assertEquals(0, epic.getSubtasks().size());
    }

    @Test
    void shouldRemoveAllSubtasks() {
        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", epic.getId(), null, null);
        subtask1.setId(2);
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", epic.getId(), null, null);
        subtask2.setId(3);

        epic.addSubtask(subtask1);
        epic.addSubtask(subtask2);
        epic.removeAllSubtasks();

        assertEquals(0, epic.getSubtasks().size());
    }

    @Test
    void shouldCalculateEpicTimeFromSubtasks() {
        Duration duration1 = Duration.ofHours(2);
        Duration duration2 = Duration.ofHours(1);
        LocalDateTime startTime1 = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime startTime2 = LocalDateTime.of(2024, 1, 1, 12, 0);

        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", epic.getId(), duration1, startTime1);
        subtask1.setId(2);
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", epic.getId(), duration2, startTime2);
        subtask2.setId(3);

        epic.addSubtask(subtask1);
        epic.addSubtask(subtask2);

        assertEquals(Duration.ofHours(3), epic.getDuration());
        assertEquals(startTime1, epic.getStartTime());
        assertEquals(LocalDateTime.of(2024, 1, 1, 13, 0), epic.getEndTime());
    }

    @Test
    void shouldHandleEpicWithNoSubtasks() {
        epic.removeAllSubtasks();

        assertNull(epic.getDuration());
        assertNull(epic.getStartTime());
        assertNull(epic.getEndTime());
    }

    @Test
    void shouldHandleEpicWithSubtasksWithoutTime() {
        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", epic.getId(), null, null);
        subtask1.setId(2);
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", epic.getId(), null, null);
        subtask2.setId(3);

        epic.addSubtask(subtask1);
        epic.addSubtask(subtask2);

        assertNull(epic.getDuration());
        assertNull(epic.getStartTime());
        assertNull(epic.getEndTime());
    }

    @Test
    void shouldHandleEpicWithMixedTimeSubtasks() {
        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", epic.getId(),
                Duration.ofHours(2), LocalDateTime.of(2024, 1, 1, 10, 0));
        subtask1.setId(2);
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", epic.getId(), null, null);
        subtask2.setId(3);

        epic.addSubtask(subtask1);
        epic.addSubtask(subtask2);

        assertEquals(Duration.ofHours(2), epic.getDuration());
        assertEquals(LocalDateTime.of(2024, 1, 1, 10, 0), epic.getStartTime());
        assertEquals(LocalDateTime.of(2024, 1, 1, 12, 0), epic.getEndTime());
    }

    // Тесты для статусов Epic в граничных условиях

    @Test
    void shouldHaveNewStatusWhenAllSubtasksAreNew() {
        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", epic.getId(), null, null);
        subtask1.setId(2);
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", epic.getId(), null, null);
        subtask2.setId(3);

        epic.addSubtask(subtask1);
        epic.addSubtask(subtask2);
        epic.updateEpicStatus();

        assertEquals(TaskStatus.NEW, epic.getStatus());
    }

    @Test
    void shouldHaveDoneStatusWhenAllSubtasksAreDone() {
        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", epic.getId(), null, null);
        subtask1.setId(2);
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", epic.getId(), null, null);
        subtask2.setId(3);

        subtask1.setStatus(TaskStatus.DONE);
        subtask2.setStatus(TaskStatus.DONE);

        epic.addSubtask(subtask1);
        epic.addSubtask(subtask2);
        epic.updateEpicStatus();

        assertEquals(TaskStatus.DONE, epic.getStatus());
    }

    @Test
    void shouldHaveInProgressStatusWhenSubtasksAreNewAndDone() {
        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", epic.getId(), null, null);
        subtask1.setId(2);
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", epic.getId(), null, null);
        subtask2.setId(3);

        subtask1.setStatus(TaskStatus.NEW);
        subtask2.setStatus(TaskStatus.DONE);

        epic.addSubtask(subtask1);
        epic.addSubtask(subtask2);
        epic.updateEpicStatus();

        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus());
    }

    @Test
    void shouldHaveInProgressStatusWhenSubtasksAreInProgress() {
        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", epic.getId(), null, null);
        subtask1.setId(2);
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", epic.getId(), null, null);
        subtask2.setId(3);

        subtask1.setStatus(TaskStatus.IN_PROGRESS);
        subtask2.setStatus(TaskStatus.NEW);

        epic.addSubtask(subtask1);
        epic.addSubtask(subtask2);
        epic.updateEpicStatus();

        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus());
    }

    @Test
    void shouldHaveInProgressStatusWhenSubtasksAreDoneAndInProgress() {
        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", epic.getId(), null, null);
        subtask1.setId(2);
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", epic.getId(), null, null);
        subtask2.setId(3);

        subtask1.setStatus(TaskStatus.DONE);
        subtask2.setStatus(TaskStatus.IN_PROGRESS);

        epic.addSubtask(subtask1);
        epic.addSubtask(subtask2);
        epic.updateEpicStatus();

        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus());
    }

    @Test
    void shouldHaveNewStatusWhenNoSubtasks() {
        epic.updateEpicStatus();
        assertEquals(TaskStatus.NEW, epic.getStatus());
    }

    @Test
    void shouldUpdateStatusWhenSubtaskStatusChanges() {
        Subtask subtask = new Subtask("Subtask", "Description", epic.getId(), null, null);
        subtask.setId(2);
        epic.addSubtask(subtask);

        // Изначально статус NEW
        epic.updateEpicStatus();
        assertEquals(TaskStatus.NEW, epic.getStatus());

        // Меняем статус подзадачи на IN_PROGRESS
        subtask.setStatus(TaskStatus.IN_PROGRESS);
        epic.updateEpicStatus();
        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus());

        // Меняем статус подзадачи на DONE
        subtask.setStatus(TaskStatus.DONE);
        epic.updateEpicStatus();
        assertEquals(TaskStatus.DONE, epic.getStatus());
    }

    @Test
    void shouldHandleComplexStatusScenarios() {
        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", epic.getId(), null, null);
        subtask1.setId(2);
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", epic.getId(), null, null);
        subtask2.setId(3);
        Subtask subtask3 = new Subtask("Subtask 3", "Description 3", epic.getId(), null, null);
        subtask3.setId(4);

        epic.addSubtask(subtask1);
        epic.addSubtask(subtask2);
        epic.addSubtask(subtask3);

        // Все NEW
        epic.updateEpicStatus();
        assertEquals(TaskStatus.NEW, epic.getStatus());

        // Один IN_PROGRESS, остальные NEW
        subtask1.setStatus(TaskStatus.IN_PROGRESS);
        epic.updateEpicStatus();
        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus());

        // Один DONE, один IN_PROGRESS, один NEW
        subtask2.setStatus(TaskStatus.DONE);
        epic.updateEpicStatus();
        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus());

        // Все DONE
        subtask1.setStatus(TaskStatus.DONE);
        subtask3.setStatus(TaskStatus.DONE);
        epic.updateEpicStatus();
        assertEquals(TaskStatus.DONE, epic.getStatus());
    }

    @Test
    void shouldUpdateEpicTimeWhenSubtaskIsRemoved() {
        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", epic.getId(),
                Duration.ofHours(2), LocalDateTime.of(2024, 1, 1, 10, 0));
        subtask1.setId(2);
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", epic.getId(),
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 12, 0));
        subtask2.setId(3);

        epic.addSubtask(subtask1);
        epic.addSubtask(subtask2);

        assertEquals(Duration.ofHours(3), epic.getDuration());
        assertEquals(LocalDateTime.of(2024, 1, 1, 10, 0), epic.getStartTime());
        assertEquals(LocalDateTime.of(2024, 1, 1, 13, 0), epic.getEndTime());

        // Удаляем одну подзадачу
        epic.removeSubtask(subtask1.getId());

        assertEquals(Duration.ofHours(1), epic.getDuration());
        assertEquals(LocalDateTime.of(2024, 1, 1, 12, 0), epic.getStartTime());
        assertEquals(LocalDateTime.of(2024, 1, 1, 13, 0), epic.getEndTime());
    }

    @Test
    void shouldUpdateEpicTimeWhenAllSubtasksAreRemoved() {
        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", epic.getId(),
                Duration.ofHours(2), LocalDateTime.of(2024, 1, 1, 10, 0));
        subtask1.setId(2);
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", epic.getId(),
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 12, 0));
        subtask2.setId(3);

        epic.addSubtask(subtask1);
        epic.addSubtask(subtask2);

        // Удаляем все подзадачи
        epic.removeAllSubtasks();

        assertNull(epic.getDuration());
        assertNull(epic.getStartTime());
        assertNull(epic.getEndTime());
    }

    @Test
    void shouldThrowExceptionWhenSubtaskIdEqualsEpicId() {
        Subtask subtask = new Subtask("Subtask", "Description", epic.getId(), null, null);
        subtask.setId(epic.getId()); // Устанавливаем тот же ID

        assertThrows(IllegalArgumentException.class, () -> {
            epic.addSubtask(subtask);
        });
    }

    @Test
    void shouldSetEndTime() {
        LocalDateTime endTime = LocalDateTime.of(2024, 1, 1, 15, 0);
        epic.setEndTime(endTime);

        assertEquals(endTime, epic.getEndTime());
    }

    @Test
    void shouldReturnCorrectEndTime() {
        epic.setEndTime(LocalDateTime.of(2024, 1, 1, 15, 0));

        assertEquals(LocalDateTime.of(2024, 1, 1, 15, 0), epic.getEndTime());
    }
}