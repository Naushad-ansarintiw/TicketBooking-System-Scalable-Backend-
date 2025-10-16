package org.example.entities;

import java.util.*;
import java.time.*;

public class Train{
    private String trainId; 
    private String trainNo; 
    private List<List<Integer>> seats; 
    private Map<String, LocalDateTime> stationTimes;
    private List<String> stations;
}