
package tracker.taskManager;

import tracker.exceptions.ManagerSaveException;
import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;
import tracker.utils.TaskStatus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FileBackendTaskManager extends InMemoryTaskManager {
    private final File file;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private boolean isLoading = false;

    public FileBackendTaskManager(File file) {
        this.file = file;
    }

    public void save() {
        if (isLoading) {
            return;
        }
        try (FileWriter writer = new FileWriter(file)) {
            // Записываем заголовок CSV
            writer.write("id,type,name,status,description,epic,duration,startTime\n");

            // Сохраняем обычные задачи
            getTasks().stream()
                    .forEach(task -> {
                        try {
                            writer.write(String.format("%d,TASK,%s,%s,%s,,%s,%s\n",
                                    task.getId(),
                                    task.getTitle(),
                                    task.getStatus(),
                                    task.getDescription(),
                                    task.getDuration() != null ? task.getDuration().toMinutes() : "",
                                    task.getStartTime() != null ? task.getStartTime().format(formatter) : ""));
                        } catch (IOException e) {
                            throw new ManagerSaveException("Ошибка при записи задачи", e);
                        }
                    });

            // Сохраняем эпики
            getEpics().stream()
                    .forEach(epic -> {
                        try {
                            writer.write(String.format("%d,EPIC,%s,%s,%s,,%s,%s\n",
                                    epic.getId(),
                                    epic.getTitle(),
                                    epic.getStatus(),
                                    epic.getDescription(),
                                    epic.getDuration() != null ? epic.getDuration().toMinutes() : "",
                                    epic.getStartTime() != null ? epic.getStartTime().format(formatter) : ""));
                        } catch (IOException e) {
                            throw new ManagerSaveException("Ошибка при записи эпика", e);
                        }
                    });

            // Сохраняем подзадачи
            getSubtasks().stream()
                    .forEach(subtask -> {
                        try {
                            writer.write(String.format("%d,SUBTASK,%s,%s,%s,%d,%s,%s\n",
                                    subtask.getId(),
                                    subtask.getTitle(),
                                    subtask.getStatus(),
                                    subtask.getDescription(),
                                    subtask.getEpicId(),
                                    subtask.getDuration() != null ? subtask.getDuration().toMinutes() : "",
                                    subtask.getStartTime() != null ? subtask.getStartTime().format(formatter) : ""));
                        } catch (IOException e) {
                            throw new ManagerSaveException("Ошибка при записи подзадачи", e);
                        }
                    });
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении в файл", e);
        }
    }

    @Override
    public Task createTask(Task task) {
        Task createdTask = super.createTask(task);
        save();
        return createdTask;
    }

    @Override
    public Epic createEpic(Epic epic) {
        Epic createdEpic = super.createEpic(epic);
        save();
        return createdEpic;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        Subtask createdSubtask = super.createSubtask(subtask);
        save();
        return createdSubtask;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    public static FileBackendTaskManager loadFromFile(File file) {
        FileBackendTaskManager manager = new FileBackendTaskManager(file);
        manager.isLoading = true;
        // Предварительные проверки доступности файла
        if (!file.exists() || !file.isFile()) {
            manager.isLoading = false;
            throw new ManagerSaveException(
                    "Файл не найден: " + file.getPath(),
                    new java.io.FileNotFoundException(file.getPath())
            );
        }
        if (!file.canRead()) {
            manager.isLoading = false;
            throw new ManagerSaveException(
                    "Файл недоступен для чтения: " + file.getPath(),
                    new IOException("Cannot read: " + file.getPath())
            );
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            // Проверяем и читаем заголовок
            String header = reader.readLine();
            String expectedHeader = "id,type,name,status,description,epic,duration,startTime";
            if (header == null || !header.trim().equals(expectedHeader)) {
                throw new ManagerSaveException(
                        "Некорректный заголовок CSV: '" + header + "', ожидалось: '" + expectedHeader + "'",
                        new IllegalArgumentException(String.valueOf(header))
                );
            }

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    continue;
                }

                String[] parts = line.split(",");

                try {
                    if (parts.length < 5) {
                        throw new ManagerSaveException(
                                "Некорректная строка CSV (мало полей): " + line,
                                new IllegalArgumentException(line)
                        );
                    }

                    int id = Integer.parseInt(parts[0]);
                    String type = parts[1];
                    String name = parts[2];
                    TaskStatus status = TaskStatus.valueOf(parts[3]);
                    String description = parts[4];

                    // Парсим новые поля
                    Duration duration = null;
                    LocalDateTime startTime = null;

                    if (parts.length > 6 && !parts[6].isEmpty()) {
                        duration = Duration.ofMinutes(Long.parseLong(parts[6]));
                    }
                    if (parts.length > 7 && !parts[7].isEmpty()) {
                        startTime = LocalDateTime.parse(parts[7], formatter);
                    }

                    switch (type) {
                        case "TASK":
                            Task task = new Task(name, description, duration, startTime);
                            task.setId(id);
                            task.setStatus(status);
                            manager.createTask(task);
                            break;
                        case "EPIC":
                            Epic epic = new Epic(name, description, duration, startTime);
                            epic.setId(id);
                            epic.setStatus(status);
                            manager.createEpic(epic);
                            break;
                        case "SUBTASK":
                            if (parts.length < 6) {
                                throw new ManagerSaveException(
                                        "Некорректная строка CSV для SUBTASK: " + line,
                                        new IllegalArgumentException(line)
                                );
                            }
                            int epicId = Integer.parseInt(parts[5]);
                            Subtask subtask = new Subtask(name, description, epicId, duration, startTime);
                            subtask.setId(id);
                            subtask.setStatus(status);
                            manager.createSubtask(subtask);
                            break;
                        default:
                            throw new ManagerSaveException(
                                    "Неизвестный тип записи: " + type + ", строка: " + line,
                                    new IllegalArgumentException(type)
                            );
                    }
                } catch (RuntimeException parseError) {
                    throw new ManagerSaveException("Ошибка парсинга строки: " + line, parseError);
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении в файл", e);
        } finally {
            manager.isLoading = false;
            // Однократное сохранение после успешной загрузки для консистентности
            manager.save();
        }

        return manager;
    }

}
