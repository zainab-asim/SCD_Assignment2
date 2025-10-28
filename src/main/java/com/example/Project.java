package com.example;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class Project {
    private Map<Integer, Task> tasks = new LinkedHashMap<>();
    private Map<String, Resource> resources = new LinkedHashMap<>();

    public void addTask(Task t) { tasks.put(t.getId(), t); }
    public void addResource(Resource r) { resources.put(r.getName(), r); }
    public Task getTaskById(int id) { return tasks.get(id); }
    public Collection<Task> getAllTasks() { return tasks.values(); }
    public Collection<Resource> getAllResources() { return resources.values(); }
    public void clear() { tasks.clear(); resources.clear(); }

    public LocalDateTime projectStart() {
        LocalDateTime min = null;
        for (Task t : tasks.values())
            if (min == null || t.getStart().isBefore(min)) min = t.getStart();
        return min;
    }

    public LocalDateTime projectEnd() {
        LocalDateTime max = null;
        for (Task t : tasks.values())
            if (max == null || t.getEnd().isAfter(max)) max = t.getEnd();
        return max;
    }

    public long projectDurationMinutes() {
        LocalDateTime s = projectStart();
        LocalDateTime e = projectEnd();
        if (s == null || e == null) return 0;
        return Duration.between(s, e).toMinutes();
    }

    // Regular Overlap Detection (unchanged)
    public List<String> findOverlaps() {
        List<String> res = new ArrayList<>();
        for (Task t : tasks.values()) {
            for (Integer depId : t.getDependencies()) {
                Task d = tasks.get(depId);
                if (d == null) continue;
                if (t.overlapsWith(d)) {
                    LocalDateTime overlapStart = t.getStart().isAfter(d.getStart()) ? t.getStart() : d.getStart();
                    LocalDateTime overlapEnd = t.getEnd().isBefore(d.getEnd()) ? t.getEnd() : d.getEnd();
                    if (overlapStart.isBefore(overlapEnd)) {
                        res.add("Task " + t.getId() + " overlaps with dependency " + d.getId()
                                + " from " + overlapStart + " to " + overlapEnd);
                    }
                }
            }
        }
        return res;
    }

    public List<List<Integer>> findCycles() {
        List<List<Integer>> cycles = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();
        Set<Integer> stack = new HashSet<>();
        LinkedList<Integer> path = new LinkedList<>();

        for (Task t : tasks.values()) {
            if (!visited.contains(t.getId())) {
                dfsCycle(t.getId(), visited, stack, path, cycles);
            }
        }
        return cycles;
    }

    private void dfsCycle(int current, Set<Integer> visited, Set<Integer> stack,
                          LinkedList<Integer> path, List<List<Integer>> cycles) {
        visited.add(current);
        stack.add(current);
        path.add(current);

        Task t = tasks.get(current);
        if (t != null) {
            for (int dep : t.getDependencies()) {
                if (!visited.contains(dep)) {
                    dfsCycle(dep, visited, stack, path, cycles);
                } else if (stack.contains(dep)) {
                    // Found a cycle
                    List<Integer> cycle = new ArrayList<>();
                    Iterator<Integer> it = path.descendingIterator();
                    while (it.hasNext()) {
                        int id = it.next();
                        cycle.add(0, id);
                        if (id == dep) break;
                    }
                    cycles.add(cycle);
                }
            }
        }

        stack.remove(current);
        path.removeLast();
    }

    public List<Resource> teamForTask(int taskId) {
        List<Resource> team = new ArrayList<>();
        for (Resource r : resources.values()) {
            for (Allocation a : r.getAllocations()) {
                if (a.getTaskId() == taskId) {
                    team.add(r);
                    break;
                }
            }
        }
        return team;
    }

    public Map<String, Double> totalEffortPerResourceHours() {
        Map<String, Double> map = new LinkedHashMap<>();
        for (Resource r : resources.values())
            map.put(r.getName(), r.totalEffortHours(this));
        return map;
    }
}