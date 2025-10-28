package com.example;


import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.*;
import java.util.*;
public class ProjectParser {
    public static List<String> readLines(String path) throws Exception{
        return Files.readAllLines(Paths.get(path));
    }
    public static void saveTasks(Project project, String filePath) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath))) {
            for (Task t : project.getAllTasks()) {
                String deps = String.join(", ", t.getDependencies().stream().map(String::valueOf).toList());
                pw.printf("%d, %s, %s, %s, %s%n",
                        t.getId(), t.getTitle(),
                        t.getStartFormatted(), t.getEndFormatted(), deps);
            }
        }
    }

    public static void saveResources(Project project, String filePath) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath))) {
            for (Resource r : project.getAllResources()) {
                StringBuilder sb = new StringBuilder(r.getName()).append(", ");
                var allocations = r.getAllocations();
                for (int i = 0; i < allocations.size(); i++) {
                    var a = allocations.get(i);
                    sb.append(a.getTaskId()).append(":").append(a.getPercent());
                    if (i < allocations.size() - 1) sb.append(", ");
                }
                pw.println(sb);
            }
        }
    }

    public static void parseTasks(Project p, List<String> lines){
        for(String raw: lines){
            String line = raw.trim();
            if(line.isEmpty()||line.startsWith("#")) continue;
            String[] parts = line.split(",");
            if(parts.length<4) continue;
            int id = Integer.parseInt(parts[0].trim());
            String title = parts[1].trim();
            String start = parts[2].trim();
            String end = parts[3].trim();
            List<Integer> deps = new ArrayList<>();
            if(parts.length>4){
                for(int i=4;i<parts.length;i++){
                    String d = parts[i].trim();
                    if(!d.isEmpty()) deps.add(Integer.parseInt(d));
                }
            }
            p.addTask(new Task(id,title,start,end,deps));
        }
    }
    public static void parseResources(Project p, List<String> lines){
        for(String raw: lines){
            String line = raw.trim();
            if(line.isEmpty()||line.startsWith("#")) continue;
            String[] parts = line.split(",");
            if(parts.length<1) continue;
            Resource r = new Resource(parts[0].trim());
            for(int i=1;i<parts.length;i++){
                String tok = parts[i].trim();
                if(tok.isEmpty()) continue;
                if(!tok.contains(":")) continue;
                String[] kv = tok.split(":");
                int tid = Integer.parseInt(kv[0].trim());
                double pct = Double.parseDouble(kv[1].trim());
                r.addAllocation(new Allocation(tid,pct));
            }
            p.addResource(r);
        }
    }
}