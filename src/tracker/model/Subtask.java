package tracker.model;

public class Subtask extends Task {
    private final int epicId;

    public Subtask(String title, String description, int id, int epicId) {
        super(title, description, id);
        if (id == epicId) {
            throw new IllegalArgumentException("ID Epic и Subtask должны отличаться");
        }
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "epicId=" + epicId +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", id=" + id +
                '}';
    }
}
