package com.example;


import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
public class Project {
    private Map<Integer, Task> tasks = new LinkedHashMap<>();
    private Map<String, Resource> resources = new LinkedHashMap<>();
    public void addTask(Task t){ tasks.put(t.getId(), t); }
    public void addResource(Resource r){ resources.put(r.getName(), r); }
    public Task getTaskById(int id){ return tasks.get(id); }
    public Collection<Task> getAllTasks(){ return tasks.values(); }
    public Collection<Resource> getAllResources(){ return resources.values(); }
    public void clear(){ tasks.clear(); resources.clear(); }
    public LocalDateTime projectStart(){
        LocalDateTime min = null;
        for(Task t: tasks.values()) if(min==null || t.getStart().isBefore(min)) min = t.getStart();
        return min;
    }
    public LocalDateTime projectEnd(){
        LocalDateTime max = null;
        for(Task t: tasks.values()) if(max==null || t.getEnd().isAfter(max)) max = t.getEnd();
        return max;
    }
    public long projectDurationMinutes(){
        LocalDateTime s = projectStart(); LocalDateTime e = projectEnd();
        if(s==null||e==null) return 0;
        return Duration.between(s,e).toMinutes();
    }
    public List<String> findOverlaps(){
        List<String> res = new ArrayList<>();
        for(Task t: tasks.values()){
            for(Integer depId: t.getDependencies()){
                Task d = tasks.get(depId);
                if(d==null) continue;
                if(t.overlapsWith(d)){
                    LocalDateTime overlapStart = t.getStart().isAfter(d.getStart())? t.getStart(): d.getStart();
                    LocalDateTime overlapEnd = t.getEnd().isBefore(d.getEnd())? t.getEnd(): d.getEnd();
                    if(overlapStart.isBefore(overlapEnd)){
                        res.add("Task " + t.getId() + " overlaps with dependency " + d.getId() + " from " + overlapStart + " to " + overlapEnd);
                    }
                }
            }
        }
        return res;
    }
    public List<Resource> teamForTask(int taskId){
        List<Resource> team = new ArrayList<>();
        for(Resource r: resources.values()){
            for(Allocation a: r.getAllocations()){
                if(a.getTaskId()==taskId){ team.add(r); break; }
            }
        }
        return team;
    }
    public Map<String, Double> totalEffortPerResourceHours(){
        Map<String, Double> map = new LinkedHashMap<>();
        for(Resource r: resources.values()) map.put(r.getName(), r.totalEffortHours(this));
        return map;
    }
}