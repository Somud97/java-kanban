package server.handlers;

import com.sun.net.httpserver.HttpExchange;
import tracker.model.Subtask;

import java.io.IOException;
import java.util.List;

public class SubtasksHandler extends BaseHttpHandler {

    public SubtasksHandler(tracker.taskManager.TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    protected void handleRequest(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        switch (method) {
            case "GET":
                if (path.equals("/subtasks")) {
                    handleGetAllSubtasks(exchange);
                } else if (path.matches("/subtasks/\\d+")) {
                    handleGetSubtaskById(exchange);
                } else {
                    sendNotFound(exchange, "Неверный путь");
                }
                break;
            case "POST":
                if (path.equals("/subtasks")) {
                    handleCreateOrUpdateSubtask(exchange);
                } else {
                    sendNotFound(exchange, "Неверный путь");
                }
                break;
            case "DELETE":
                if (path.equals("/subtasks")) {
                    handleDeleteAllSubtasks(exchange);
                } else if (path.startsWith("/subtasks/")) {
                    handleDeleteSubtaskById(exchange);
                } else {
                    sendNotFound(exchange, "Неверный путь");
                }
                break;
            default:
                sendNotFound(exchange, "Метод не поддерживается");
        }
    }

    private void handleGetAllSubtasks(HttpExchange exchange) throws IOException {
        List<Subtask> subtasks = taskManager.getSubtasks();
        String response = gson.toJson(subtasks);
        sendText(exchange, response);
    }

    private void handleGetSubtaskById(HttpExchange exchange) throws IOException {
        String idParam = getPathParameter(exchange);
        if (idParam == null) {
            sendNotFound(exchange, "ID подзадачи не указан");
            return;
        }

        try {
            int id = Integer.parseInt(idParam);
            Subtask subtask = taskManager.getSubtaskById(id);
            if (subtask == null) {
                sendNotFound(exchange, "Подзадача с ID " + id + " не найдена");
                return;
            }
            String response = gson.toJson(subtask);
            sendText(exchange, response);
        } catch (NumberFormatException e) {
            sendNotFound(exchange, "Неверный формат ID");
        }
    }

    private void handleCreateOrUpdateSubtask(HttpExchange exchange) throws IOException {
        String requestBody = readText(exchange);
        if (requestBody.isEmpty()) {
            sendNotFound(exchange, "Тело запроса пустое");
            return;
        }

        try {
            Subtask subtask = gson.fromJson(requestBody, Subtask.class);
            if (subtask.getId() == 0) {
                // Создание новой подзадачи
                Subtask createdSubtask = taskManager.createSubtask(subtask);
                String response = gson.toJson(createdSubtask);
                sendCreated(exchange, response);
            } else {
                // Обновление существующей подзадачи
                taskManager.updateSubtask(subtask);
                sendCreated(exchange, "Подзадача обновлена");
            }
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("пересекается")) {
                sendHasInteractions(exchange, e.getMessage());
            } else {
                sendNotFound(exchange, e.getMessage());
            }
        }
    }

    private void handleDeleteAllSubtasks(HttpExchange exchange) throws IOException {
        taskManager.deleteAllSubtasks();
        sendText(exchange, "Все подзадачи удалены");
    }

    private void handleDeleteSubtaskById(HttpExchange exchange) throws IOException {
        String idParam = getPathParameter(exchange);
        if (idParam == null) {
            sendNotFound(exchange, "ID подзадачи не указан");
            return;
        }

        try {
            int id = Integer.parseInt(idParam);
            Subtask subtask = taskManager.getSubtaskById(id);
            if (subtask == null) {
                sendNotFound(exchange, "Подзадача с ID " + id + " не найдена");
                return;
            }
            taskManager.deleteSubtask(id);
            sendText(exchange, "Подзадача удалена");
        } catch (NumberFormatException e) {
            sendNotFound(exchange, "Неверный формат ID");
        }
    }
}
