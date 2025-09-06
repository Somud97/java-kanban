package tracker.taskManager;

import tracker.history.HistoryManager;
import tracker.model.Task;
import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.utils.Managers;
import tracker.utils.TaskType;

import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();
    private final HistoryManager inMemoryHistoryManager = Managers.getDefaultHistory();
    private final TreeSet<Task> prioritizedTasks = new TreeSet<>((task1, task2) -> {
        // Сравниваем по startTime, если startTime null, то задача не попадает в список
        if (task1.getStartTime() == null && task2.getStartTime() == null) {
            return Integer.compare(task1.getId(), task2.getId());
        }
        if (task1.getStartTime() == null) {
            return 1; // Задачи без startTime идут в конец
        }
        if (task2.getStartTime() == null) {
            return -1; // Задачи без startTime идут в конец
        }
        int timeComparison = task1.getStartTime().compareTo(task2.getStartTime());
        if (timeComparison != 0) {
            return timeComparison;
        }
        // Если время одинаковое, сортируем по ID
        return Integer.compare(task1.getId(), task2.getId());
    });
    private int idCounter = 1;

    private void validateTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null.");
        }
    }

    private void addToPrioritizedTasks(Task task) {
        // В приоритизированный список попадают только Task и Subtask.
        // Epics исключаем, чтобы их интервалы не конфликтовали с подзадачами.
        if (task.getType().equals(TaskType.EPIC)) {
            return;
        }
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
    }

    private void removeFromPrioritizedTasks(Task task) {
        prioritizedTasks.remove(task);
    }

    @Override
    public boolean isTasksOverlapping(Task task1, Task task2) {
        // Если у любой из задач нет времени начала или продолжительности, пересечений нет
        if (task1.getStartTime() == null || task1.getDuration() == null ||
                task2.getStartTime() == null || task2.getDuration() == null) {
            return false;
        }

        // Если это ровно один и тот же объект, пересечений нет
        if (task1.equals(task2)) {
            return false;
        }
        // Если оба id равны — считаем одну и ту же задачу
        if (task1.getId() == task2.getId()) {
            return false;
        }

        LocalDateTime start1 = task1.getStartTime();
        LocalDateTime end1 = task1.getEndTime();
        LocalDateTime start2 = task2.getStartTime();
        LocalDateTime end2 = task2.getEndTime();

        // Подстрахуемся от null в endTime
        if (end1 == null || end2 == null) {
            return false;
        }

        return !(end1.isBefore(start2) || end1.isEqual(start2) ||
                end2.isBefore(start1) || end2.isEqual(start1));
    }

    @Override
    public boolean hasTaskOverlaps(Task task) {
        if (task.getStartTime() == null || task.getDuration() == null) {
            return false;
        }

        // Используем Stream API для проверки пересечений с приоритизированными задачами
        return prioritizedTasks.stream()
                .anyMatch(existingTask -> isTasksOverlapping(task, existingTask));
    }

    private void validateNoOverlaps(Task task) {
        if (hasTaskOverlaps(task)) {
            throw new IllegalArgumentException(
                    "Задача '" + task.getTitle() + "' пересекается по времени с другой задачей. " +
                            "Время выполнения: " + task.getStartTime() + " - " + task.getEndTime());
        }
    }

    @Override
    public Task createTask(Task task) {
        validateTask(task);
        validateNoOverlaps(task);

        int taskId = idCounter++;
        task.setId(taskId);
        tasks.put(taskId, task);
        addToPrioritizedTasks(task);

        return task;
    }

    @Override
    public Epic createEpic(Epic epic) {
        validateTask(epic);

        int epicId = idCounter++;
        epic.setId(epicId);
        epics.put(epicId, epic);

        return epic;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        validateTask(subtask);
        validateNoOverlaps(subtask);

        System.out.println(subtask.getEpicId());

        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) {
            throw new IllegalArgumentException("You need epicId to create a subtask.");
        }

        int subtaskId = idCounter++;
        subtask.setId(subtaskId);
        subtasks.put(subtaskId, subtask);

        epic.addSubtask(subtask);
        addToPrioritizedTasks(subtask);

        return subtask;
    }

    @Override
    public void updateTask(Task task) {
        validateTask(task);

        if (!tasks.containsKey(task.getId())) {
            throw new IllegalArgumentException("Task with ID " + task.getId() + " does not exist.");
        }

        Task oldTask = tasks.get(task.getId());
        removeFromPrioritizedTasks(oldTask);

        if (task.getStartTime() != null && task.getDuration() != null) {
            boolean hasOverlaps = prioritizedTasks.stream()
                    .anyMatch(existingTask ->
                            existingTask.getId() != task.getId() &&
                                    isTasksOverlapping(task, existingTask));

            if (hasOverlaps) {
                addToPrioritizedTasks(oldTask); // Восстанавливаем старую задачу
                throw new IllegalArgumentException(
                        "Задача '" + task.getTitle() + "' пересекается по времени с другой задачей. " +
                                "Время выполнения: " + task.getStartTime() + " - " + task.getEndTime());
            }
        }

        tasks.put(task.getId(), task);
        addToPrioritizedTasks(task);
    }

    @Override
    public void updateEpic(Epic epic) {
        validateTask(epic);

        if (!epics.containsKey(epic.getId())) {
            throw new IllegalArgumentException("Epic with ID " + epic.getId() + " does not exist.");
        }

        epics.put(epic.getId(), epic);
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        validateTask(subtask);

        if (!subtasks.containsKey(subtask.getId())) {
            throw new IllegalArgumentException("Subtask with ID " + subtask.getId() + " does not exist.");
        }

        Subtask oldSubtask = subtasks.get(subtask.getId());
        removeFromPrioritizedTasks(oldSubtask);

        // Проверяем пересечения с другими задачами (исключая саму подзадачу)
        if (subtask.getStartTime() != null && subtask.getDuration() != null) {
            boolean hasOverlaps = prioritizedTasks.stream()
                    .anyMatch(existingTask ->
                            existingTask.getId() != subtask.getId() &&
                                    isTasksOverlapping(subtask, existingTask));

            if (hasOverlaps) {
                addToPrioritizedTasks(oldSubtask); // Восстанавливаем старую подзадачу
                throw new IllegalArgumentException(
                        "Подзадача '" + subtask.getTitle() + "' пересекается по времени с другой задачей. " +
                                "Время выполнения: " + subtask.getStartTime() + " - " + subtask.getEndTime());
            }
        }

        subtasks.put(subtask.getId(), subtask);
        addToPrioritizedTasks(subtask);

        Epic epic = epics.get(subtask.getEpicId());
        epic.updateEpicStatus();
    }

    @Override
    public void deleteTask(int id) {
        Task task = tasks.remove(id);
        if (task != null) {
            removeFromPrioritizedTasks(task);
        }
        inMemoryHistoryManager.remove(id);
    }

    @Override
    public void deleteSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            removeFromPrioritizedTasks(subtask);
            Epic epic = epics.get(subtask.getEpicId());
            epic.removeSubtask(id);
            epic.updateEpicStatus();
        }
        inMemoryHistoryManager.remove(id);
    }

    @Override
    public void deleteEpic(int id) {
        Epic removedEpic = epics.remove(id);
        if (removedEpic != null) {
            removedEpic.getSubtasks().stream()
                    .forEach(subtask -> {
                        subtasks.remove(subtask.getId());
                        removeFromPrioritizedTasks(subtask);
                        inMemoryHistoryManager.remove(subtask.getId());
                    });
        }
        inMemoryHistoryManager.remove(id);
    }

    @Override
    public void deleteAllTasks() {
        tasks.values().stream()
                .forEach(this::removeFromPrioritizedTasks);
        tasks.clear();
        inMemoryHistoryManager.getHistory().stream()
                .filter(task -> task.getClass().equals(Task.class))
                .forEach(task -> inMemoryHistoryManager.remove(task.getId()));
    }

    @Override
    public void deleteAllSubtasks() {
        subtasks.values().stream()
                .forEach(this::removeFromPrioritizedTasks);
        subtasks.clear();

        epics.values().stream()
                .forEach(Epic::removeAllSubtasks);

        inMemoryHistoryManager.getHistory().stream()
                .filter(task -> task.getClass().equals(Subtask.class))
                .forEach(task -> inMemoryHistoryManager.remove(task.getId()));
    }

    @Override
    public void deleteAllEpics() {
        subtasks.values().stream()
                .forEach(this::removeFromPrioritizedTasks);
        epics.clear();
        subtasks.clear();

        inMemoryHistoryManager.getHistory().stream()
                .filter(task -> task.getClass().equals(Subtask.class) || task.getClass().equals(Epic.class))
                .forEach(task -> inMemoryHistoryManager.remove(task.getId()));
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
            return null;
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
    public List<Task> getHistory() {
        return inMemoryHistoryManager.getHistory();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    @Override
    public List<Subtask> getSubtasksByEpic(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            throw new IllegalArgumentException("Epic with ID " + epicId + " does not exist.");
        }
        return epic.getSubtasks();
    }
}
