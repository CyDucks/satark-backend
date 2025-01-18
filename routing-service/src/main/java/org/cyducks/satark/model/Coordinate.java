package org.cyducks.satark.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Coordinate implements Serializable {
    private double latitude;
    private double longitude;

    public Coordinate(org.locationtech.jts.geom.Coordinate coordinate) {
        this.latitude = coordinate.getY();
        this.longitude = coordinate.getX();
    }

    public Coordinate(String string) {
        String[] split = string.split(",");
        this.latitude = Double.parseDouble(split[0]);
        this.longitude = Double.parseDouble(split[1]);
    }

    /**
     * @param other the coordinate whose distance is to be calculated
     * @return distance between this and other point
     */
    public double distanceTo(Coordinate other) {
        final int R = 6371;

        double thisLat = Math.toRadians(this.latitude);
        double otherLat = Math.toRadians(other.latitude);
        double thisLon = Math.toRadians(this.longitude);
        double otherLon = Math.toRadians(other.longitude);

        double dLat = otherLat - thisLat;
        double dLon = otherLon - thisLon;

        double angle = Math.pow(Math.sin(dLat/2),2) + Math.cos(thisLat) * Math.cos(otherLat) * Math.pow(Math.sin(dLon/2),2);
        double c = 2 * Math.atan2(Math.sqrt(angle), Math.sqrt(1 - angle));

        return R * c;
    }

    public org.locationtech.jts.geom.Coordinate getGeometricInstance() {
        return new org.locationtech.jts.geom.Coordinate(this.longitude, this.latitude);
    }
}
