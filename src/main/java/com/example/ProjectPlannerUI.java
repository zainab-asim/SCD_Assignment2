package com.example;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
public class ProjectPlannerUI extends JFrame {
    private Project project = new Project();
    private TaskTableModel taskModel = new TaskTableModel();
    private ResourceTableModel resourceModel = new ResourceTableModel();
    private JTable taskTable = new JTable(taskModel);
    private JTable resourceTable = new JTable(resourceModel);
    private JTextArea analysisArea = new JTextArea(8,60);

    private GanttPanel gantt = new GanttPanel(project);
    public ProjectPlannerUI(){
        setTitle("Project Planner");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton newBtn = new JButton("New");
        JButton saveBtn = new JButton("Save");
        JButton closeBtn = new JButton("Close");
        JButton uploadTasks = new JButton("Upload Tasks");
        JButton uploadResources = new JButton("Upload Resources");
        JButton analyzeBtn = new JButton("Analyze");
        JButton visualizeBtn = new JButton("Visualize");
        top.add(newBtn); top.add(saveBtn); top.add(closeBtn);
        top.add(new JLabel(" Project: "));
        top.add(new JTextField(20));
        top.add(uploadTasks); top.add(uploadResources); top.add(analyzeBtn); top.add(visualizeBtn);
        add(top, BorderLayout.NORTH);
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        JPanel tables = new JPanel(new GridLayout(1,2));
        tables.add(new JScrollPane(taskTable));
        tables.add(new JScrollPane(resourceTable));
        split.setTopComponent(tables);
        JPanel lower = new JPanel(new BorderLayout());
        lower.add(new JScrollPane(analysisArea), BorderLayout.CENTER);
        lower.add(gantt, BorderLayout.SOUTH);
        gantt.setVisible(false);
        analysisArea.setVisible(false);
        split.setBottomComponent(lower);
        split.setDividerLocation(220);
        add(split, BorderLayout.CENTER);
        newBtn.addActionListener(e->{ project.clear(); refreshAll();
            JOptionPane.showMessageDialog(this, "All Data Cleared Successfully!");});
        closeBtn.addActionListener(e->{dispose();
            JOptionPane.showMessageDialog(this, "Project closed successfully!");});
        uploadTasks.addActionListener(e->{
            JFileChooser fc = new JFileChooser();
            if(fc.showOpenDialog(this)==JFileChooser.APPROVE_OPTION){
                try{
                    List<String> lines = ProjectParser.readLines(fc.getSelectedFile().getAbsolutePath());
                    ProjectParser.parseTasks(project, lines);
                    refreshAll();
                }catch(Exception ex){ JOptionPane.showMessageDialog(this,ex.getMessage()); }
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

        uploadResources.addActionListener(e->{
            JFileChooser fc = new JFileChooser();
            if(fc.showOpenDialog(this)==JFileChooser.APPROVE_OPTION){
                try{
                    List<String> lines = ProjectParser.readLines(fc.getSelectedFile().getAbsolutePath());
                    ProjectParser.parseResources(project, lines);
                    refreshAll();
                }catch(Exception ex){ JOptionPane.showMessageDialog(this,ex.getMessage()); }
            }
        });
        analyzeBtn.addActionListener(e->doAnalyze());
        visualizeBtn.addActionListener(e->{ gantt.setProject(project); gantt.repaint();gantt.setVisible(true);analysisArea.setVisible(false); });
        setSize(1100,700);
        setLocationRelativeTo(null);
        setVisible(true);
    }
    private void refreshAll(){
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
        sb.append("Duration: ").append(days).append(" days, ").append(hours).append(" hours, ").append(rem).append(" minutes\n\n");

        // Check for dependency cycles
        List<List<Integer>> cycles = project.findCycles();

        if (!cycles.isEmpty()) {
            sb.append("⚠Dependency cycle detected!\n");
            for (List<Integer> cycle : cycles) {
                sb.append("Cycle: ");
                for (int i = 0; i < cycle.size(); i++) {
                    sb.append("Task ").append(cycle.get(i));
                    if (i < cycle.size() - 1) sb.append(" → ");
                }
                sb.append("\n");
            }
            sb.append("\nTeams per task and total effort cannot be calculated due to dependency cycle.\n");
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

            sb.append("\nTotal effort per resource (hours):\n");
            var efforts = project.totalEffortPerResourceHours();
            if (efforts.isEmpty()) sb.append("No resources\n");
            else for (var en : efforts.entrySet())
                sb.append(en.getKey()).append(": ").append(String.format("%.2f", en.getValue())).append("\n");
        }

        analysisArea.setSize(1100, 100);
        analysisArea.setVisible(true);
        analysisArea.setText(sb.toString());
        analysisArea.setLineWrap(true);
        analysisArea.setWrapStyleWord(true);
        gantt.setVisible(false);
    }

    public static void main(String[] args){
        SwingUtilities.invokeLater(ProjectPlannerUI::new);
    }
}