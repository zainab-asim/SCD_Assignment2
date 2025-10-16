package com.example;

import javax.swing.*;
import java.awt.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
public class GanttPanel extends JPanel {
    private Project project;
    public GanttPanel(Project p){ this.project = p; setPreferredSize(new Dimension(900,400)); }
    public void setProject(Project p){ this.project = p; repaint(); }
    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        if(project==null) return;
        LocalDateTime start = project.projectStart();
        LocalDateTime end = project.projectEnd();
        if(start==null||end==null) return;
        long totalMins = Duration.between(start,end).toMinutes();
        int w = getWidth()-120;
        int y = 30;
        g.drawString("Gantt view",10,15);
        for(Task t: project.getAllTasks()){
            long sOff = Duration.between(start,t.getStart()).toMinutes();
            long len = Duration.between(t.getStart(),t.getEnd()).toMinutes();
            int x = 100 + (int)((sOff*1.0/totalMins)*w);
            int width = Math.max(4,(int)((len*1.0/totalMins)*w));
            g.setColor(new Color(100,150,240));
            g.fillRect(x,y,width,20);
            g.setColor(Color.BLACK);
            g.drawRect(x,y,width,20);
            g.drawString(t.getId()+": "+t.getTitle(),10,y+15);
            y += 30;
        }
    }
}
