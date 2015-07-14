package ca.uwaterloo.Lab4_206_03;

import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import ca.uwaterloo.mapper.MapView;
import ca.uwaterloo.sensortoy.LineGraphView;

public class StepCounter implements SensorEventListener {

    // Initialize variables
    TextView stepsView, yStepsView, xStepsView, yDisplacementView, xDisplacementView, pathStatusTextView, directionsTextView;
    float currentMagnitude, prevMagnitude, currentDerivative, prevDerivative, magnitudeOfStep;
    Long prevTimestamp, currentTimestamp, lastStepTimestamp;
    int steps, ySteps, xSteps = 0;
    double yDisplacement = 0, xDisplacement = 0, stepMagnitude = 0.5, stepMagnitudeY = 0, stepMagnitudeX = 0, calibratedOrientationRadians = 0, angleToTurn = 0;
    float[] values = new float[3];
    float[] prev20Magnitudes = new float[20];
    float largestMagnitudeInPast20Readings = 0;
    float degreesFromNorth = 0;
    float currentOrientation, calibratedOrientation;
    int filledCount = 0;
    boolean filled = false;
    LineGraphView accelerometerGraph;
    Button reset_button, calibration_button, start_button;
    String direction = "North";;
    PathFinder pathFinder;
    MapView mv;
    Compass orientationCompass, destinationCompass;
    EditText stepMagnitudeInput;

    // Constructor for the class, takes in a graph (for graphing steps), buttons (for reset/calibration) and TextViews (for displaying information)
    public StepCounter(PathFinder pathFinder1, MapView mapView1, Compass compass1, Compass compass2, LineGraphView graph, Button button1, Button button2, Button button3, TextView view1, TextView view2, TextView view3, TextView view4, TextView view5, TextView view6, TextView view7, EditText text1) {
        pathFinder = pathFinder1;
        mv = mapView1;
        orientationCompass = compass1;
        destinationCompass = compass2;
        accelerometerGraph = graph;
        reset_button = button1;
        calibration_button = button2;
        start_button = button3;
        stepsView = view1;
        yStepsView = view2;
        xStepsView = view3;
        yDisplacementView = view4;
        xDisplacementView = view5;
        pathStatusTextView = view6;
        directionsTextView = view7;
        stepMagnitudeInput = text1;
    }

    // Find the derivative given a point (y = magnitude, x = timestamp)
    public float calcDerivative(float magnitude, Long timestamp) {
        // On first iteration, set the prevTimestamp & prevMagnitude
        if(prevTimestamp == null && prevMagnitude == 0.0f) {
            prevTimestamp = timestamp;
            prevMagnitude = magnitude;
            return 0;
        } else {
            // Calculate the derivative by ( y2 - y1 / x2 - x1)
            float changeInY = magnitude-prevMagnitude;
            float changeInX = (float) (timestamp-prevTimestamp);
            float derivative = changeInY/changeInX;
            prevMagnitude = magnitude;
            prevTimestamp = timestamp;
            return derivative;
        }
    }

    public void findLargestMagnitudeInPast20Readings() {
        // If the array hasn't been filled yet
        if(!filled) {
            // Fill the next unfilled index in the array
            prev20Magnitudes[filledCount] = currentMagnitude;
            float largestValue = 0;
            // Find the largest value in the array
            for(int i = 0; i < filledCount; i++) {
                if(Math.abs(prev20Magnitudes[i]) > Math.abs(largestValue)) {
                    largestValue = prev20Magnitudes[i];
                }
            }
            largestMagnitudeInPast20Readings = largestValue;
            filledCount++;
            if(filledCount == prev20Magnitudes.length) {
                filled = true;
            }
        } else {
            // Move the array down by one index by moving everything backwards and setting the final index in the array to the current magnitude
            for(int i = 0; i < prev20Magnitudes.length; i++) {
                if(i == (prev20Magnitudes.length-1)) {
                    prev20Magnitudes[i] = currentMagnitude;
                } else {
                    prev20Magnitudes[i] = prev20Magnitudes[i+1];
                }
            }
            float largestValue = 0;
            // Find the largest value in the array
            for(int i = 0; i < prev20Magnitudes.length; i++) {
                if(Math.abs(prev20Magnitudes[i]) > Math.abs(largestValue)) {
                    largestValue = prev20Magnitudes[i];
                }
            }
            largestMagnitudeInPast20Readings = largestValue;
        }
    }

