package tracker.controllers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;
import tracker.utils.TaskStatus;

public class TaskManager {
    private HashMap<Integer, Task> tasks = new HashMap<>();
    private HashMap<Integer, Epic> epics = new HashMap<>();
    private HashMap<Integer, Subtask> subtasks = new HashMap<>();
    private int uidCounter = 1;

    private void updateTask(Task task) {
        tasks.put(task.getUid(), task);
    }

    private void updateSubtask(Subtask subtask) {
        subtasks.put(subtask.getUid(), subtask);
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.setEpicStatus();
        }
    }

    public Task createTask(Task task) {
        int uid = uidCounter++;
        task.setId(uid);
        tasks.put(uid, task);
        return task;
    }

    public Epic createEpic(Epic epic) {
        int uid = uidCounter++;
        epic.setId(uid);
        epics.put(uid, epic);
        return epic;
    }

    public Subtask createSubtask(Subtask subtask) {
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            int uid = uidCounter++;
            subtask.setId(uid);
            subtasks.put(uid, subtask);
            epic.addSubtask(subtask);
            epic.setEpicStatus();
            return subtask;
        } else {
            return null;
        }
    }

    public void deleteTask(int uid) {
        tasks.remove(uid);
    }

    public void deleteSubtask(int uid) {
        if (subtasks.isEmpty()) return;

        Epic epic = epics.get(subtasks.get(uid).getEpicId());
        if (epic != null) {
            epic.removeSubtask(uid);
            epic.setEpicStatus();
        }

        subtasks.remove(uid);
    }

    public void deleteEpic(int uid) {
        Epic epic = epics.get(uid);
        epics.remove(uid);
        epic.getSubtasks().clear();
        epic.setEpicStatus();
    }

    public void deleteTasks() {
        tasks.clear();
    }

    public void deleteSubtasks() {
        for (Epic epic : epics.values()) {
            epic.getSubtasks().clear();
            epic.setEpicStatus();
        }
        subtasks.clear();
    }

    public void deleteEpics() {
        epics.clear();
        subtasks.clear();
    }

    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    public List<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }


    public void setSubtaskStatus(Subtask subtask, TaskStatus status) {
        subtask.setStatus(status);
        updateSubtask(subtask);
    }

    public Task getTaskByUid(int uid) {
        return tasks.get(uid);
    }

    public Collection<Subtask> getEpicsSubtasks(Epic epic) {
        return epic.getSubtasks().values();
    }

    public Subtask getSubtaskByUid(int uid) {
        return subtasks.get(uid);
    }

    public Epic getEpicByUid(int uid) {
        return epics.get(uid);
    }
}
