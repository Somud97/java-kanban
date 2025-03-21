package tracker.history;

import java.util.Deque;

import tracker.model.Task;

public interface HistoryManager {
    void add(Task task);

    Deque<Task> getHistory();
}
