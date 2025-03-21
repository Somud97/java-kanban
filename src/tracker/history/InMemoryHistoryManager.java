package tracker.history;

import java.util.ArrayDeque;
import java.util.Deque;

import tracker.model.Task;

public class InMemoryHistoryManager implements HistoryManager {
    private final Deque<Task> taskHistory = new ArrayDeque<>(10);
    private static final int MAX_HISTORY_SIZE = 10;


    @Override
    public void add(Task task) {
        if (taskHistory.size() >= MAX_HISTORY_SIZE) {
            taskHistory.removeFirst();
        }
        taskHistory.addLast(task);
    }

    @Override
    public Deque<Task> getHistory() {
        return new ArrayDeque<>(taskHistory);
    }
}
