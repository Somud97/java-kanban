package tracker.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import tracker.utils.TaskStatus;
import tracker.utils.TaskType;

public class Epic extends Task {
    private final Map<Integer, Subtask> subtasks;
    private LocalDateTime endTime;

    public Epic() {
        super("", "");
        subtasks = new HashMap<>();
    }

    public Epic(String title, String description) {
        super(title, description);
        subtasks = new HashMap<>();
    }

    public Epic(String title, String description, Duration duration, LocalDateTime startTime) {
        super(title, description, duration, startTime);
        subtasks = new HashMap<>();
    }

    public void addSubtask(Subtask subtask) {
        if (subtask.getId() == this.getId()) {
            throw new IllegalArgumentException("ID Epic и Subtask должны отличаться");
        }
        if (subtasks == null) {
            throw new IllegalStateException("Subtasks field is not initialized");
        }
        subtasks.put(subtask.getId(), subtask);
        updateEpicTime();
    }

    public List<Subtask> getSubtasks() {
        if (subtasks == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(subtasks.values());
    }

    public void removeSubtask(int subtaskId) {
        subtasks.remove(subtaskId);
        updateEpicTime();
    }

    public void removeAllSubtasks() {
        subtasks.clear();
        this.updateEpicStatus();
        updateEpicTime();
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    private void updateEpicTime() {
        if (subtasks.isEmpty()) {
            this.duration = null;
            this.startTime = null;
            this.endTime = null;
            return;
        }

        LocalDateTime earliestStart = subtasks.values().stream()
                .map(Subtask::getStartTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        LocalDateTime latestEnd = subtasks.values().stream()
                .map(Subtask::getEndTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        // Суммарная длительность только если есть хотя бы одна ненулевая длительность
        java.util.Optional<Duration> summedDuration = subtasks.values().stream()
                .map(Subtask::getDuration)
                .filter(Objects::nonNull)
                .reduce(Duration::plus);

        this.startTime = earliestStart;
        this.endTime = latestEnd;
        this.duration = summedDuration.orElse(null);
    }

    @Override
    public LocalDateTime getEndTime() {
        // Если endTime ещё не вычислено (например, у эпика нет подзадач),
        // используем поведение базового класса: startTime + duration
        if (endTime != null) {
            return endTime;
        }
        return null;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    private Map<TaskStatus, Integer> countSubtaskStatuses() {
        return this.getSubtasks().stream()
                .collect(Collectors.groupingBy(
                        Subtask::getStatus,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));
    }

    private TaskStatus calculateEpicStatus(int completed, int inProgress) {
        if (completed == this.getSubtasks().size()) {
            return TaskStatus.DONE;
        } else if (inProgress > 0 || completed > 0) {
            return TaskStatus.IN_PROGRESS;
        } else {
            return TaskStatus.NEW;
        }
    }

    public void updateEpicStatus() {
        if (this.getSubtasks().isEmpty()) {
            this.setStatus(TaskStatus.NEW);
            return;
        }

        Map<TaskStatus, Integer> statusCounts = countSubtaskStatuses();

        int completed = statusCounts.getOrDefault(TaskStatus.DONE, 0);
        int inProgress = statusCounts.getOrDefault(TaskStatus.IN_PROGRESS, 0);

        // Определяем и устанавливаем статус эпика
        TaskStatus epicStatus = calculateEpicStatus(completed, inProgress);
        this.setStatus(epicStatus);
    }

    @Override
    public TaskType getType() {
        return TaskType.EPIC;
    }

    @Override
    public String toString() {
        return "Epic{" +
                "subtasks=" + subtasks.values() +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", id=" + id +
                ", duration=" + duration +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}
