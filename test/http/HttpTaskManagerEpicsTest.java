package test.http;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.HttpTaskServer;
import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.taskManager.InMemoryTaskManager;
import tracker.taskManager.TaskManager;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerEpicsTest {

    private TaskManager manager;
    private HttpTaskServer taskServer;
    private Gson gson;
    private HttpClient client;

    public HttpTaskManagerEpicsTest() throws IOException {
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
    public void testAddEpic() throws IOException, InterruptedException {
        // создаём эпик
        Epic epic = new Epic("Test Epic", "Testing epic");
        String epicJson = gson.toJson(epic);

        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(201, response.statusCode());

        List<Epic> epicsFromManager = manager.getEpics();
        assertNotNull(epicsFromManager, "Эпики не возвращаются");
        assertEquals(1, epicsFromManager.size(), "Некорректное количество эпиков");
        assertEquals("Test Epic", epicsFromManager.get(0).getTitle(), "Некорректное имя эпика");
    }

    @Test
    public void testGetAllEpics() throws IOException, InterruptedException {
        // создаём эпик напрямую в менеджере
        Epic epic = manager.createEpic(new Epic("Test Epic", "Description"));
        
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Test Epic"));
        assertTrue(response.body().contains("Description"));
    }

    @Test
    public void testGetEpicById() throws IOException, InterruptedException {
        Epic epic = manager.createEpic(new Epic("Test Epic", "Description"));
        int epicId = epic.getId();
        
        URI url = URI.create("http://localhost:8080/epics/" + epicId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Test Epic"));
        assertTrue(response.body().contains("Description"));
    }

    @Test
    public void testGetEpicByIdNotFound() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/epics/999");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(404, response.statusCode());
    }

    @Test
    public void testUpdateEpic() throws IOException, InterruptedException {
        Epic epic = manager.createEpic(new Epic("Original Title", "Original Description"));
        int epicId = epic.getId();
        
        epic.setTitle("Updated Title");
        epic.setDescription("Updated Description");
        String epicJson = gson.toJson(epic);
        
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(201, response.statusCode());
        
        Epic updatedEpic = manager.getEpicById(epicId);
        assertEquals("Updated Title", updatedEpic.getTitle());
        assertEquals("Updated Description", updatedEpic.getDescription());
    }

    @Test
    public void testDeleteEpic() throws IOException, InterruptedException {
        Epic epic = manager.createEpic(new Epic("Test Epic", "Description"));
        int epicId = epic.getId();
        
        URI url = URI.create("http://localhost:8080/epics/" + epicId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(200, response.statusCode());
        
        assertNull(manager.getEpicById(epicId));
    }

    @Test
    public void testDeleteAllEpics() throws IOException, InterruptedException {
        manager.createEpic(new Epic("Epic 1", "Description 1"));
        manager.createEpic(new Epic("Epic 2", "Description 2"));
        
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(200, response.statusCode());
        assertEquals(0, manager.getEpics().size());
    }

    @Test
    public void testGetSubtasksByEpic() throws IOException, InterruptedException {
        Epic epic = manager.createEpic(new Epic("Test Epic", "Description"));
        int epicId = epic.getId();
        
        // создаём подзадачу
        Subtask subtask = manager.createSubtask(new Subtask("Test Subtask", "Description", epicId, null, null));
        
        URI url = URI.create("http://localhost:8080/epics/" + epicId + "/subtasks");
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
    public void testGetSubtasksByEpicNotFound() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/epics/999/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(404, response.statusCode());
    }

    @Test
    public void testCreateEpicWithInvalidData() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(""))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(404, response.statusCode());
    }
}
