package ca.uwaterloo.Lab4_206_03;

import android.app.Fragment;
import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ca.uwaterloo.mapper.MapLoader;
import ca.uwaterloo.mapper.NavigationalMap;
import ca.uwaterloo.mapper.PositionListener;
import ca.uwaterloo.sensortoy.LineGraphView;
import ca.uwaterloo.mapper.MapView;

public class MainActivityFragment extends Fragment {

    // Variable declarations
    private MapView mv;
    private SensorManager sensorManager;
    private SensorEventListener stepCounterEventListener;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Create a linear layout so that we can scroll in case the content goes off screen
        LinearLayout layout = (LinearLayout) rootView.findViewById(R.id.label);
        mv = new MapView(rootView.getContext(), 1200, 800, 60, 60);
        registerForContextMenu(mv);
        unregisterForContextMenu(mv);

        //Initialize the map
        NavigationalMap map = MapLoader.loadMap(rootView.getContext().getExternalFilesDir(null), "Lab-room-peninsula.svg");
        mv.setMap(map);

        // Initialize a graph for displaying accelerometer data
        LineGraphView graph = new LineGraphView(rootView.getContext(),100, Arrays.asList("x", "y", "z"));
        graph.setVisibility(View.VISIBLE);
        Button reset_button = (Button) rootView.findViewById(R.id.reset_button);
        Button calibration_button = (Button) rootView.findViewById(R.id.calibration_button);

        // Initialize the sensor manager for our sensors
        sensorManager = (SensorManager) rootView.getContext().getSystemService(rootView.getContext().SENSOR_SERVICE);

        // Create text views
        final TextView directionsTextView = new TextView(rootView.getContext());
        TextView stepsTextView = new TextView(rootView.getContext());
        TextView yStepsTextView = new TextView(rootView.getContext());
        TextView xStepsTextView = new TextView(rootView.getContext());
        TextView yDisplacementTextView = new TextView(rootView.getContext());
        TextView xDisplacementTextView = new TextView(rootView.getContext());
        TextView orientationTextView = new TextView(rootView.getContext());
        TextView spacing = new TextView(rootView.getContext());
        directionsTextView.setText("Directions: Select start/end points.");
        spacing.setText("====================================");

        // Create PathFinder class, which is where determining a route takes place
        final TextView testView1 = new TextView(rootView.getContext());
        final PathFinder pathFinder = new PathFinder(map, mv, testView1);

        // Instantiate a StepCounter class, which is where StepCounting and directions take place
        Compass compass1, compass2;
        compass1 = (Compass) rootView.findViewById(R.id.compass1);
        compass1.compassText = "Compass";
        compass2 = (Compass) rootView.findViewById(R.id.compass2);
        compass2.compassText = "Go this way";
        stepCounterEventListener = new StepCounter(pathFinder, mv, compass1, compass2, graph, reset_button, calibration_button, stepsTextView, yStepsTextView, xStepsTextView, yDisplacementTextView, xDisplacementTextView, orientationTextView, directionsTextView);
        // Register two sensors (linear acceleration and orientation) for the stepCounterEventListener
        //sensorManager.registerListener(stepCounterEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_FASTEST);
        // sensorManager.registerListener(stepCounterEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_FASTEST);


