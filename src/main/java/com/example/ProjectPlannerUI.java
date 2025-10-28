package com.example;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ProjectPlannerUI extends JFrame {
    private Project project = new Project();
    private TaskTableModel taskModel = new TaskTableModel();
    private ResourceTableModel resourceModel = new ResourceTableModel();
    private JTable taskTable = new JTable(taskModel);
    private JTable resourceTable = new JTable(resourceModel);
    private JTextArea analysisArea = new JTextArea(8, 60);
    private GanttPanel gantt = new GanttPanel(project);

    public ProjectPlannerUI() {
        setTitle("Project Planner");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 🔹 Top toolbar
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton newBtn = new JButton("New");
        JButton saveBtn = new JButton("Save");
        JButton closeBtn = new JButton("Close");
        JButton uploadTasks = new JButton("Upload Tasks");
        JButton uploadResources = new JButton("Upload Resources");
        JButton analyzeBtn = new JButton("Analyze");
        JButton visualizeBtn = new JButton("Visualize");
        JButton addTaskBtn = new JButton("Add Task"); // 🆕 Moved here

        top.add(newBtn);
        top.add(saveBtn);
        top.add(closeBtn);
        top.add(addTaskBtn);
        top.add(new JLabel(" Project: "));
        top.add(new JTextField(20));
        top.add(uploadTasks);
        top.add(uploadResources);
        top.add(analyzeBtn);
        top.add(visualizeBtn);
        add(top, BorderLayout.NORTH);

        // 🔹 Split panel for tables + analysis/gantt
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        JPanel tables = new JPanel(new GridLayout(1, 2));
        tables.add(new JScrollPane(taskTable));
        tables.add(new JScrollPane(resourceTable));
        split.setTopComponent(tables);

        JPanel lower = new JPanel(new BorderLayout());
        lower.add(new JScrollPane(analysisArea), BorderLayout.CENTER);
        lower.add(gantt, BorderLayout.SOUTH);
        gantt.setVisible(false);
        analysisArea.setVisible(false);
        split.setBottomComponent(lower);
        split.setDividerLocation(250);

        add(split, BorderLayout.CENTER);

        // 🔹 Button actions
        newBtn.addActionListener(e -> {
            project.clear();
            refreshAll();
            JOptionPane.showMessageDialog(this, "All Data Cleared Successfully!");
        });

        closeBtn.addActionListener(e -> {
            dispose();
            JOptionPane.showMessageDialog(this, "Project closed successfully!");
        });

        uploadTasks.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    List<String> lines = ProjectParser.readLines(fc.getSelectedFile().getAbsolutePath());
                    ProjectParser.parseTasks(project, lines);
                    refreshAll();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage());
                }
            }
        });

        uploadResources.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    List<String> lines = ProjectParser.readLines(fc.getSelectedFile().getAbsolutePath());
                    ProjectParser.parseResources(project, lines);
                    refreshAll();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage());
                }
            }
        });

        saveBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Select folder to save project files");
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    String folder = fc.getSelectedFile().getAbsolutePath();
                    ProjectParser.saveTasks(project, folder + "/tasks_saved.txt");
                    ProjectParser.saveResources(project, folder + "/resources_saved.txt");
                    JOptionPane.showMessageDialog(this, "Project saved successfully!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error saving files: " + ex.getMessage());
                }
            }
        });

        analyzeBtn.addActionListener(e -> doAnalyze());
        visualizeBtn.addActionListener(e -> {
            gantt.setProject(project);
            gantt.repaint();
            gantt.setVisible(true);
            analysisArea.setVisible(false);
        });

        // 🆕 Add Task button handler - opens form
        addTaskBtn.addActionListener(e -> openAddTaskDialog());

        setSize(1100, 700);
        setLocationRelativeTo(null);
        setVisible(true);
    }
    private void openAddTaskDialog() {
        JDialog dialog = new JDialog(this, "Add New Task", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel idLabel = new JLabel("Task ID:");
        JLabel titleLabel = new JLabel("Title:");
        JLabel startLabel = new JLabel("Start (yyyyMMddHHmm):");
        JLabel endLabel = new JLabel("End (yyyyMMddHHmm):");
        JLabel depLabel = new JLabel("Dependencies:");

        JTextField idField = new JTextField(10);
        JTextField titleField = new JTextField(15);
        JTextField startField = new JTextField(15);
        JTextField endField = new JTextField(15);

        // ✅ Dependencies dropdown (multi-select)
        DefaultListModel<String> depModel = new DefaultListModel<>();
        for (Task t : project.getAllTasks()) {
            depModel.addElement(t.getId() + " - " + t.getTitle());
        }
        JList<String> depList = new JList<>(depModel);
        depList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane depScroll = new JScrollPane(depList);
        depScroll.setPreferredSize(new Dimension(180, 80));

        JButton addBtn = new JButton("Add Task");

        gbc.gridx = 0; gbc.gridy = 0; dialog.add(idLabel, gbc);
        gbc.gridx = 1; dialog.add(idField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; dialog.add(titleLabel, gbc);
        gbc.gridx = 1; dialog.add(titleField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; dialog.add(startLabel, gbc);
        gbc.gridx = 1; dialog.add(startField, gbc);
        gbc.gridx = 0; gbc.gridy = 3; dialog.add(endLabel, gbc);
        gbc.gridx = 1; dialog.add(endField, gbc);
        gbc.gridx = 0; gbc.gridy = 4; dialog.add(depLabel, gbc);
        gbc.gridx = 1; dialog.add(depScroll, gbc);
        gbc.gridx = 1; gbc.gridy = 5; dialog.add(addBtn, gbc);

        addBtn.addActionListener(e -> {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                String title = titleField.getText().trim();
                String startRaw = startField.getText().trim();
                String endRaw = endField.getText().trim();

                if (title.isEmpty() || startRaw.isEmpty() || endRaw.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Please fill all required fields.");
                    return;
                }

                // ✅ Duplicate ID check
                for (Task existing : project.getAllTasks()) {
                    if (existing.getId() == id) {
                        JOptionPane.showMessageDialog(dialog, "A task with this ID already exists. Please choose another ID.");
                        return;
                    }
                }

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
                LocalDateTime start = LocalDateTime.parse(startRaw, formatter);
                LocalDateTime end = LocalDateTime.parse(endRaw, formatter);

                if (end.isBefore(start)) {
                    JOptionPane.showMessageDialog(dialog, "End time must be after start time.");
                    return;
                }

                // ✅ Collect selected dependencies
                List<Integer> deps = new ArrayList<>();
                for (String s : depList.getSelectedValuesList()) {
                    deps.add(Integer.parseInt(s.split(" - ")[0]));
                }

                Task t = new Task(id, title, startRaw, endRaw, deps);
                project.addTask(t);
                refreshAll();
                JOptionPane.showMessageDialog(dialog, "Task added successfully!");
                dialog.dispose();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
            }
        });

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }



    private void refreshAll() {
        taskModel.setTasks(project.getAllTasks());
        resourceModel.setResources(project.getAllResources());
        analysisArea.setText("");
        gantt.setProject(project);
        gantt.repaint();
        gantt.setVisible(false);
        analysisArea.setVisible(false);
    }

    private void doAnalyze() {
        StringBuilder sb = new StringBuilder();
        sb.append("Project start: ").append(project.projectStart()).append("\n");
        sb.append("Project end: ").append(project.projectEnd()).append("\n");
        long mins = project.projectDurationMinutes();
        long days = mins / (60 * 24);
        long hours = (mins % (60 * 24)) / 60;
        long rem = mins % 60;
        sb.append("Duration: ").append(days).append(" days, ").append(hours)
                .append(" hours, ").append(rem).append(" minutes\n\n");

        List<List<Integer>> cycles = project.findCycles();

        if (!cycles.isEmpty()) {
            sb.append("⚠️ Dependency cycle detected!\n");
            for (List<Integer> cycle : cycles) {
                sb.append("Cycle: ");
                for (int i = 0; i < cycle.size(); i++) {
                    sb.append("Task ").append(cycle.get(i));
                    if (i < cycle.size() - 1) sb.append(" → ");
                }
                sb.append("\n");
            }
        } else {
            sb.append("Overlapping tasks:\n");
            var overlaps = project.findOverlaps();
            if (overlaps.isEmpty()) sb.append("None\n");
            else for (String s : overlaps) sb.append(s).append("\n");

            sb.append("\nTeams per task:\n");
            for (Task t : project.getAllTasks()) {
                var team = project.teamForTask(t.getId());
                sb.append("Task ").append(t.getId()).append(": ");
                if (team.isEmpty()) sb.append("No resources\n");
                else {
                    for (int i = 0; i < team.size(); i++) {
                        sb.append(team.get(i).getName());
                        if (i < team.size() - 1) sb.append(", ");
                    }
                    sb.append("\n");
                }
            }
        }

        analysisArea.setVisible(true);
        analysisArea.setText(sb.toString());
        analysisArea.setLineWrap(true);
        analysisArea.setWrapStyleWord(true);
        gantt.setVisible(false);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ProjectPlannerUI::new);
    }
}