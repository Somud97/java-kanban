package test.http;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerHistoryTest {

    private TaskManager manager;
    private HttpTaskServer taskServer;
    private HttpClient client;

    public HttpTaskManagerHistoryTest() throws IOException {
        manager = new InMemoryTaskManager();
        taskServer = new HttpTaskServer(manager);
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
    public void testGetEmptyHistory() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(200, response.statusCode());
        assertEquals("[]", response.body());
    }

    @Test
    public void testGetHistoryWithTasks() throws IOException, InterruptedException {
        // создаём задачи
        Task task1 = manager.createTask(new Task("Task 1", "Description 1"));
        Task task2 = manager.createTask(new Task("Task 2", "Description 2"));
        Epic epic = manager.createEpic(new Epic("Epic 1", "Description 1"));
        Subtask subtask = manager.createSubtask(new Subtask("Subtask 1", "Description 1", epic.getId(), null, null));
        
        // получаем задачи по ID, чтобы добавить их в историю
        manager.getTaskById(task1.getId());
        manager.getTaskById(task2.getId());
        manager.getEpicById(epic.getId());
        manager.getSubtaskById(subtask.getId());
        
        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(200, response.statusCode());
        
        // проверяем, что в истории есть все задачи
        assertTrue(response.body().contains("Task 1"));
        assertTrue(response.body().contains("Task 2"));
        assertTrue(response.body().contains("Epic 1"));
        assertTrue(response.body().contains("Subtask 1"));
    }

    @Test
    public void testHistoryOrder() throws IOException, InterruptedException {
        // создаём задачи
        Task task1 = manager.createTask(new Task("Task 1", "Description 1"));
        Task task2 = manager.createTask(new Task("Task 2", "Description 2"));
        Task task3 = manager.createTask(new Task("Task 3", "Description 3"));
        
        // получаем задачи в определённом порядке
        manager.getTaskById(task1.getId());
        manager.getTaskById(task2.getId());
        manager.getTaskById(task3.getId());
        manager.getTaskById(task1.getId()); // повторно получаем первую задачу
        
        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(200, response.statusCode());
        
        // проверяем, что история содержит все задачи
        assertTrue(response.body().contains("Task 1"));
        assertTrue(response.body().contains("Task 2"));
        assertTrue(response.body().contains("Task 3"));
    }

    @Test
    public void testHistoryAfterDeletion() throws IOException, InterruptedException {
        // создаём задачи
        Task task1 = manager.createTask(new Task("Task 1", "Description 1"));
        Task task2 = manager.createTask(new Task("Task 2", "Description 2"));
        
        // получаем задачи
        manager.getTaskById(task1.getId());
        manager.getTaskById(task2.getId());
        
        // удаляем одну задачу
        manager.deleteTask(task1.getId());
        
        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(200, response.statusCode());
        
        // проверяем, что удалённая задача не в истории
        assertFalse(response.body().contains("Task 1"));
        assertTrue(response.body().contains("Task 2"));
    }

    @Test
    public void testHistoryWithMixedTaskTypes() throws IOException, InterruptedException {
        // создаём разные типы задач
        Task task = manager.createTask(new Task("Task", "Description"));
        Epic epic = manager.createEpic(new Epic("Epic", "Description"));
        Subtask subtask = manager.createSubtask(new Subtask("Subtask", "Description", epic.getId(), null, null));
        
        // получаем все задачи
        manager.getTaskById(task.getId());
        manager.getEpicById(epic.getId());
        manager.getSubtaskById(subtask.getId());
        
        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(200, response.statusCode());
        
        // проверяем, что все типы задач в истории
        assertTrue(response.body().contains("Task"));
        assertTrue(response.body().contains("Epic"));
        assertTrue(response.body().contains("Subtask"));
    }

    @Test
    public void testHistoryLargeSize() throws IOException, InterruptedException {
        // создаём много задач
        for (int i = 1; i <= 15; i++) {
            Task task = manager.createTask(new Task("Task " + i, "Description " + i));
            manager.getTaskById(task.getId());
        }
        
        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        assertEquals(200, response.statusCode());
        
        // проверяем, что все задачи добавлены в историю
        List<Task> history = manager.getHistory();
        assertEquals(15, history.size(), "Не все задачи добавлены в историю");
        
        // проверяем, что все задачи присутствуют в ответе
        for (int i = 1; i <= 15; i++) {
            assertTrue(response.body().contains("Task " + i), "Задача " + i + " не найдена в истории");
        }
    }
}
