package com.example;

import java.util.ArrayList;
import java.util.List;
public class Resource {
    private String name;
    private List<Allocation> allocations = new ArrayList<>();
    public Resource(String name){ this.name = name.trim(); }
    public String getName(){ return name; }
    public void addAllocation(Allocation a){ allocations.add(a); }
    public List<Allocation> getAllocations(){ return allocations; }
    public double totalEffortHours(Project p){
        double hours = 0.0;
        for(Allocation a: allocations){
            Task t = p.getTaskById(a.getTaskId());
            if(t!=null){
                double taskHours = t.getDurationMinutes()/60.0;
                hours += taskHours * (a.getPercent()/100.0);
            }
        }
        return hours;
    }
}
