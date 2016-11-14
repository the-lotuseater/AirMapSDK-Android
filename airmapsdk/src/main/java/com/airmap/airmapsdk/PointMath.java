package com.airmap.airmapsdk;

import android.graphics.PointF;

import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vansh Gandhi on 9/22/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */

public class PointMath {
    //TODO: test PolylineUtils simplify
    public static List<PointF> simplify(List<PointF> vertices, double distanceThreshold) {
        return vertices != null ? simplificationOf(vertices, distanceThreshold) : null;
    }

    private static List<PointF> simplificationOf(List<PointF> vertices, double distanceThreshold) {
        List<PointF> simplifiedVertices = new ArrayList<>();
        Double maxDistance = null;
        int maxDistancePointFIdx = 0;
        int lastPointFIdx = vertices.size() - 1;
        int currentIdx = 0;
        for (PointF aVertex : vertices) {
            if (currentIdx != 0 && currentIdx != lastPointFIdx) {
                Double distance = shortestDistanceToSegment(aVertex, vertices.get(0), vertices.get(lastPointFIdx));
                if (maxDistance == null || distance > maxDistance) {
                    maxDistancePointFIdx = currentIdx;
                    maxDistance = distance;
                }
            }
            currentIdx++;
        }

        if (maxDistance != null) {
            if (maxDistance > distanceThreshold) {
                List<PointF> sub = simplify(vertices.subList(0, maxDistancePointFIdx + 1), distanceThreshold);
                List<PointF> sup = simplify(vertices.subList(maxDistancePointFIdx, lastPointFIdx + 1), distanceThreshold);

                simplifiedVertices.addAll(sub);
                simplifiedVertices.addAll(sup);

            } else {
                simplifiedVertices.add(vertices.get(0));
                simplifiedVertices.add(vertices.get(lastPointFIdx));
            }
        }
        return simplifiedVertices;
    }

    private static double shortestDistanceToSegment(PointF thePointF, PointF segmentPointF_A, PointF segmentPointF_B) {
        double area = calculateTriangleAreaGivenVertices(thePointF, segmentPointF_A, segmentPointF_B);
        double lengthSegment = distanceBetween(segmentPointF_A, segmentPointF_B);
        return (2 * area) / lengthSegment;
    }

    private static double calculateTriangleAreaGivenVertices(PointF a, PointF b, PointF c) {
        return Math.abs(((a.x * (b.y - c.y)) + (b.x * (c.y - a.y)) + (c.x * (a.y - b.y))) / 2);
    }

    public static double distanceBetween(PointF a, PointF b) {
        return Math.sqrt(((a.x - b.x) * (a.x - b.x)) + ((a.y - b.y) * (a.y - b.y)));
    }

    public static List<LatLng> findIntersections(List<LatLng> polygon) {
        List<LatLng> intersections = new ArrayList<>();
        List<Line> lineSegments = new ArrayList<>();

        for (int i = 0; i < polygon.size() - 1; i++) {
            LatLng a = polygon.get(i);
            LatLng b = polygon.get(i + 1);
            lineSegments.add(new Line(a, b));
        }
        for (int i = 0; i < lineSegments.size(); i++) {
            for (int j = i+2; j < lineSegments.size(); j++) {
                LatLng intersection = lineIntersect(lineSegments.get(i), lineSegments.get(j));
                if (intersection != null) {
                    intersections.add(intersection);
                }
            }
        }
        for (LatLng polygonPoint : polygon) {
            intersections.remove(polygonPoint); //Remove points that are in the actual polygon cuz those aren't actually intersections
        }
        return intersections;
    }

    private static LatLng lineIntersect(Line line1, Line line2) {
        double x1 = line1.a.getLatitude();
        double y1 = line1.a.getLongitude();
        double x2 = line1.b.getLatitude();
        double y2 = line1.b.getLongitude();
        double x3 = line2.a.getLatitude();
        double y3 = line2.a.getLongitude();
        double x4 = line2.b.getLatitude();
        double y4 = line2.b.getLongitude();
        double denom = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);
        if (denom == 0.0) { // Lines are parallel.
            return null;
        }
        double ua = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3)) / denom;
        double ub = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3)) / denom;
        if (ua >= 0.0f && ua <= 1.0f && ub >= 0.0f && ub <= 1.0f) {
            // Get the intersection point.
            return new LatLng((x1 + ua * (x2 - x1)), (y1 + ua * (y2 - y1)));
        }
        return null;
    }

    private static class Line {
        LatLng a, b;

        public Line(LatLng a, LatLng b) {
            this.a = a;
            this.b = b;
        }
    }
}