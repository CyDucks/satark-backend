package org.cyducks.satark.model;

import lombok.Data;

import java.util.List;

@Data
public class RouteRequest {
    private Coordinate origin;
    private Coordinate destination;
    private List<Coordinate> ignoreBounds;
}
