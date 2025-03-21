package tracker.taskManager;

import java.util.Deque;
import java.util.List;

import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;
import tracker.utils.TaskStatus;

public interface TaskManager {

    Task createTask(String title, String description);

    Epic createEpic(String title, String description);

    Subtask createSubtask(String title, String description, int epicId);

    void updateTask(Task task);

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

    Deque<Task> getHistory();
}
