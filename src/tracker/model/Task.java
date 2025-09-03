package tracker.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

import tracker.utils.TaskStatus;
import tracker.utils.TaskType;

public class Task {
    protected String title;
    protected String description;
    protected TaskStatus status;
    protected int id;
    protected Duration duration;
    protected LocalDateTime startTime;

    public Task(String title, String description) {
        this.title = title;
        this.description = description;
        this.status = TaskStatus.NEW;
    }

    public Task(String title, String description, Duration duration, LocalDateTime startTime) {
        this.title = title;
        this.description = description;
        this.status = TaskStatus.NEW;
        this.duration = duration;
        this.startTime = startTime;
    }

    public String getTitle() {
        return title;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public int getId() {
        return this.id;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        if (startTime != null && duration != null) {
            return startTime.plus(duration);
        }
        return null;
    }

    public TaskType getType() {
        return TaskType.TASK;
    }

    @Override
    public String toString() {
        return "Task{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", id=" + id +
                ", status=" + status +
                ", duration=" + duration +
                ", startTime=" + startTime +
                ", endTime=" + getEndTime() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        Task task = (Task) o;
        return id == task.id && 
               Objects.equals(title, task.title) && 
               Objects.equals(description, task.description) && 
               status == task.status &&
               Objects.equals(duration, task.duration) &&
               Objects.equals(startTime, task.startTime);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(title);
        result = 31 * result + Objects.hashCode(description);
        result = 31 * result + id;
        result = 31 * result + Objects.hashCode(status);
        result = 31 * result + Objects.hashCode(duration);
        result = 31 * result + Objects.hashCode(startTime);
        return result;
    }
}