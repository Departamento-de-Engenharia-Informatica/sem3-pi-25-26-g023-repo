package pt.ipp.isep.dei.domain;

import java.time.LocalDateTime;
import java.util.List;

public class PickingPlan {
    private String planId;
    private LocalDateTime createdDate;
    private double trolleyCapacity;
    //     private HeuristicType heuristic;
    //     private SplitPolicy splitPolicy;
    private List<Trolley> trolleys;
    private double totalUtilization;
}
