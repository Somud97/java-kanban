package server.handlers;

import com.sun.net.httpserver.HttpExchange;
import tracker.model.Task;

import java.io.IOException;
import java.util.List;

public class HistoryHandler extends BaseHttpHandler {

    public HistoryHandler(tracker.taskManager.TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    protected void handleRequest(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        if (!method.equals("GET")) {
            sendNotFound(exchange, "Метод не поддерживается");
            return;
        }

        if (path.equals("/history")) {
            handleGetHistory(exchange);
        } else {
            sendNotFound(exchange, "Неверный путь");
        }
    }

    private void handleGetHistory(HttpExchange exchange) throws IOException {
        List<Task> history = taskManager.getHistory();
        String response = gson.toJson(history);
        sendText(exchange, response);
    }
}
