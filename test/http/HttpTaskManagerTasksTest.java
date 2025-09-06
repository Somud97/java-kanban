package test.http;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.model.Task;
import tracker.taskManager.InMemoryTaskManager;
import tracker.taskManager.TaskManager;
import server.HttpTaskServer;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerTasksTest {

    private TaskManager manager;
    private HttpTaskServer taskServer;
    private Gson gson;
    private HttpClient client;

    public HttpTaskManagerTasksTest() throws IOException {
        manager = new InMemoryTaskManager();
        taskServer = new HttpTaskServer(manager);
        gson = HttpTaskServer.getGson();
        client = HttpClient.newHttpClient();
    }

    @BeforeEach
    public void setUp() {
        manager.deleteAllTasks();
        manager.deleteAllSubtasks();
        manager.deleteAllEpics();
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    public void testAddTask() throws IOException, InterruptedException {
        // создаём задачу
        Task task = new Task("Test Task", "Testing task",
                Duration.ofMinutes(5), LocalDateTime.now());
        // конвертируем её в JSON
        String taskJson = gson.toJson(task);

        // создаём HTTP-клиент и запрос
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .header("Content-Type", "application/json")
                .build();

        // вызываем REST, отвечающий за создание задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        // проверяем код ответа
        assertEquals(201, response.statusCode());

        // проверяем, что создалась одна задача с корректным именем
        List<Task> tasksFromManager = manager.getTasks();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Test Task", tasksFromManager.get(0).getTitle(), "Некорректное имя задачи");
    }

    @Test
    public void testGetAllTasks() throws IOException, InterruptedException {
        // создаём задачу напрямую в менеджере
        Task task = manager.createTask(new Task("Test Task", "Description"));
        
        // делаем GET запрос
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(200, response.statusCode());
        
        // проверяем, что ответ содержит нашу задачу
        assertTrue(response.body().contains("Test Task"));
        assertTrue(response.body().contains("Description"));
    }

    @Test
    public void testGetTaskById() throws IOException, InterruptedException {
        // создаём задачу
        Task task = manager.createTask(new Task("Test Task", "Description"));
        int taskId = task.getId();
        
        // делаем GET запрос по ID
        URI url = URI.create("http://localhost:8080/tasks/" + taskId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Test Task"));
        assertTrue(response.body().contains("Description"));
    }

    @Test
    public void testGetTaskByIdNotFound() throws IOException, InterruptedException {
        // делаем GET запрос по несуществующему ID
        URI url = URI.create("http://localhost:8080/tasks/999");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(404, response.statusCode());
    }

    @Test
    public void testUpdateTask() throws IOException, InterruptedException {
        // создаём задачу
        Task task = manager.createTask(new Task("Original Title", "Original Description"));
        int taskId = task.getId();
        
        // обновляем задачу
        task.setTitle("Updated Title");
        task.setDescription("Updated Description");
        String taskJson = gson.toJson(task);
        
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(201, response.statusCode());
        
        // проверяем, что задача обновилась
        Task updatedTask = manager.getTaskById(taskId);
        assertEquals("Updated Title", updatedTask.getTitle());
        assertEquals("Updated Description", updatedTask.getDescription());
    }

    @Test
    public void testDeleteTask() throws IOException, InterruptedException {
        // создаём задачу
        Task task = manager.createTask(new Task("Test Task", "Description"));
        int taskId = task.getId();
        
        // удаляем задачу
        URI url = URI.create("http://localhost:8080/tasks/" + taskId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(200, response.statusCode());
        
        // проверяем, что задача удалена
        assertNull(manager.getTaskById(taskId));
    }

    @Test
    public void testDeleteAllTasks() throws IOException, InterruptedException {
        // создаём несколько задач
        manager.createTask(new Task("Task 1", "Description 1"));
        manager.createTask(new Task("Task 2", "Description 2"));
        
        // удаляем все задачи
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(200, response.statusCode());
        
        // проверяем, что все задачи удалены
        assertEquals(0, manager.getTasks().size());
    }

    @Test
    public void testCreateTaskWithTimeOverlap() throws IOException, InterruptedException {
        // создаём первую задачу
        Task task1 = new Task("Task 1", "Description 1", 
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 10, 0));
        manager.createTask(task1);
        
        // пытаемся создать вторую задачу с пересекающимся временем
        Task task2 = new Task("Task 2", "Description 2", 
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 10, 30));
        String taskJson = gson.toJson(task2);
        
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(406, response.statusCode());
        assertTrue(response.body().contains("пересекается"));
    }

    @Test
    public void testCreateTaskWithInvalidData() throws IOException, InterruptedException {
        // отправляем пустое тело запроса
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(""))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(404, response.statusCode());
    }
}
