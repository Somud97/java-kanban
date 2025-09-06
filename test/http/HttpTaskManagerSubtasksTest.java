package test.http;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.model.Epic;
import tracker.model.Subtask;
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

public class HttpTaskManagerSubtasksTest {

    private TaskManager manager;
    private HttpTaskServer taskServer;
    private Gson gson;
    private HttpClient client;
    private Epic epic;

    public HttpTaskManagerSubtasksTest() throws IOException {
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
        epic = manager.createEpic(new Epic("Test Epic", "Description"));
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    public void testAddSubtask() throws IOException, InterruptedException {
        Subtask subtask = new Subtask("Test Subtask", "Testing subtask", epic.getId(), null, null);
        String subtaskJson = gson.toJson(subtask);

        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(201, response.statusCode());

        List<Subtask> subtasksFromManager = manager.getSubtasks();
        assertNotNull(subtasksFromManager, "Подзадачи не возвращаются");
        assertEquals(1, subtasksFromManager.size(), "Некорректное количество подзадач");
        assertEquals("Test Subtask", subtasksFromManager.get(0).getTitle(), "Некорректное имя подзадачи");
    }

    @Test
    public void testGetAllSubtasks() throws IOException, InterruptedException {
        manager.createSubtask(new Subtask("Test Subtask", "Description", epic.getId(), null, null));
        
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Test Subtask"));
        assertTrue(response.body().contains("Description"));
    }

    @Test
    public void testGetSubtaskById() throws IOException, InterruptedException {
        Subtask subtask = manager.createSubtask(new Subtask("Test Subtask", "Description", epic.getId(), null, null));
        int subtaskId = subtask.getId();
        
        URI url = URI.create("http://localhost:8080/subtasks/" + subtaskId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Test Subtask"));
        assertTrue(response.body().contains("Description"));
    }

    @Test
    public void testGetSubtaskByIdNotFound() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/subtasks/999");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(404, response.statusCode());
    }

    @Test
    public void testUpdateSubtask() throws IOException, InterruptedException {
        Subtask subtask = manager.createSubtask(new Subtask("Original Title", "Original Description", epic.getId(), null, null));
        int subtaskId = subtask.getId();
        
        subtask.setTitle("Updated Title");
        subtask.setDescription("Updated Description");
        String subtaskJson = gson.toJson(subtask);
        
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(201, response.statusCode());
        
        Subtask updatedSubtask = manager.getSubtaskById(subtaskId);
        assertEquals("Updated Title", updatedSubtask.getTitle());
        assertEquals("Updated Description", updatedSubtask.getDescription());
    }

    @Test
    public void testDeleteSubtask() throws IOException, InterruptedException {
        Subtask subtask = manager.createSubtask(new Subtask("Test Subtask", "Description", epic.getId(), null, null));
        int subtaskId = subtask.getId();
        
        URI url = URI.create("http://localhost:8080/subtasks/" + subtaskId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(200, response.statusCode());
        
        assertNull(manager.getSubtaskById(subtaskId));
    }

    @Test
    public void testDeleteAllSubtasks() throws IOException, InterruptedException {
        manager.createSubtask(new Subtask("Subtask 1", "Description 1", epic.getId(), null, null));
        manager.createSubtask(new Subtask("Subtask 2", "Description 2", epic.getId(), null, null));
        
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(200, response.statusCode());
        assertEquals(0, manager.getSubtasks().size());
    }

    @Test
    public void testCreateSubtaskWithTimeOverlap() throws IOException, InterruptedException {
        // создаём первую подзадачу
        Subtask subtask1 = manager.createSubtask(new Subtask("Subtask 1", "Description 1", epic.getId(),
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 10, 0)));
        
        // пытаемся создать вторую подзадачу с пересекающимся временем
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", epic.getId(),
                Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 10, 30));
        String subtaskJson = gson.toJson(subtask2);
        
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(406, response.statusCode());
        assertTrue(response.body().contains("пересекается"));
    }

    @Test
    public void testCreateSubtaskWithInvalidEpicId() throws IOException, InterruptedException {
        Subtask subtask = new Subtask("Test Subtask", "Description", 999, null, null);
        String subtaskJson = gson.toJson(subtask);
        
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(404, response.statusCode());
    }

    @Test
    public void testCreateSubtaskWithInvalidData() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(""))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(404, response.statusCode());
    }
}
