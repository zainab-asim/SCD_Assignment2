package com.example;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
public class Task {
    private int id;
    private String title;
    private LocalDateTime start;
    private LocalDateTime end;
    private List<Integer> dependencies = new ArrayList<>();
    private static DateTimeFormatter parser = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
    public Task(int id, String title, String startRaw, String endRaw, List<Integer> deps){
        this.id = id;
        this.title = title.trim();
        this.start = parseDateTime(startRaw.trim());
        this.end = parseDateTime(endRaw.trim());
        if(deps!=null) this.dependencies.addAll(deps);
    }
    private LocalDateTime parseDateTime(String s){
        String cleaned = s.replace("+","").replace("-","");
        if(cleaned.length()==8) cleaned = cleaned + "0000";
        return LocalDateTime.parse(cleaned, parser);
    }
    public int getId(){ return id; }
    public String getTitle(){ return title; }
    public LocalDateTime getStart(){ return start; }
    public LocalDateTime getEnd(){ return end; }
    public List<Integer> getDependencies(){ return dependencies; }
    public long getDurationMinutes(){ return Duration.between(start,end).toMinutes(); }
    public boolean overlapsWith(Task other){
        LocalDateTime a1 = start, a2 = end, b1 = other.start, b2 = other.end;
        LocalDateTime maxStart = a1.isAfter(b1)? a1: b1;
        LocalDateTime minEnd = a2.isBefore(b2)? a2: b2;
        return maxStart.isBefore(minEnd);
    }
    public String getStartFormatted() {
        return start.toString().replace("T", "+");
    }

    public String getEndFormatted() {
        return end.toString().replace("T", "+");
    }

}