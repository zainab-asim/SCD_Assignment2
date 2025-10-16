package com.example;

import javax.swing.table.AbstractTableModel;
import java.time.format.DateTimeFormatter;
import java.util.*;
public class TaskTableModel extends AbstractTableModel {
    private List<Task> tasks = new ArrayList<>();
    private String[] cols = {"Id","Task","Start","End","Dependencies"};
    private DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    public void setTasks(Collection<Task> t){ tasks = new ArrayList<>(t); fireTableDataChanged(); }
    public Task getTaskAt(int r){ return tasks.get(r); }
    public int getRowCount(){ return tasks.size(); }
    public int getColumnCount(){ return cols.length; }
    public String getColumnName(int c){ return cols[c]; }
    public Object getValueAt(int r,int c){
        Task t = tasks.get(r);
        switch(c){
            case 0: return t.getId();
            case 1: return t.getTitle();
            case 2: return t.getStart().format(fmt);
            case 3: return t.getEnd().format(fmt);
            case 4: return t.getDependencies().toString();
        }
        return "";
    }
}
