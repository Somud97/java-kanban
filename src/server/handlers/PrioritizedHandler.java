package server.handlers;

import com.sun.net.httpserver.HttpExchange;
import tracker.model.Task;

import java.io.IOException;
import java.util.List;

public class PrioritizedHandler extends BaseHttpHandler {

    public PrioritizedHandler(tracker.taskManager.TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    protected void handleRequest(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        if (!method.equals("GET")) {
            sendMethodNotAllowed(exchange, "Метод не поддерживается");
            return;
        }

        if (path.equals("/prioritized")) {
            handleGetPrioritizedTasks(exchange);
        } else {
            sendNotFound(exchange, "Неверный путь");
        }
    }

    private void handleGetPrioritizedTasks(HttpExchange exchange) throws IOException {
        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
        String response = gson.toJson(prioritizedTasks);
        sendText(exchange, response);
    }
}
