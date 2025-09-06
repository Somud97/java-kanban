package server.handlers;

import com.sun.net.httpserver.HttpExchange;
import tracker.model.Epic;
import tracker.model.Subtask;

import java.io.IOException;
import java.util.List;

public class EpicsHandler extends BaseHttpHandler {

    public EpicsHandler(tracker.taskManager.TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    protected void handleRequest(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        switch (method) {
            case "GET":
                if (path.equals("/epics")) {
                    handleGetAllEpics(exchange);
                } else if (path.matches("/epics/\\d+")) {
                    handleGetEpicById(exchange);
                } else if (path.matches("/epics/\\d+/subtasks")) {
                    handleGetSubtasksByEpic(exchange);
                } else {
                    sendNotFound(exchange, "Неверный путь");
                }
                break;
            case "POST":
                if (path.equals("/epics")) {
                    handleCreateOrUpdateEpic(exchange);
                } else {
                    sendNotFound(exchange, "Неверный путь");
                }
                break;
            case "DELETE":
                if (path.equals("/epics")) {
                    handleDeleteAllEpics(exchange);
                } else if (path.matches("/epics/\\d+")) {
                    handleDeleteEpicById(exchange);
                } else {
                    sendNotFound(exchange, "Неверный путь");
                }
                break;
            default:
                sendNotFound(exchange, "Метод не поддерживается");
        }
    }

    private void handleGetAllEpics(HttpExchange exchange) throws IOException {
        List<Epic> epics = taskManager.getEpics();
        String response = gson.toJson(epics);
        sendText(exchange, response);
    }

    private void handleGetEpicById(HttpExchange exchange) throws IOException {
        String idParam = getPathParameter(exchange);
        if (idParam == null) {
            sendNotFound(exchange, "ID эпика не указан");
            return;
        }

        try {
            int id = Integer.parseInt(idParam);
            Epic epic = taskManager.getEpicById(id);
            if (epic == null) {
                sendNotFound(exchange, "Эпик с ID " + id + " не найден");
                return;
            }
            String response = gson.toJson(epic);
            sendText(exchange, response);
        } catch (NumberFormatException e) {
            sendNotFound(exchange, "Неверный формат ID");
        }
    }

    private void handleGetSubtasksByEpic(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String[] pathParts = path.split("/");
        if (pathParts.length < 3) {
            sendNotFound(exchange, "ID эпика не указан");
            return;
        }

        try {
            int epicId = Integer.parseInt(pathParts[2]);
            List<Subtask> subtasks = taskManager.getSubtasksByEpic(epicId);
            String response = gson.toJson(subtasks);
            sendText(exchange, response);
        } catch (NumberFormatException e) {
            sendNotFound(exchange, "Неверный формат ID");
        } catch (Exception e) {
            sendNotFound(exchange, e.getMessage());
        }
    }

    private void handleCreateOrUpdateEpic(HttpExchange exchange) throws IOException {
        String requestBody = readText(exchange);
        if (requestBody.isEmpty()) {
            sendNotFound(exchange, "Тело запроса пустое");
            return;
        }

        try {
            Epic epic = gson.fromJson(requestBody, Epic.class);

            if (epic.getSubtasks() == null) {
                try {
                    java.lang.reflect.Field subtasksField = Epic.class.getDeclaredField("subtasks");
                    subtasksField.setAccessible(true);
                    subtasksField.set(epic, new java.util.HashMap<>());
                } catch (Exception e) {
                    throw new RuntimeException("Failed to initialize subtasks field", e);
                }
            }

            if (epic.getId() == 0) {
                // Создание нового эпика
                Epic createdEpic = taskManager.createEpic(epic);
                String response = gson.toJson(createdEpic);
                sendCreated(exchange, response);
            } else {
                // Обновление существующего эпика
                taskManager.updateEpic(epic);
                sendCreated(exchange, "Эпик обновлен");
            }
        } catch (IllegalArgumentException e) {
            sendNotFound(exchange, e.getMessage());
        }
    }

    private void handleDeleteAllEpics(HttpExchange exchange) throws IOException {
        taskManager.deleteAllEpics();
        sendText(exchange, "Все эпики удалены");
    }

    private void handleDeleteEpicById(HttpExchange exchange) throws IOException {
        String idParam = getPathParameter(exchange);
        if (idParam == null) {
            sendNotFound(exchange, "ID эпика не указан");
            return;
        }

        try {
            int id = Integer.parseInt(idParam);
            Epic epic = taskManager.getEpicById(id);
            if (epic == null) {
                sendNotFound(exchange, "Эпик с ID " + id + " не найден");
                return;
            }
            taskManager.deleteEpic(id);
            sendText(exchange, "Эпик удален");
        } catch (NumberFormatException e) {
            sendNotFound(exchange, "Неверный формат ID");
        }
    }
}
