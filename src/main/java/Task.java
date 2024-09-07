public class Task {
    private int id;
    private String taskName;
    private String category;
    private boolean isCompleted;

    // Constructor for Task
    public Task(int id, String taskName, String category, boolean isCompleted) {
        this.id = id;
        this.taskName = taskName;
        this.category = category;
        this.isCompleted = isCompleted;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    // Task details as a String (for export purposes or printing)
    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", taskName='" + taskName + '\'' +
                ", category='" + category + '\'' +
                ", isCompleted=" + isCompleted +
                '}';
    }
}