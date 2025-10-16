package com.example;

import javax.swing.table.AbstractTableModel;
import java.util.*;
public class ResourceTableModel extends AbstractTableModel {
    private List<Resource> resources = new ArrayList<>();
    private String[] cols = {"Resource","Allocations (task:%)"};
    public void setResources(Collection<Resource> r){ resources = new ArrayList<>(r); fireTableDataChanged(); }
    public int getRowCount(){ return resources.size(); }
    public int getColumnCount(){ return cols.length; }
    public String getColumnName(int c){ return cols[c]; }
    public Object getValueAt(int r,int c){
        Resource res = resources.get(r);
        if(c==0) return res.getName();
        StringBuilder sb = new StringBuilder();
        for(Allocation a: res.getAllocations()){
            if(sb.length()>0) sb.append(", ");
            sb.append(a.getTaskId()).append(":").append((int)a.getPercent());
        }
        return sb.toString();
    }
}
