package tracker.taskManager;

import java.util.List;

import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;

public interface TaskManager {

    Task createTask(Task task);

    Epic createEpic(Epic epic);

    Subtask createSubtask(Subtask subtask);

    void updateTask(Task task);

    void updateEpic(Epic epic);

    void updateSubtask(Subtask subtask);

    void deleteTask(int id);

    void deleteSubtask(int id);

    void deleteEpic(int id);

    void deleteAllTasks();

    void deleteAllSubtasks();

    void deleteAllEpics();

    List<Task> getTasks();

    List<Epic> getEpics();

    List<Subtask> getSubtasks();

    Task getTaskById(int id);

    Subtask getSubtaskById(int id);

    Epic getEpicById(int id);

    List<Task> getHistory();

    List<Task> getPrioritizedTasks();

    List<Subtask> getSubtasksByEpic(int epicId);

    /**
     * Проверяет, пересекаются ли две задачи по времени выполнения
     * @param task1 первая задача
     * @param task2 вторая задача
     * @return true если задачи пересекаются, false если нет
     */
    boolean isTasksOverlapping(Task task1, Task task2);

    /**
     * Проверяет, пересекается ли задача с любой другой задачей в менеджере
     * @param task задача для проверки
     * @return true если есть пересечения, false если нет
     */
    boolean hasTaskOverlaps(Task task);
}
