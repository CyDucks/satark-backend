package org.cyducks.satark.model;

import lombok.Data;

@Data
public class RoutingNode {
    private Coordinate destination;
    private double distance;
}
