import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        TaskManager taskManager = new TaskManager();

        Task task1 = taskManager.createTask(new Task("Создать таск", "Нужно закончить создание таска"));
        Task task2 = taskManager.createTask(new Task("Посмотреть сериал", "Досмотреть его уже наконец"));

        Epic epic1 = taskManager.createEpic(new Epic("Переезд", "Уехать далеко-далеко"));
        Epic epic2 = taskManager.createEpic(new Epic("Поиск работы", "Получить как можно больше оферов"));

        Subtask subtask1 = taskManager.createSubtask(new Subtask("Собрать вещи", "Не забыть про кошку", epic1.getUid()));
        Subtask subtask2 = taskManager.createSubtask(new Subtask("Приехать в аэропорт", "Вылет в 9:00", epic1.getUid()));

        Subtask subtask3 = taskManager.createSubtask(new Subtask("Открыть HH.ru", "Наконец уже", epic2.getUid()));

        System.out.println("\nСписок тасков:");
        System.out.println(taskManager.getTasks());
        System.out.println("\nСписок эпиков:");
        System.out.println(taskManager.getEpics());
        System.out.println("\nСписок сабтасков:");
        System.out.println(taskManager.getSubtasks());

        System.out.println("\nПробуем найти таск по ЮИД:");
        System.out.println(taskManager.getTaskByUid(1));

        System.out.println("\nПробуем найти епик по ЮИД:");
        System.out.println(taskManager.getEpicByUid(3));

        System.out.println("\nПробуем найти сабтаск по ЮИД:");
        System.out.println(taskManager.getSubtaskByUid(5));

        System.out.println("\nПробуем получить список сабтасков 2 эпика:");
        System.out.println(taskManager.getEpicsSubtasks(epic2));

        System.out.println("\nПробуем обновить статус сабтаска 1 в IN_PROGRESS:");
        taskManager.setSubtaskStatus(subtask1, TaskStatus.IN_PROGRESS);
        if (subtask1.getStatus() == TaskStatus.IN_PROGRESS && epic1.getStatus() == TaskStatus.IN_PROGRESS) {
            System.out.println("Статус успешно изменен");
        }

        System.out.println("\nПробуем обновить статус сабтаска 1 и сабтаска 2 в DONE:");
        taskManager.setSubtaskStatus(subtask1, TaskStatus.DONE);
        taskManager.setSubtaskStatus(subtask2, TaskStatus.DONE);

        if (subtask1.getStatus() == TaskStatus.DONE
                && subtask2.getStatus() == TaskStatus.DONE
                && epic1.getStatus() == TaskStatus.DONE) {
            System.out.println("Статус успешно изменен");
        }

        System.out.println("\nПробуем удалить сабтаск 3...");
        taskManager.removeSubtask(subtask3.getUid());
        if (!taskManager.getSubtasks().contains(subtask3)) {
            System.out.println("Удаление успешно");
        };

        System.out.println("\nПробуем удалить епик 2...");
        taskManager.removeEpic(epic2.getUid());
        if (!taskManager.getEpics().contains(epic2)) {
            System.out.println("Удаление успешно");
        };
    }
}
