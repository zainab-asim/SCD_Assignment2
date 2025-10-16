package com.example;
public class Allocation {
    private int taskId;
    private double percent;
    public Allocation(int taskId, double percent){ this.taskId = taskId; this.percent = percent; }
    public int getTaskId(){ return taskId; }
    public double getPercent(){ return percent; }
}
