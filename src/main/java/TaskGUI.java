import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

public class TaskGUI extends JFrame {

    private DefaultTableModel taskTableModel;
    private JTable taskTable;
    private JComboBox<String> categoryComboBox; // Category drop-down

    public TaskGUI() {
        // Initialize the database (ensures tables are created if not present)
        try {
            SQLiteHandler.initDatabase();  // This ensures the tasks table is created if not already present
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database initialization failed: " + e.getMessage());
            System.exit(1);
        }

        setTitle("Task Manager");
        setSize(850, 610);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create the table model and table
        taskTableModel = new DefaultTableModel(new String[]{"ID", "Task", "Category", "Completed"}, 0);
        taskTable = new JTable(taskTableModel);
        JScrollPane tableScrollPane = new JScrollPane(taskTable);
        add(tableScrollPane, BorderLayout.CENTER);

        // Hide the ID column
        TableColumnModel tcm = taskTable.getColumnModel();
        tcm.removeColumn(tcm.getColumn(0));

        // Create top panel for controls
        JPanel controlPanel = new JPanel(new FlowLayout());

        // Category combo box (drop-down list)
        categoryComboBox = new JComboBox<>(new String[]{"All", "Work", "Personal", "Other", "Miscellaneous"});
        controlPanel.add(new JLabel("Filter by Category:"));
        controlPanel.add(categoryComboBox);

        // Filter button
        JButton filterButton = new JButton("Filter");
        filterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                filterTasksByCategory((String) categoryComboBox.getSelectedItem());
            }
        });
        controlPanel.add(filterButton);

        // Create task button
        JButton createTaskButton = new JButton("Create Task");
        createTaskButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createTask();
            }
        });
        controlPanel.add(createTaskButton);

        // Mark task as completed button
        JButton markCompletedButton = new JButton("Mark Completed");
        markCompletedButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                markTaskAsCompleted();
            }
        });
        controlPanel.add(markCompletedButton);

        // Export button (drop-down for CSV and .txt)
        JComboBox<String> exportComboBox = new JComboBox<>(new String[]{"Export as CSV", "Export as TXT"});
        JButton exportButton = new JButton("Export");
        exportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exportTasks((String) exportComboBox.getSelectedItem());
            }
        });
        controlPanel.add(exportComboBox);
        controlPanel.add(exportButton);

        // Import button
        JButton importButton = new JButton("Import CSV");
        importButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                importTasks();
            }
        });
        controlPanel.add(importButton);

        add(controlPanel, BorderLayout.NORTH);

        // Enable drag-and-drop import
        new DropTarget(this, new DropTargetListener() {
            @Override
            public void dragEnter(DropTargetDragEvent dtde) {}

            @Override
            public void dragOver(DropTargetDragEvent dtde) {}

            @Override
            public void dropActionChanged(DropTargetDragEvent dtde) {}

            @Override
            public void dragExit(DropTargetEvent dte) {}

            @Override
            public void drop(DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    List<File> droppedFiles = (List<File>) dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    for (File file : droppedFiles) {
                        importTasksFromCSV(file);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        loadTasksFromDatabase(); // Load initial tasks from the database
        setVisible(true);
    }

    private void loadTasksFromDatabase() {
        try {
            ResultSet rs = SQLiteHandler.getTasks();  // Retrieve tasks from the SQLite database
            taskTableModel.setRowCount(0);  // Clear existing data
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("task_name"));
                row.add(rs.getString("category"));
                row.add(rs.getBoolean("completed"));
                taskTableModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void filterTasksByCategory(String category) {
        if (category.equals("All")) {
            loadTasksFromDatabase();
        } else {
            try {
                ResultSet rs = SQLiteHandler.getTasksByCategory(category);  // Filter tasks by category in the SQLite database
                taskTableModel.setRowCount(0);  // Clear existing data
                while (rs.next()) {
                    Vector<Object> row = new Vector<>();
                    row.add(rs.getInt("id"));
                    row.add(rs.getString("task_name"));
                    row.add(rs.getString("category"));
                    row.add(rs.getBoolean("completed"));
                    taskTableModel.addRow(row);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void createTask() {
        String taskName = JOptionPane.showInputDialog("Enter Task Name:");
        if (taskName != null && !taskName.trim().isEmpty()) {
            String category = (String) JOptionPane.showInputDialog(null, "Select Category", "Category", JOptionPane.QUESTION_MESSAGE, null, new String[]{"Work", "Personal", "Other", "Miscellaneous"}, "Work");
            try {
                boolean isCompleted = false;
                SQLiteHandler.addTask(taskName, category, isCompleted);  // Add task to the SQLite database
                loadTasksFromDatabase();  // Refresh the task list
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void markTaskAsCompleted() {
        int selectedRow = taskTable.getSelectedRow();
        if (selectedRow != -1) {
            int taskId = (int) taskTableModel.getValueAt(selectedRow, 0);
            try {
                SQLiteHandler.markTaskAsCompleted(taskId);  // Mark the task as completed in the SQLite database
                loadTasksFromDatabase();  // Refresh the task list
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a task to mark as completed.");
        }
    }

    private void exportTasks(String format) {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (FileWriter writer = new FileWriter(file)) {
                if (format.equals("Export as CSV")) {
                    for (int i = 0; i < taskTableModel.getRowCount(); i++) {
                        for (int j = 0; j < taskTableModel.getColumnCount(); j++) {
                            writer.write(taskTableModel.getValueAt(i, j) + (j == taskTableModel.getColumnCount() - 1 ? "\n" : ","));
                        }
                    }
                } else if (format.equals("Export as TXT")) {
                    for (int i = 0; i < taskTableModel.getRowCount(); i++) {
                        for (int j = 0; j < taskTableModel.getColumnCount(); j++) {
                            writer.write(taskTableModel.getValueAt(i, j) + "\t");
                        }
                        writer.write("\n");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void importTasks() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            importTasksFromCSV(file);
        }
    }

    private void importTasksFromCSV(File file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length >= 3) {
                    int importedId = Integer.parseInt(values[0]);
                    String taskName = values[1];
                    String category = values[2];
                    boolean isCompleted = values.length > 3 && Boolean.parseBoolean(values[3]);

                    // Check if task with the same ID already exists
                    if (SQLiteHandler.taskExists(importedId)) {
                        int choice = JOptionPane.showOptionDialog(
                                this,
                                "Task with ID " + importedId + " already exists. What do you want to do?",
                                "Duplicate ID",
                                JOptionPane.YES_NO_CANCEL_OPTION,
                                JOptionPane.QUESTION_MESSAGE,
                                null,
                                new String[]{"Merge (Create New)", "Replace", "Cancel"},
                                "Merge");

                        if (choice == 0) { // Merge (Create New)
                            SQLiteHandler.addTask(taskName, category, isCompleted); // Task is added with a new auto-generated ID
                        } else if (choice == 1) { // Replace
                            SQLiteHandler.updateTask(importedId, taskName, category, isCompleted); // Replace existing task
                        }
                    } else {
                        // If no conflict, add task as normal
                        SQLiteHandler.addTask(taskName, category, isCompleted);
                    }
                }
            }
            loadTasksFromDatabase();  // Refresh the task list
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new TaskGUI();
    }
}