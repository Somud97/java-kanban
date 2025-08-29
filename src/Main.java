import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;
import tracker.taskManager.TaskManager;
import tracker.utils.Managers;

import java.time.Duration;
import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {
        TaskManager inMemoryTaskManager = getInMemoryTaskManager();

        inMemoryTaskManager.getTaskById(1);
        inMemoryTaskManager.getTaskById(2);

        inMemoryTaskManager.getEpicById(3);
        inMemoryTaskManager.getEpicById(4);

        inMemoryTaskManager.getSubtaskById(5);
        inMemoryTaskManager.getSubtaskById(6);
        inMemoryTaskManager.getSubtaskById(7);

        inMemoryTaskManager.getTaskById(1);
        inMemoryTaskManager.getTaskById(2);

        inMemoryTaskManager.getEpicById(3);
        inMemoryTaskManager.getEpicById(4);

        printAllTasks(inMemoryTaskManager);
        
        // Демонстрация нового метода getPrioritizedTasks
        System.out.println("\n=== Задачи, отсортированные по приоритету (startTime) ===");
        printPrioritizedTasks(inMemoryTaskManager);
        
        // Демонстрация проверки пересечений
        System.out.println("\n=== Демонстрация проверки пересечений ===");
        demonstrateOverlapChecking(inMemoryTaskManager);
    }

    private static TaskManager getInMemoryTaskManager() {
        TaskManager inMemoryTaskManager = Managers.getDefault();

        // Создаем задачи с duration и startTime (без пересечений)
        Task task1 = inMemoryTaskManager.createTask(
            new Task("Создать таск", "Нужно закончить создание таска", 
                    Duration.ofHours(2), LocalDateTime.of(2024, 1, 1, 9, 0))
        );
        Task task2 = inMemoryTaskManager.createTask(
            new Task("Посмотреть сериал", "Досмотреть его уже наконец",
                    Duration.ofHours(3), LocalDateTime.of(2024, 1, 1, 19, 0))
        );

        Epic epic1 = inMemoryTaskManager.createEpic(new Epic("Переезд", "Уехать далеко-далеко"));
        Epic epic2 = inMemoryTaskManager.createEpic(new Epic("Поиск работы", "Получить как можно больше оферов"));

        // Создаем подзадачи с duration и startTime (без пересечений)
        Subtask subtask1 = inMemoryTaskManager.createSubtask(
            new Subtask("Собрать вещи", "Не забыть про кошку", epic1.getId(),
                       Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 12, 0))
        );
        Subtask subtask2 = inMemoryTaskManager.createSubtask(
            new Subtask("Приехать в аэропорт", "Вылет в 9:00", epic1.getId(),
                       Duration.ofMinutes(30), LocalDateTime.of(2024, 1, 1, 7, 30))
        );

        Subtask subtask3 = inMemoryTaskManager.createSubtask(
            new Subtask("Открыть HH.ru", "Наконец уже", epic2.getId(),
                       Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 14, 0))
        );
        return inMemoryTaskManager;
    }

    private static void printAllTasks(TaskManager manager) {
        System.out.println("Задачи:");
        manager.getTasks().stream()
                .forEach(System.out::println);
        
        System.out.println("Эпики:");
        manager.getEpics().stream()
                .forEach(epic -> {
                    System.out.println(epic);
                    epic.getSubtasks().values().stream()
                            .forEach(subtask -> System.out.println("--> " + subtask));
                });
        
        System.out.println("Подзадачи:");
        manager.getSubtasks().stream()
                .forEach(System.out::println);

        System.out.println("История:");
        manager.getHistory().stream()
                .forEach(System.out::println);

        System.out.println("Длина истории:");
        System.out.println(manager.getHistory().size());
    }

    private static void printPrioritizedTasks(TaskManager manager) {
        System.out.println("Задачи и подзадачи, отсортированные по времени начала:");
        manager.getPrioritizedTasks().stream()
                .forEach(task -> System.out.println("[" + task.getStartTime() + "] " + task.getTitle() + 
                             " (ID: " + task.getId() + ", Тип: " + task.getClass().getSimpleName() + ")"));
        
        System.out.println("\nВсего задач с приоритетом: " + manager.getPrioritizedTasks().size());
    }

    private static void demonstrateOverlapChecking(TaskManager manager) {
        System.out.println("1. Проверка пересечений между существующими задачами:");
        
        var prioritizedTasks = manager.getPrioritizedTasks();
        for (int i = 0; i < prioritizedTasks.size(); i++) {
            for (int j = i + 1; j < prioritizedTasks.size(); j++) {
                Task task1 = prioritizedTasks.get(i);
                Task task2 = prioritizedTasks.get(j);
                
                boolean isOverlapping = manager.isTasksOverlapping(task1, task2);
                System.out.printf("   %s (%s - %s) и %s (%s - %s): %s%n",
                    task1.getTitle(), task1.getStartTime(), task1.getEndTime(),
                    task2.getTitle(), task2.getStartTime(), task2.getEndTime(),
                    isOverlapping ? "ПЕРЕСЕКАЮТСЯ ❌" : "не пересекаются ✅");
            }
        }

        System.out.println("\n2. Попытка создать задачу с пересечением:");
        try {
            Task overlappingTask = new Task("Пересекающаяся задача", "Эта задача пересекается с существующей",
                    Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 9, 30)); // Пересекается с task1
            
            manager.createTask(overlappingTask);
            System.out.println("   ❌ Ошибка: задача была создана, хотя должна была быть отклонена");
        } catch (IllegalArgumentException e) {
            System.out.println("   ✅ Правильно отклонена: " + e.getMessage());
        }

        System.out.println("\n3. Попытка создать задачу без пересечений:");
        try {
            Task nonOverlappingTask = new Task("Непересекающаяся задача", "Эта задача не пересекается ни с чем",
                    Duration.ofHours(1), LocalDateTime.of(2024, 1, 1, 15, 0)); // Не пересекается
            
            Task createdTask = manager.createTask(nonOverlappingTask);
            System.out.println("   ✅ Успешно создана: " + createdTask.getTitle() + 
                             " (" + createdTask.getStartTime() + " - " + createdTask.getEndTime() + ")");
        } catch (IllegalArgumentException e) {
            System.out.println("   ❌ Ошибка: " + e.getMessage());
        }

        System.out.println("\n4. Попытка обновить задачу с пересечением:");
        try {
            Task taskToUpdate = manager.getTasks().get(0); // Берем первую задачу
            taskToUpdate.setStartTime(LocalDateTime.of(2024, 1, 1, 12, 30)); // Пересекается с subtask1
            
            manager.updateTask(taskToUpdate);
            System.out.println("   ❌ Ошибка: задача была обновлена, хотя должна была быть отклонена");
        } catch (IllegalArgumentException e) {
            System.out.println("   ✅ Правильно отклонена: " + e.getMessage());
        }

        System.out.println("\n5. Проверка задачи без времени (не должна вызывать пересечений):");
        Task taskWithoutTime = new Task("Задача без времени", "Эта задача не имеет времени начала");
        boolean hasOverlaps = manager.hasTaskOverlaps(taskWithoutTime);
        System.out.println("   Задача без времени имеет пересечения: " + (hasOverlaps ? "Да ❌" : "Нет ✅"));
    }
}
