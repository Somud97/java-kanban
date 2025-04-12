package tracker.history;

import java.util.List;

import tracker.model.Task;
import tracker.utils.CustomLinkedList;

public class InMemoryHistoryManager implements HistoryManager {
    private final CustomLinkedList<Task> history = new CustomLinkedList<>();

    @Override
    public void add(Task task) {
        if (task == null) return;

        history.removeNode(task.getId());

        history.linkLast(task);
    }

    @Override
    public List<Task> getHistory() {
        return history.getTasks();
    }

    @Override
    public void remove(int id) {
        history.removeNode(id);
    }
}
