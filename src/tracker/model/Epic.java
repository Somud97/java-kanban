package tracker.model;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import tracker.utils.TaskStatus;

public class Epic extends Task {
    private final Map<Integer, Subtask> subtasks = new HashMap<>();

    public Epic(String title, String description) {
        super(title, description);
    }

    public void addSubtask(Subtask subtask) {
        if (subtask.getId() == this.getId()) {
            throw new IllegalArgumentException("ID Epic и Subtask должны отличаться");
        }
        subtasks.put(subtask.getId(), subtask);
    }

    public Map<Integer, Subtask> getSubtasks() {
        return new HashMap<>(subtasks);
    }

    public void removeSubtask(int subtaskId) {
        subtasks.remove(subtaskId);
    }

    public void removeAllSubtasks() {
        subtasks.clear();
        this.updateEpicStatus();
    }

    private Map<TaskStatus, Integer> countSubtaskStatuses() {
        Map<TaskStatus, Integer> statusCounts = new EnumMap<>(TaskStatus.class);

        for (int subtaskId : this.getSubtasks().keySet()) {
            Subtask subtask = subtasks.get(subtaskId);

            if (subtask == null) {
                continue;
            }

            TaskStatus status = subtask.getStatus();
            statusCounts.put(status, statusCounts.getOrDefault(status, 0) + 1);
        }

        return statusCounts;
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
    public String toString() {
        return "Epic{" +
                "subtasks=" + subtasks.values() +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", id=" + id +
                '}';
    }
}