    public void checkForStep() {
        // There are 6 steps to our step checking algorithm:

        // 1. Check if there's been at least 100ms since the last step

        // 2. If there has, then use the sensor data (magnitude/timestamp) to calculate a derivative.
        // Check that the current derivative is negative and previous derivative was positive,
        // if this occurs we know there was probably just a peak in the graph.

        // 3. Check that the largest magnitude in the past 20 readings is < 5, this is to prevent steps being counted when the phone is being shaken
        // For walking the accelerometer magnitude should never be above 5

        // 4. Check that the current sensor magnitude is > 1, this is to prevent counting steps for insignificant values

        // 5. Lastly check time intervals, make sure there was somewhere between 600 and 1000 milliseconds since the last step was taken - a normal walking pace!

        // 6. If all of the above hold, increment the number of steps!
        if(lastStepTimestamp == null) {
            lastStepTimestamp = currentTimestamp;
        } else {
            // Find the difference between the current timestamp and timestamp of the last step
            float timeDiff = (currentTimestamp - lastStepTimestamp)/1000000;
            // Set the derivative if it hasn't been set yet
            if(prevDerivative == 0.0f) {
                prevDerivative = calcDerivative(currentMagnitude, currentTimestamp);
            } else {
                // Check for a step as long as it's been 100ms since the last step
                if(timeDiff > 100) {
                    currentDerivative = calcDerivative(currentMagnitude, currentTimestamp);
                    // Bulk of the step checking, implements steps 2-6 as mentioned above
                    if(largestMagnitudeInPast20Readings < 5 && currentMagnitude > 1.3 && currentDerivative < 0 && prevDerivative > 0 && (timeDiff > 600) && (timeDiff < 1000)) {
                        steps += 1;
                        if(direction == "North" || direction == "South") {
                            ySteps +=1 ;
                        } else {
                            xSteps +=1;
                        }
                        yDisplacement+= stepMagnitudeY;
                        xDisplacement+= stepMagnitudeX;
                        if(pathFinder.givingDirections == true) {
                            PointF currentPoint = new PointF(mv.getUserPoint().x, mv.getUserPoint().y);
                            PointF newPoint = new PointF(currentPoint.x+(float)stepMagnitudeX,currentPoint.y-(float)stepMagnitudeY);
                            if(mv.map.calculateIntersections(currentPoint, newPoint).isEmpty()) {
                                mv.setUserPoint(newPoint);
                            }
                        }
                        lastStepTimestamp = currentTimestamp;
                        magnitudeOfStep = currentMagnitude;
                    }
                    prevDerivative = currentDerivative;
                }
            }
            if(timeDiff > 1000) {
                lastStepTimestamp = currentTimestamp;
            }
        }
    }

    public void onAccuracyChanged(Sensor s, int i) {

    }

