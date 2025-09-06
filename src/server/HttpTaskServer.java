package server;

import com.sun.net.httpserver.HttpServer;
import server.handlers.*;
import tracker.taskManager.TaskManager;
import tracker.utils.Managers;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private final HttpServer server;
    private final TaskManager taskManager;

    public HttpTaskServer() throws IOException {
        this(Managers.getDefault());
    }

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this.taskManager = taskManager;
        this.server = HttpServer.create(new InetSocketAddress(PORT), 0);
        setupHandlers();
    }

    private void setupHandlers() {
        // Регистрируем обработчики для каждого пути
        server.createContext("/tasks", new TasksHandler(taskManager));
        server.createContext("/epics", new EpicsHandler(taskManager));
        server.createContext("/subtasks", new SubtasksHandler(taskManager));
        server.createContext("/history", new HistoryHandler(taskManager));
        server.createContext("/prioritized", new PrioritizedHandler(taskManager));
    }

    public void start() {
        System.out.println("Запуск сервера на порту " + PORT);
        server.start();
        System.out.println("HTTP Task Server запущен на http://localhost:" + PORT);
    }

    public void stop() {
        System.out.println("Остановка сервера...");
        server.stop(0);
        System.out.println("Сервер остановлен");
    }

    public static void main(String[] args) {
        try {
            HttpTaskServer server = new HttpTaskServer();
            server.start();

            // Добавляем обработчик для graceful shutdown
            Runtime.getRuntime().addShutdownHook(new Thread(server::stop));

            // Ждем бесконечно, пока сервер работает
            Thread.currentThread().join();
        } catch (IOException e) {
            System.err.println("Ошибка при запуске сервера: " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.err.println("Сервер был прерван: " + e.getMessage());
        }
    }
}
