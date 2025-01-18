package org.cyducks.satark.service;

import lombok.RequiredArgsConstructor;
import org.cyducks.satark.model.Coordinate;
import org.cyducks.satark.model.RouteRequest;
import org.cyducks.satark.model.RouteResponse;
import org.cyducks.satark.model.RoutingNode;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RouteFinderService {
    private final RoutingTableService routingTableService;
    private final GeometryFactory geometryFactory = new GeometryFactory();
    private final Set<Coordinate> dangerNodes = new HashSet<>();
    private final List<Polygon> dangerPolygons = new ArrayList<>();

    @Value("${routing.tolerance-meters}")
    private double toleranceMeters;

    public RouteResponse findRoute(RouteRequest routeRequest) {
        clearDangerZones();
        addDangerZone(routeRequest.getIgnoreBounds());

        Coordinate startNode = findNearestNode(routeRequest.getOrigin());
        Coordinate endNode = findNearestNode(routeRequest.getDestination());

        var result = calculateRoute(startNode, endNode);

        return new RouteResponse(
                encodeRoute(result.path()),
                result.distance(),
                result.dangerIntersectionDistance()
        );
    }

    private String encodeRoute(List<Coordinate> path) {
        if(path == null || path.isEmpty()) return "";

        StringBuilder result = new StringBuilder();

        long prevLat = 0;
        long prevLon = 0;

        for(Coordinate coord : path) {
            long lat = Math.round(coord.getLatitude() * 100000);
            long lon = Math.round(coord.getLongitude() * 100000);

            encodeValue(result, lat - prevLat);
            encodeValue(result, lon - prevLon);

            prevLat = lat;
            prevLon = lon;

        }
        return result.toString();
    }

    private void encodeValue(StringBuilder result, long value) {
        value = value << 1;

        if(value < 0) {
            value = ~value;
        }

        while (value > 0x20) {
            long chunk = (value & 0x1F) | 0x20;
            result.append((char)(chunk+63));
            value >>= 5;
        }

        result.append((char)(value+63));
    }

    private void addDangerZone(List<Coordinate> ignoreBounds) {
        if(ignoreBounds == null || ignoreBounds.isEmpty()) {
            return;
        }

        org.locationtech.jts.geom.Coordinate[] coordinates = new org.locationtech.jts.geom.Coordinate[ignoreBounds.size() + 1];

        for (int i = 0; i < ignoreBounds.size(); i++) {
            coordinates[i] = ignoreBounds.get(i).getGeometricInstance();
        }

        coordinates[coordinates.length - 1] = coordinates[0];

        CoordinateSequence ring = new CoordinateArraySequence(coordinates);
        LinearRing linearRing = geometryFactory.createLinearRing(ring);
        Polygon polygon = geometryFactory.createPolygon(linearRing);
        dangerPolygons.add(polygon);

        routingTableService.getRoutingTable().keySet().forEach(coordinate -> {
            CoordinateSequence pointSeq = new CoordinateArraySequence(
                    new org.locationtech.jts.geom.Coordinate[] {
                            coordinate.getGeometricInstance()
                    }
            );

            Point point = geometryFactory.createPoint(pointSeq);
            if(polygon.contains(point)) {
                dangerNodes.add(coordinate);
            }
        });
    }

    private void clearDangerZones() {
        dangerNodes.clear();
        dangerPolygons.clear();
    }

    private Coordinate findNearestNode(Coordinate target) {
        Coordinate nearestNode = null;
        double minDistance = Double.MAX_VALUE;

        for(Coordinate node : routingTableService.getRoutingTable().keySet()) {
            if(node.distanceTo(target) < minDistance) {
                minDistance = node.distanceTo(target);
                nearestNode = node;
            }
        }


        return nearestNode;
    }

    private RouteResult calculateRoute(Coordinate startNode, Coordinate endNode) {
        Map<Coordinate, Double> distances = new HashMap<>();
        Map<Coordinate, Double> dangerIntersections = new HashMap<>();
        Map<Coordinate, Coordinate> previous = new HashMap<>();
        Set<Coordinate> visited = new HashSet<>();

        distances.put(startNode, 0.0);
        dangerIntersections.put(startNode, 0.0);

        PriorityQueue<NodeDistance> queue = new PriorityQueue<>();
        queue.offer(new NodeDistance(startNode, 0.0));


        while (!queue.isEmpty()) {
            NodeDistance current = queue.poll();

            if(visited.contains(current.node)) {
                continue;
            }

            visited.add(current.node);

            if(current.node.equals(endNode)) {
                break;
            }

            for(RoutingNode neighbour : routingTableService.getRoutingTable().get(current.node)) {
                Coordinate neighbourNode = neighbour.getDestination();
                double intersectionLength = calculateTotalIntersection(current.node, neighbourNode);

                if(intersectionLength > toleranceMeters) {
                    continue;
                }

                double newDistance = distances.get(current.node) + neighbour.getDistance();
                double newIntersection = dangerIntersections.get(current.node) + intersectionLength;

               if(
                       !distances.containsKey(neighbourNode) ||
                               newDistance < distances.get(neighbourNode) ||
                               (newDistance == distances.get(neighbourNode) && newIntersection < dangerIntersections.get(neighbourNode))
               ) {
                   distances.put(neighbourNode, newDistance);
                   dangerIntersections.put(neighbourNode, newIntersection);
                   previous.put(neighbourNode, current.node);
                   queue.offer(new NodeDistance(neighbourNode, newDistance));
               }
            }
        }

        if(!distances.containsKey(endNode)) {
            throw new RuntimeException("No route found");
        }

        List<Coordinate> path = reconstructPath(previous, endNode);
        return new RouteResult(path, distances.get(endNode), dangerIntersections.get(endNode));
    }

    private List<Coordinate> reconstructPath(Map<Coordinate, Coordinate> previous, Coordinate endNode) {
        List<Coordinate> path = new ArrayList<>();
        Coordinate current = endNode;

        while(current != null) {
            path.add(current);
            current = previous.get(current);
        }

        Collections.reverse(path);

        return path;
    }

    private double calculateTotalIntersection(Coordinate node, Coordinate neighbourNode) {
        CoordinateSequence lineSeq = new CoordinateArraySequence(
                new org.locationtech.jts.geom.Coordinate[] {
                        node.getGeometricInstance(),
                        neighbourNode.getGeometricInstance()
                });

        LineString roadSegment = geometryFactory.createLineString(lineSeq);

        double totalLength = 0.0;
        for(Polygon polygon : dangerPolygons) {
            if(polygon.intersects(roadSegment)) {
                Geometry intersection = polygon.intersection(roadSegment);

                if(intersection instanceof LineString) {
                    totalLength += getTotalLength((LineString) intersection);
                } else if(intersection instanceof MultiLineString multiLineString) {
                    for(int i=0; i<multiLineString.getNumGeometries(); i++) {
                        LineString line = (LineString) multiLineString.getGeometryN(i);
                        totalLength += getTotalLength(line);
                    }
                }
            }
        }

        return totalLength;
    }

    private static double getTotalLength(LineString line) {
        org.locationtech.jts.geom.Coordinate[] coordinates = line.getCoordinates();
        double totalLength = 0.0;
        for (int i = 0; i < coordinates.length - 1; i++) {
            double distance = new Coordinate(coordinates[i]).distanceTo(new Coordinate(coordinates[i + 1]));
            totalLength += (distance * 1000);
        }
        return totalLength;
    }

    private record RouteResult(List<Coordinate> path, double distance, double dangerIntersectionDistance) {}
    private record NodeDistance(Coordinate node, double distance) implements Comparable<NodeDistance> {
        @Override
        public int compareTo(NodeDistance other) {
            return Double.compare(distance, other.distance);
        }
    }


}
