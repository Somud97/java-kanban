package server.handlers;

import com.sun.net.httpserver.HttpExchange;
import tracker.model.Task;

import java.io.IOException;
import java.util.List;

public class TasksHandler extends BaseHttpHandler {

    public TasksHandler(tracker.taskManager.TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    protected void handleRequest(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        switch (method) {
            case "GET":
                if (path.equals("/tasks")) {
                    handleGetAllTasks(exchange);
                } else if (path.matches("/tasks/\\d+")) {
                    handleGetTaskById(exchange);
                } else {
                    sendNotFound(exchange, "Неверный путь");
                }
                break;
            case "POST":
                if (path.equals("/tasks")) {
                    handleCreateOrUpdateTask(exchange);
                } else {
                    sendNotFound(exchange, "Неверный путь");
                }
                break;
            case "DELETE":
                if (path.equals("/tasks")) {
                    handleDeleteAllTasks(exchange);
                } else if (path.matches("/tasks/\\d+")) {
                    handleDeleteTaskById(exchange);
                } else {
                    sendNotFound(exchange, "Неверный путь");
                }
                break;
            default:
                sendMethodNotAllowed(exchange, "Метод не поддерживается");
        }
    }

    private void handleGetAllTasks(HttpExchange exchange) throws IOException {
        List<Task> tasks = taskManager.getTasks();
        String response = gson.toJson(tasks);
        sendText(exchange, response);
    }

    private void handleGetTaskById(HttpExchange exchange) throws IOException {
        String idParam = getPathParameter(exchange);
        if (idParam == null) {
            sendBadRequest(exchange, "ID задачи не указан");
            return;
        }

        try {
            int id = Integer.parseInt(idParam);
            Task task = taskManager.getTaskById(id);
            if (task == null) {
                sendNotFound(exchange, "Задача с ID " + id + " не найдена");
                return;
            }
            String response = gson.toJson(task);
            sendText(exchange, response);
        } catch (NumberFormatException e) {
            sendBadRequest(exchange, "Неверный формат ID");
        }
    }

    private void handleCreateOrUpdateTask(HttpExchange exchange) throws IOException {
        String requestBody = readText(exchange);
        if (requestBody.isEmpty()) {
            sendNotFound(exchange, "Тело запроса пустое");
            return;
        }

        try {
            Task task = gson.fromJson(requestBody, Task.class);
            System.out.println(task.getId() == 0);
            if (task.getId() == 0) {
                // Создание новой задачи
                Task createdTask = taskManager.createTask(task);
                System.out.println("Created task: " + createdTask);
                String response = gson.toJson(createdTask);
                System.out.println("JSON response: " + response);
                sendCreated(exchange, response);
            } else {
                // Обновление существующей задачи
                taskManager.updateTask(task);
                sendCreated(exchange, "Задача обновлена");
            }
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("пересекается")) {
                sendHasInteractions(exchange, e.getMessage());
            } else {
                sendNotFound(exchange, e.getMessage());
            }
        }
    }

    private void handleDeleteAllTasks(HttpExchange exchange) throws IOException {
        taskManager.deleteAllTasks();
        sendText(exchange, "Все задачи удалены");
    }

    private void handleDeleteTaskById(HttpExchange exchange) throws IOException {
        String idParam = getPathParameter(exchange);
        if (idParam == null) {
            sendBadRequest(exchange, "ID задачи не указан");
            return;
        }

        try {
            int id = Integer.parseInt(idParam);
            Task task = taskManager.getTaskById(id);
            if (task == null) {
                sendNotFound(exchange, "Задача с ID " + id + " не найдена");
                return;
            }
            taskManager.deleteTask(id);
            sendText(exchange, "Задача удалена");
        } catch (NumberFormatException e) {
            sendBadRequest(exchange, "Неверный формат ID");
        }
    }
}
