package com.airmap.freehand;

import android.graphics.PointF;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vansh Gandhi on 9/22/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */

class PointMath {
    //TODO: Just use PolylineUtils instead
    static List<PointF> simplify(List<PointF> vertices, double distanceThreshold) {
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

    public static List<PointF> findIntersections(List<PointF> polygon) {
        List<PointF> intersections = new ArrayList<>();
        if (polygon.size() < 4) {
            return intersections;
        }
        List<Line> lineSegments = new ArrayList<>();
        for (int i = 0; i < polygon.size() - 1; i++) {
            PointF a = polygon.get(i);
            PointF b = polygon.get(i + 1);
            lineSegments.add(new Line(a, b));
        }
        for (int i = 0; i < lineSegments.size(); i++) {
            for (int j = i+2; j < lineSegments.size(); j++) {
                PointF intersection = lineIntersect(lineSegments.get(i), lineSegments.get(j));
                if (intersection != null) {
                    intersections.add(intersection);
                }
            }
        }
        return intersections;
    }

    private static PointF lineIntersect(Line line1, Line line2) {
        float x1 = line1.a.x;
        float y1 = line1.a.y;
        float x2 = line1.b.x;
        float y2 = line1.b.y;
        float x3 = line2.a.x;
        float y3 = line2.a.y;
        float x4 = line2.b.x;
        float y4 = line2.b.y;
        float denom = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);
        if (denom == 0.0) { // Lines are parallel.
            return null;
        }
        float ua = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3))/denom;
        float ub = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3))/denom;
        if (ua >= 0.0f && ua <= 1.0f && ub >= 0.0f && ub <= 1.0f) {
            // Get the intersection point.
            return new PointF((x1 + ua*(x2 - x1)), (y1 + ua*(y2 - y1)));
        }
        return null;
    }

    private static class Line {
        PointF a, b;

        public Line(PointF a, PointF b) {
            this.a = a;
            this.b = b;
        }
    }
}