    public void onSensorChanged(SensorEvent se) {
        if (se.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            // Reset the # of steps when the reset button is clicked
            reset_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View vw) {
                    steps = 0;
                    ySteps = 0;
                    xSteps = 0;
                    yDisplacement = 0;
                    xDisplacement = 0;
                }
            });
            start_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View vw) {
                    stepMagnitude = Double.parseDouble(stepMagnitudeInput.getText().toString());
                    if(pathFinder.givingDirections == false) {
                        pathFinder.givingDirections = true;
                        pathFinder.directionPoints = new ArrayList<PointF>();
                        pathFinder.angleToTurnCalculated = false;
                        // Find the route between the origin and destination point on map
                        pathStatusTextView.setText(pathFinder.calculateShortestPath(new PointF(mv.getUserPoint().x, mv.getUserPoint().y)));
                        start_button.setText("Stop");
                    } else {
                        pathFinder.givingDirections = false;
                        directionsTextView.setText("Directions: Press GO to start.");
                        start_button.setText("Go!");
                    }
                }
            });

            // Implement a low-pass filter to values received from the sensor, this will smooth out the data
            values[0] += (se.values[0] - values[0]) / 3;
            values[1] += (se.values[1] - values[1]) / 3;
            values[2] += (se.values[2] - values[2]) / 3;
            // Compute the magnitude of the acceleration vector sqrt(x^2 + y ^2 + z^2)
            currentMagnitude = (float) Math.sqrt(((values[0]*values[0]) + (values[1]*values[1]) + (values[2]*values[2])));
            currentTimestamp = se.timestamp;
            findLargestMagnitudeInPast20Readings();
            checkForStep();
            // Update the TextViews
            stepsView.setText("Total Steps: " + steps);
            yStepsView.setText("Total Steps North/South: " + ySteps);
            xStepsView.setText("Total Steps East/West: " + xSteps);
            yDisplacementView.setText("North/South Displacement: " + yDisplacement + "m");
            xDisplacementView.setText("East/West Displacement: " + xDisplacement + "m");
            // Update the graph
            accelerometerGraph.addPoint(values);
        } else if(se.sensor.getType() == Sensor.TYPE_ORIENTATION) {
            currentOrientation = se.values[0];
            // Calibrate the orientation (set the current orientation as north) when the button is clicked
            calibration_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View vw) {
                    degreesFromNorth = currentOrientation;
                }
            });
            // Calculate a calibrated orientation by using modulus
            calibratedOrientation = (((currentOrientation-degreesFromNorth)%360) + 360)%360;
            orientationCompass.updateDirection(calibratedOrientation);
            // Choose directions to go
            if(!pathFinder.directionPoints.isEmpty()) {
                directionsTextView.setText("Size is :" + pathFinder.directionPoints.size());
                if(pathFinder.directionPoints.size() == 1) {
                    directionsTextView.setText("You've arrived!");
                } else {
                    if(pathFinder.directionPoints.size() == 2) {
                        if(!pathFinder.angleToTurnCalculated) {
                            PointF point1 = pathFinder.directionPoints.get(0);
                            PointF point2 = pathFinder.directionPoints.get(1);
                            float deltaX = point2.x - point1.x;
                            float deltaY = point1.y - point2.y;
                            angleToTurn = Math.atan(deltaX/deltaY)*(180/Math.PI);
                            if(deltaY > 0) {
                                if(angleToTurn < 0) {
                                    angleToTurn+=360;
                                }
                            } else {
                                angleToTurn += 180;
                            }
                            pathFinder.angleToTurnCalculated = true;
                            //directionsTextView.setText("Tan of: + " + deltaX + " / " + deltaY + " is " + angleToTurn + " degrees");
                        } else {
                            destinationCompass.updateDirection((float) angleToTurn);
                            PointF point1 = new PointF(mv.getUserPoint().x, mv.getUserPoint().y);
                            PointF point2 = pathFinder.directionPoints.get(1);
                            float deltaX = point2.x - point1.x;
                            float deltaY = point1.y - point2.y;
                            if(Math.abs(deltaX) <= 0.5 && Math.abs(deltaY) <= 0.5) {
                                directionsTextView.setText("Directions: Congratz, you've arrived! :)");
                                pathFinder.givingDirections = false;
                                pathFinder.directionPoints = new ArrayList<PointF>();
                                start_button.setText("Go!");
                            } else {
                                if(Math.abs(angleToTurn-calibratedOrientation) <= 5) {
                                    directionsTextView.setText("Directions: Walk forward.");
                                } else {
                                    directionsTextView.setText("Directions: Turn to the direction indicated by the compass");
                                }
                            }
                        }
                    }
                }
            }
            // Determine the general direction user is facing
            if(calibratedOrientation < 45 || calibratedOrientation > 315) {
                direction = "North";
            } else if(calibratedOrientation < 135 && calibratedOrientation > 45) {
                direction = "East";

            } else if(calibratedOrientation < 225 && calibratedOrientation > 135) {
                direction = "South";
            } else if(calibratedOrientation < 315 && calibratedOrientation > 225) {
                direction = "West";
            }
            calibratedOrientationRadians = calibratedOrientation*Math.PI/180;
            // Calculate the vector components of a step in this direction
            stepMagnitudeY = stepMagnitude*Math.cos(calibratedOrientationRadians);
            stepMagnitudeX = stepMagnitude*Math.sin(calibratedOrientationRadians);
        }
    }
}
