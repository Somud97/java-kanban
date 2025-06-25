package tracker.history;

import java.util.List;

import tracker.model.Task;

public interface HistoryManager {
    void add(Task task);

    void remove(int id);

    List<Task> getHistory();
}
