package ca.uwaterloo.Lab4_206_03;

import android.graphics.PointF;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ca.uwaterloo.mapper.MapView;
import ca.uwaterloo.mapper.NavigationalMap;

public class PathFinder {

    // debugTag allows us to only view logs in the debugger associated with the tag
    private String debugTag = "CLOCKS";
    public NavigationalMap map;
    public MapView mv;
    public List<PointF> directionPoints = new ArrayList<PointF>();
    public boolean angleToTurnCalculated = false;
    private StepCounter stepCounter;
    TextView testView;

    public PathFinder(NavigationalMap map1, MapView mapView1, TextView view1) {
        map = map1;
        mv = mapView1;
        testView = view1;
    }

    public String calculateShortestPath(PointF userPoint) {
        // Points are the set of points to take you from the start point to the destination point
        List<PointF> points = new ArrayList<PointF>();
        points.add(new PointF(userPoint.x, userPoint.y));
        // Priorities are the prioritized directions you want to move in
        int[] priorities = new int[5];
        getPriorities(priorities, userPoint);
        // Last move keeps track of the last direction we moved in
        int last_move = 0;
        // num_tries is the number of moves we have made, usually if we make over 10 moves and still haven't found a solution then we're stuck
        int num_tries = 0;

        // While there isn't a direct path between userPoint and the end
        while(!map.calculateIntersections(userPoint, mv.getDestinationPoint()).isEmpty()) {
            num_tries++;
            if(num_tries >= 100) {
                Log.d(debugTag, "======================== FAILURE ===================");
                Log.d(debugTag, points.toString());
                mv.setUserPath(points);
                return "Couldn't find a path - stuck in an infinite loop.";
            }
            // In order to make a move:
            // Check each direction, in order of priority
            // 1. See if we can go in that direction
            // 2. Check if we haven't previously been there
            // If both these conditions pass, move accordingly
            for(int i = 0; i < priorities.length; i++) {
                if(priorities[i] == 0) {
                    Log.d(debugTag, "Got stuck");
                    mv.setUserPath(points);
                    return "Couldn't find a path - Got stuck :(";
                } else if(priorities[i] == 1) {
                    Log.d(debugTag, "Trying to go north");
                    if(canGoNorth(userPoint)) {
                        PointF newPoint = new PointF(userPoint.x, userPoint.y-1);
                        if(alreadyVisited(newPoint, points)) {
                            Log.d(debugTag, "Continuing");
                            continue;
                        } else {
                            Log.d(debugTag, "Went north");
                            userPoint.y -= 1;
                            points.add(newPoint);
                            break;
                        }
                    }
                } else if(priorities[i] == 2) {
                    Log.d(debugTag, "Trying to go east");
                    if(canGoEast(userPoint)) {
                        PointF newPoint = new PointF(userPoint.x+1, userPoint.y);
                        if(alreadyVisited(newPoint, points)) {
                            Log.d(debugTag, "Continuing");
                            continue;
                        } else {
                            Log.d(debugTag, "Went east");
                            userPoint.x += 1;
                            points.add(newPoint);
                            break;
                        }
                    }
                } else if(priorities[i] == 3) {
                    Log.d(debugTag, "Trying to go south");
                    if(canGoSouth(userPoint)) {
                        PointF newPoint = new PointF(userPoint.x, userPoint.y+1);
                        if(alreadyVisited(newPoint, points)) {
                            Log.d(debugTag, "Continuing");
                            continue;
                        } else {
                            Log.d(debugTag, "Went south");
                            userPoint.y += 1;
                            points.add(newPoint);
                            break;
                        }
                    }
                } else if(priorities[i] == 4) {
                    Log.d(debugTag, "Trying to go west");
                    if(canGoWest(userPoint)) {
                        PointF newPoint = new PointF(userPoint.x-1, userPoint.y);
                        if(alreadyVisited(newPoint, points)) {
                            Log.d(debugTag, "Continuing");
                            continue;
                        } else {
                            Log.d(debugTag, "Went west");
                            userPoint.x -= 1;
                            points.add(newPoint);
                            break;
                        }
                    }
                }
            }
        }
        points.add(new PointF(mv.getDestinationPoint().x, mv.getDestinationPoint().y));
        Log.d(debugTag, "======================== SUCCESS ===================");
        directionPoints = points;
        mv.setUserPath(points);
        return "Path determined! :D";
    }

