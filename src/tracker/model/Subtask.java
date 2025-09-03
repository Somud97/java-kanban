package tracker.model;

import tracker.utils.TaskType;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {
    private final int epicId;

    public Subtask(String title, String description, int epicId,  Duration duration, LocalDateTime startTime) {
        super(title, description);
        if (id == epicId) {
            throw new IllegalArgumentException("ID Epic и Subtask должны отличаться");
        }
        this.epicId = epicId;
        this.duration = duration;
        this.startTime = startTime;
    }

    @Override
    public TaskType getType() {
        return TaskType.SUBTASK;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "epicId=" + epicId +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", id=" + id +
                ", duration=" + duration +
                ", startTime=" + startTime +
                ", endTime=" + getEndTime() +
                '}';
    }
}
