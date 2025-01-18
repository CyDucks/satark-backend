package org.cyducks.satark.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RouteResponse {
    private String encodedPolyline;
    private double totalDistance;
    private double zoneIntersectionDistance;

}
