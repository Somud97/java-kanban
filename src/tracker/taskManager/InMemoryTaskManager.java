package tracker.taskManager;

import tracker.history.HistoryManager;
import tracker.model.Task;
import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.utils.Managers;

import java.util.*;


public class InMemoryTaskManager implements TaskManager {
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();
    private final HistoryManager inMemoryHistoryManager = Managers.getDefaultHistory();
    private int idCounter = 1;

    private void validateTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null.");
        }
    }


    @Override
    public Task createTask(String title, String description) {
        int taskId = idCounter++;
        Task task = new Task(title, description, taskId);
        validateTask(task);
        tasks.put(taskId, task);

        return task;
    }

    @Override
    public Epic createEpic(String title, String description) {
        int epicId = idCounter++;
        Epic epic = new Epic(title, description, epicId);
        validateTask(epic);
        epics.put(epicId, epic);

        return epic;
    }

    @Override
    public Subtask createSubtask(String title, String description, int epicId) {
        int subtaskId = idCounter++;
        Subtask subtask = new Subtask(title, description, subtaskId, epicId);
        validateTask(subtask);
        subtasks.put(subtaskId, subtask);
        epics.get(subtask.getEpicId()).addSubtask(subtask);

        return subtask;
    }

    @Override
    public void updateTask(Task task) {
        validateTask(task);

        if (!tasks.containsKey(task.getId())) {
            throw new IllegalArgumentException("Task with ID " + task.getId() + " does not exist.");
        }

        tasks.put(task.getId(), task);
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        validateTask(subtask);

        if (!subtasks.containsKey(subtask.getId())) {
            throw new IllegalArgumentException("Subtask with ID " + subtask.getId() + " does not exist.");
        }

        subtasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getEpicId());
        epic.updateEpicStatus();
    }


    @Override
    public void deleteTask(int id) {
        tasks.remove(id);
    }

    @Override
    public void deleteSubtask(int id) {
        Subtask subtask = subtasks.remove(id);

        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId());
            epic.removeSubtask(id);
            epic.updateEpicStatus();
        }
    }

    @Override
    public void deleteEpic(int id) {
        Epic removedEpic = epics.remove(id);

        if (removedEpic != null) {
            for (int subtaskId : removedEpic.getSubtasks().keySet()) {
                subtasks.remove(subtaskId);
            }
        }
    }

    @Override
    public void deleteAllTasks() {
        tasks.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        subtasks.clear();

        for (Epic epic : epics.values()) {
            epic.removeAllSubtasks();
        }
    }

    @Override
    public void deleteAllEpics() {
        epics.clear();
        subtasks.clear();
    }

    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);

        if (task == null) {
            return null;
        }

        inMemoryHistoryManager.add(task);
        return task;
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask == null) {
            throw new IllegalArgumentException("Subtask with ID " + id + " not found.");
        }

        inMemoryHistoryManager.add(subtask);
        return subtask;
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);

        if (epic == null) {
            return null;
        }

        inMemoryHistoryManager.add(epic);
        return epic;

    }

    @Override
    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public Deque<Task> getHistory() {
        return inMemoryHistoryManager.getHistory();
    }
}
