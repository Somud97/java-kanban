
package tracker.taskManager;

import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;
import tracker.utils.TaskStatus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    public void save() {
        try (FileWriter writer = new FileWriter(file)) {
            // Записываем заголовок CSV
            writer.write("id,type,name,status,description,epic\n");

            // Сохраняем обычные задачи
            for (Task task : getTasks()) {
                writer.write(String.format("%d,TASK,%s,%s,%s,\n",
                        task.getId(),
                        task.getTitle(),
                        task.getStatus(),
                        task.getDescription()));
            }

            // Сохраняем эпики
            for (Epic epic : getEpics()) {
                writer.write(String.format("%d,EPIC,%s,%s,%s,\n",
                        epic.getId(),
                        epic.getTitle(),
                        epic.getStatus(),
                        epic.getDescription()));
            }

            // Сохраняем подзадачи
            for (Subtask subtask : getSubtasks()) {
                writer.write(String.format("%d,SUBTASK,%s,%s,%s,%d\n",
                        subtask.getId(),
                        subtask.getTitle(),
                        subtask.getStatus(),
                        subtask.getDescription(),
                        subtask.getEpicId()));
            }
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при сохранении в файл:", e);
        }
    }

    // Переопределяем методы, модифицирующие данные
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

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            // Пропускаем заголовок
            reader.readLine();

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    continue;
                }

                String[] parts = line.split(",");
                int id = Integer.parseInt(parts[0]);
                String type = parts[1];
                String name = parts[2];
                TaskStatus status = TaskStatus.valueOf(parts[3]);
                String description = parts[4];

                switch (type) {
                    case "TASK":
                        Task task = new Task(name, description);
                        task.setId(id);
                        task.setStatus(status);
                        manager.createTask(task);
                        break;
                    case "EPIC":
                        Epic epic = new Epic(name, description);
                        epic.setId(id);
                        epic.setStatus(status);
                        manager.createEpic(epic);
                        break;
                    case "SUBTASK":
                        int epicId = Integer.parseInt(parts[5]);
                        Subtask subtask = new Subtask(name, description, epicId);
                        subtask.setId(id);
                        subtask.setStatus(status);
                        manager.createSubtask(subtask);
                        Epic parentEpic = manager.getEpicById(epicId);
                        if (parentEpic != null) {
                            parentEpic.addSubtask(subtask);
                        }
                        break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при чтении из файла", e);
        }

        return manager;
    }

}
