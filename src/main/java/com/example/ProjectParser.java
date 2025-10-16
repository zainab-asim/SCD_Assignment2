package com.example;


import java.nio.file.*;
import java.util.*;
public class ProjectParser {
    public static List<String> readLines(String path) throws Exception{
        return Files.readAllLines(Paths.get(path));
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