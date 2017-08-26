package org.lejos.example.Behaviours;

import java.io.DataInputStream;
import java.io.IOException;
import lejos.nxt.LightSensor;
import lejos.nxt.SensorPort;
import org.lejos.example.AutomatedControl;

public class LineFollower implements AutomatedControl {

    static LightSensor cs = new LightSensor(SensorPort.S1);
    static int colorThreshold = 50;
    static int lineSearchSteps = 8;

    MovementController mc = new MovementController();
    DataInputStream dis;
    
    boolean lastFoundLeft = false;
    
    public LineFollower() {
        
    }

    public void start(DataInputStream dis) throws InterruptedException {
        this.dis = dis;
        advance();
    }

    private void advance() throws InterruptedException {
        checkIfStop();
        //System.out.println("Forward!");
        mc.moveForward();
        if (lineFound()) {
            advance();
        } else {
            searchForLine();
        }
    }

    private boolean lineFound() {
        System.out.println("LV " + cs.getLightValue());
        return cs.getLightValue() < colorThreshold;
    }

    private void searchForLine() throws InterruptedException {
        searchForLine(1);
    }
    
    private void searchForLine(int attempt) throws InterruptedException {
        checkIfStop();
        boolean lineFound = false;
        int steps = attempt;
        // Left search
        if (lastFoundLeft) {
            lineFound = searchLeft(steps) || searchRight(steps) || searchRight(steps);
        } else {
            lineFound = searchRight(steps) || searchLeft(steps) || searchLeft(steps);
        }
        if (lineFound) {
            advance();
        } else {
            searchForLine(attempt + 1);
        }
    }

    private boolean searchLeft(int steps) throws InterruptedException {
        checkIfStop();
        for (int i = 0; i < steps; i++) {
            mc.turnLeft();
            if (lineFound()) {
                lastFoundLeft = true;
                return true;
            }
        }
        return false;
    }

    private boolean searchRight(int steps) throws InterruptedException {
        checkIfStop();
        // Right search
        for (int i = 0; i < steps; i++) {
            mc.turnRight();
            if (lineFound()) {
                lastFoundLeft = false;
                return true;
            }
        }
        return false;
    }
    
    private void checkIfStop() {
        stop();
    }
    
    public void stop() {
        char n = 1;
        try {
            n = dis.readChar();
        } catch (IOException ex) {
            // error
        }
        if (n == '0') {
            System.out.println("Switched to manual control!");
            return;
        }
        System.out.println("" + n);
    }

}