    // Returns true if the given point has already been visited
    private boolean alreadyVisited(PointF newPoint, List<PointF> visitedPoints) {
        for(int i = 1; i < visitedPoints.size(); i++) {
            if(visitedPoints.get(i).x == newPoint.x && visitedPoints.get(i).y == newPoint.y) {
                return true;
            }
        }
        return false;
    }

    // Returns true if there no wall north of the user
    private boolean canGoNorth(PointF userPoint) {
        PointF testPoint = new PointF(userPoint.x, userPoint.y);
        if(map.calculateIntersections(testPoint, new PointF(testPoint.x, testPoint.y - 1)).isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    // Returns true if there no wall east of the user
    private boolean canGoEast(PointF userPoint) {
        PointF testPoint = new PointF(userPoint.x, userPoint.y);
        if(map.calculateIntersections(testPoint, new PointF(testPoint.x + 1, testPoint.y)).isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    // Returns true if there no wall south of the user
    private boolean canGoSouth(PointF userPoint) {
        PointF testPoint = new PointF(userPoint.x, userPoint.y);
        if(map.calculateIntersections(testPoint, new PointF(testPoint.x, testPoint.y + 1)).isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    // Returns true if there no wall west of the user
    private boolean canGoWest(PointF userPoint) {
        PointF testPoint = new PointF(userPoint.x, userPoint.y);
        if(map.calculateIntersections(testPoint, new PointF(testPoint.x - 1, testPoint.y)).isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    // Get the prioritized directions to move in
    private void getPriorities(int[] priorities, PointF userPoint) {
        //1 = North, 2 = East, 3 = South, 4 = West, 0 = Stuck.
        float deltaX = mv.getDestinationPoint().x - userPoint.x;
        float deltaY = userPoint.y - mv.getDestinationPoint().y;
        if (Math.abs(deltaX) > Math.abs(deltaY) && deltaX > 0) {
            // Prioritize eastern movement
            priorities[0] = 2;
            if(deltaY > 0) {
                priorities[1] = 1;
                priorities[2] = 3;
            } else {
                priorities[1] = 3;
                priorities[2] = 1;
            }
            priorities[3] = 4;
            priorities[4] = 0;
        } else if (Math.abs(deltaX) > Math.abs(deltaY) && deltaX < 0) {
            // Prioritize western movement
            priorities[0] = 4;
            if(deltaY > 0) {
                priorities[1] = 1;
                priorities[2] = 3;
            } else {
                priorities[1] = 3;
                priorities[2] = 1;
            }
            priorities[3] = 2;
            priorities[4] = 0;
        } else if (Math.abs(deltaX) < Math.abs(deltaY) && deltaY > 0) {
            // Prioritize northern movement
            priorities[0] = 1;
            if(deltaX > 0) {
                priorities[1] = 2;
                priorities[2] = 4;
            } else {
                priorities[1] = 4;
                priorities[2] = 2;
            }
            priorities[3] = 3;
            priorities[4] = 0;
        } else if (Math.abs(deltaX) < Math.abs(deltaY) && deltaY < 0) {
            // Prioritize southern movement
            priorities[0] = 3;
            if(deltaX > 0) {
                priorities[1] = 2;
                priorities[2] = 4;
            } else {
                priorities[1] = 4;
                priorities[2] = 2;
            }
            priorities[3] = 1;
            priorities[4] = 0;
        }
    }
}
