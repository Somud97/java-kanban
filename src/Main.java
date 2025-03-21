import tracker.taskManager.TaskManager;
import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;
import tracker.utils.Managers;

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
    }

    private static TaskManager getInMemoryTaskManager() {
        TaskManager inMemoryTaskManager = Managers.getDefault();

        Task task1 = inMemoryTaskManager.createTask("Создать таск", "Нужно закончить создание таска");
        Task task2 = inMemoryTaskManager.createTask("Посмотреть сериал", "Досмотреть его уже наконец");

        Epic epic1 = inMemoryTaskManager.createEpic("Переезд", "Уехать далеко-далеко");
        Epic epic2 = inMemoryTaskManager.createEpic("Поиск работы", "Получить как можно больше оферов");

        Subtask subtask1 = inMemoryTaskManager.createSubtask("Собрать вещи", "Не забыть про кошку", epic1.getId());
        Subtask subtask2 = inMemoryTaskManager.createSubtask("Приехать в аэропорт", "Вылет в 9:00", epic1.getId());

        Subtask subtask3 = inMemoryTaskManager.createSubtask("Открыть HH.ru", "Наконец уже", epic2.getId());
        return inMemoryTaskManager;
    }

    private static void printAllTasks(TaskManager manager) {
        System.out.println("Задачи:");
        for (Task task : manager.getTasks()) {
            System.out.println(task);
        }
        System.out.println("Эпики:");
        for (Epic epic : manager.getEpics()) {
            System.out.println(epic);

            for (Task subtask : epic.getSubtasks().values()) {
                System.out.println("--> " + subtask);
            }
        }
        System.out.println("Подзадачи:");
        for (Task subtask : manager.getSubtasks()) {
            System.out.println(subtask);
        }

        System.out.println("История:");
        for (Task task : manager.getHistory()) {
            System.out.println(task);
        }

        System.out.println("Длина истории:");
        System.out.println(manager.getHistory().size());
    }
}