        Button change_map_button = (Button) rootView.findViewById(R.id.change_map_button);
        change_map_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View vw) {
                if (mv.map_number == 0) {
                    mv.map_number++;
                    NavigationalMap map0 = MapLoader.loadMap(rootView.getContext().getExternalFilesDir(null), "Lab-room-peninsula.svg");
                    mv.setMap(map0);
                    mv.changeScale(60, 60);
                } else if (mv.map_number == 1) {
                    NavigationalMap map1 = MapLoader.loadMap(rootView.getContext().getExternalFilesDir(null), "E2-3344.svg");
                    mv.setMap(map1);
                    mv.changeScale(35, 35);
                    mv.map_number++;
                } else if (mv.map_number == 2) {
                    mv.map_number++;
                    NavigationalMap map2 = MapLoader.loadMap(rootView.getContext().getExternalFilesDir(null), "Lab-room.svg");
                    mv.setMap(map2);
                    mv.changeScale(60, 60);
                } else if (mv.map_number == 3) {
                    mv.map_number++;
                    NavigationalMap map3 = MapLoader.loadMap(rootView.getContext().getExternalFilesDir(null), "Lab-room-inclined-9.4deg.svg");
                    mv.setMap(map3);
                    mv.changeScale(60, 60);
                } else if (mv.map_number == 4) {
                    mv.map_number++;
                    NavigationalMap map4 = MapLoader.loadMap(rootView.getContext().getExternalFilesDir(null), "Lab-room-inclined-16deg.svg");
                    mv.setMap(map4);
                    mv.changeScale(60, 60);
                } else if (mv.map_number == 5) {
                    mv.map_number++;
                    NavigationalMap map5 = MapLoader.loadMap(rootView.getContext().getExternalFilesDir(null), "Lab-room-peninsula-9.4deg.svg");
                    mv.setMap(map5);
                    mv.changeScale(60, 60);
                } else if (mv.map_number == 6) {
                    mv.map_number++;
                    NavigationalMap map6 = MapLoader.loadMap(rootView.getContext().getExternalFilesDir(null), "Lab-room-peninsula-16deg.svg");
                    mv.setMap(map6);
                    mv.changeScale(60, 60);
                } else if (mv.map_number == 7) {
                    mv.map_number = 0;
                    NavigationalMap map7 = MapLoader.loadMap(rootView.getContext().getExternalFilesDir(null), "Lab-room-unconnected.svg");
                    mv.setMap(map7);
                    mv.changeScale(60, 60);
                }
            }
        });

        Button path_finder_button = (Button) rootView.findViewById(R.id.path_finder_button);
        // When the path finder button is clicked, show the route between the origin and destination point on map
        path_finder_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View vw) {
                pathFinder.directionPoints = new ArrayList<PointF>();
                pathFinder.angleToTurnCalculated = false;
                testView1.setText(pathFinder.calculateShortestPath(new PointF(mv.getUserPoint().x, mv.getUserPoint().y)));
            }
        });

        final TextView coordinates = new TextView(rootView.getContext());
        mv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    // Set either the users point or the destination, depending on what was changed last
                    if (mv.setDestination == false) {
                        mv.setDestination = true;
                        mv.setUserPoint(event.getX() / mv.getXScale(), event.getY() / mv.getYScale());
                        mv.setOriginPoint(new PointF(event.getX() / mv.getXScale(), event.getY() / mv.getYScale()));
                    } else if (mv.setDestination == true) {
                        mv.setDestination = false;
                        //PointF destination = new PointF(event.getX()/mv.getXScale(), event.getY()/mv.getYScale());
                        mv.setDestinationPoint(event.getX() / mv.getXScale(), event.getY() / mv.getYScale());
                        directionsTextView.setText("Directions: Tap on Plan Route button.");
                        // mv.map.calculateIntersections(mv.getUserPoint(), mv.getDestinationPoint());
                    }
                    coordinates.setText("Touch coordinates : " +
                            //mv.ma
                            String.valueOf(mv.getUserPoint()));
                }
                return true;
            }
        });

        // Add the map to our layout
        layout.addView(mv);
        layout.addView(directionsTextView);
        layout.addView(orientationTextView);
        // Add the graph to our layout
        layout.addView(graph);
        // Add the text views to our layout
        layout.addView(coordinates);
        testView1.setText("Path yet to be determined");
        layout.addView(testView1);
        layout.addView(stepsTextView);
        layout.addView(yStepsTextView);
        layout.addView(xStepsTextView);
        layout.addView(spacing);
        layout.addView(yDisplacementTextView);
        layout.addView(xDisplacementTextView);
        return rootView;
    }

     @Override
     public void onCreateContextMenu(ContextMenu menu , View v, ContextMenuInfo menuInfo) {
         super.onCreateContextMenu(menu, v, menuInfo);
         mv.onCreateContextMenu(menu,v, menuInfo);
     }

    @Override
    public boolean  onContextItemSelected(MenuItem item) {
        return  super.onContextItemSelected(item) ||  mv.onContextItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Re-register sensor event listeners
        sensorManager.registerListener(stepCounterEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(stepCounterEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void onPause() {
        // Unregister sensor event listeners
        sensorManager.unregisterListener(stepCounterEventListener);
        super.onPause();
    }
}
