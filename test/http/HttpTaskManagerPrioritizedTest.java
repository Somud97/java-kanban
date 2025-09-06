package test.http;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.handlers.BaseHttpHandler;
import tracker.model.Epic;
import tracker.model.Subtask;
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

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerPrioritizedTest {

    private TaskManager manager;
    private HttpTaskServer taskServer;
    private Gson gson;
    private HttpClient client;

    public HttpTaskManagerPrioritizedTest() throws IOException {
        manager = new InMemoryTaskManager();
        taskServer = new HttpTaskServer(manager);
        gson = BaseHttpHandler.getGson();
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
    public void testGetEmptyPrioritizedTasks() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(200, response.statusCode());
        assertEquals("[]", response.body());
    }

    @Test
    public void testGetPrioritizedTasksWithTime() throws IOException, InterruptedException {
        // создаём задачи с разным временем
        Task task1 = manager.createTask(new Task("Task 1", "Description 1", 
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 10, 0)));
        Task task2 = manager.createTask(new Task("Task 2", "Description 2", 
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 9, 0)));
        Task task3 = manager.createTask(new Task("Task 3", "Description 3", 
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 11, 0)));
        
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(200, response.statusCode());
        
        // проверяем, что все задачи с временем в ответе
        assertTrue(response.body().contains("Task 1"));
        assertTrue(response.body().contains("Task 2"));
        assertTrue(response.body().contains("Task 3"));
    }

    @Test
    public void testGetPrioritizedTasksWithoutTime() throws IOException, InterruptedException {
        // создаём задачи без времени
        Task task1 = manager.createTask(new Task("Task 1", "Description 1"));
        Task task2 = manager.createTask(new Task("Task 2", "Description 2"));
        
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(200, response.statusCode());
        
        // задачи без времени не должны попадать в приоритетные
        assertEquals("[]", response.body());
    }

    @Test
    public void testGetPrioritizedTasksWithSubtasks() throws IOException, InterruptedException {
        // создаём эпик и подзадачи
        Epic epic = manager.createEpic(new Epic("Epic", "Description"));
        Subtask subtask1 = manager.createSubtask(new Subtask("Subtask 1", "Description 1", epic.getId(),
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 10, 0)));
        Subtask subtask2 = manager.createSubtask(new Subtask("Subtask 2", "Description 2", epic.getId(),
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 9, 0)));
        
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(200, response.statusCode());
        
        // проверяем, что подзадачи в приоритетных задачах
        assertTrue(response.body().contains("Subtask 1"));
        assertTrue(response.body().contains("Subtask 2"));
    }

    @Test
    public void testGetPrioritizedTasksOrder() throws IOException, InterruptedException {
        // создаём задачи в разном порядке
        Task task1 = manager.createTask(new Task("Task 1", "Description 1", 
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 12, 0)));
        Task task2 = manager.createTask(new Task("Task 2", "Description 2", 
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 10, 0)));
        Task task3 = manager.createTask(new Task("Task 3", "Description 3", 
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 11, 0)));
        
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(200, response.statusCode());
        
        // проверяем порядок: Task 2 (10:00), Task 3 (11:00), Task 1 (12:00)
        String responseBody = response.body();
        int task2Index = responseBody.indexOf("Task 2");
        int task3Index = responseBody.indexOf("Task 3");
        int task1Index = responseBody.indexOf("Task 1");
        
        assertTrue(task2Index < task3Index, "Task 2 должна быть раньше Task 3");
        assertTrue(task3Index < task1Index, "Task 3 должна быть раньше Task 1");
    }

    @Test
    public void testGetPrioritizedTasksWithConsecutiveTime() throws IOException, InterruptedException {
        LocalDateTime baseTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        
        // создаём задачи с последовательным временем (не пересекающиеся)
        Task task1 = manager.createTask(new Task("Task 1", "Description 1", 
                Duration.ofHours(1), baseTime));
        Task task2 = manager.createTask(new Task("Task 2", "Description 2", 
                Duration.ofHours(1), baseTime.plusHours(1))); // начинается сразу после первой
        
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(200, response.statusCode());
        
        // обе задачи должны быть в ответе
        assertTrue(response.body().contains("Task 1"));
        assertTrue(response.body().contains("Task 2"));
        
        // проверяем порядок: Task 1 должна быть раньше Task 2
        String responseBody = response.body();
        int task1Index = responseBody.indexOf("Task 1");
        int task2Index = responseBody.indexOf("Task 2");
        assertTrue(task1Index < task2Index, "Task 1 должна быть раньше Task 2");
    }

    @Test
    public void testGetPrioritizedTasksAfterDeletion() throws IOException, InterruptedException {
        // создаём задачи
        Task task1 = manager.createTask(new Task("Task 1", "Description 1", 
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 10, 0)));
        Task task2 = manager.createTask(new Task("Task 2", "Description 2", 
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 11, 0)));
        
        // удаляем одну задачу
        manager.deleteTask(task1.getId());
        
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(200, response.statusCode());
        
        // проверяем, что удалённая задача не в приоритетных
        assertFalse(response.body().contains("Task 1"));
        assertTrue(response.body().contains("Task 2"));
    }

    @Test
    public void testGetPrioritizedTasksWithMixedTypes() throws IOException, InterruptedException {
        LocalDateTime baseTime = LocalDateTime.of(2024, 1, 1, 10, 0);
        
        // создаём задачи разных типов
        Task task = manager.createTask(new Task("Task", "Description", 
                Duration.ofHours(1), baseTime));
        
        Epic epic = manager.createEpic(new Epic("Epic", "Description"));
        Subtask subtask = manager.createSubtask(new Subtask("Subtask", "Description", epic.getId(),
                Duration.ofHours(1), baseTime.plusHours(1)));
        
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(200, response.statusCode());
        
        // проверяем, что и задачи, и подзадачи в приоритетных
        assertTrue(response.body().contains("Task"));
        assertTrue(response.body().contains("Subtask"));
    }
}
