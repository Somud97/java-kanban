package tracker.model;

import java.util.HashMap;

import tracker.utils.TaskStatus;

public class Epic extends Task {
    private final HashMap<Integer, Subtask> subtasks = new HashMap<>();

    public Epic(String title, String description) {
        super(title, description);
    }

    public void addSubtask(Subtask subtask) {
        subtasks.put(subtask.getUid(), subtask);
    }

    public HashMap<Integer, Subtask> getSubtasks() {
        return subtasks;
    }

    public void removeSubtask(int subtaskUid) {
        subtasks.remove(subtaskUid);
    }

    public void setEpicStatus() {
        boolean isAllSubtasksNew = subtasks.values()
                .stream()
                .allMatch(subtask -> subtask.getStatus() == TaskStatus.NEW);

        if (subtasks.isEmpty() || isAllSubtasksNew) {
            setStatus(TaskStatus.NEW);
        }

        for (Subtask subtask : subtasks.values()) {
            if (subtask.getStatus() == TaskStatus.IN_PROGRESS) {
                setStatus(TaskStatus.IN_PROGRESS);
                return;
            }
        }

        boolean isAllSubtasksCompleted = subtasks.values()
                .stream()
                .allMatch(subtask -> subtask.getStatus() == TaskStatus.DONE);

        if (isAllSubtasksCompleted) {
            setStatus(TaskStatus.DONE);
        }
    }

    @Override
    public String toString() {
        return "Epic{" +
                "subtasks=" + subtasks.values() +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", uid=" + uid +
                '}';
    }
}
