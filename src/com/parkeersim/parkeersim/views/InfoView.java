package com.parkeersim.parkeersim.views;

import com.parkeersim.mvc.BaseModel;
import com.parkeersim.mvc.BaseView;

import javax.swing.*;
import java.awt.*;

public class InfoView extends BaseView {
    private JFrame frame = new JFrame();

    private JPanel timePanel = new JPanel();
    private JLabel minutelabel = new JLabel();
    private JLabel hourlabel = new JLabel();
    private JLabel daylabel = new JLabel();

    private JPanel parkingSpotsPanel = new JPanel();
    private JLabel openParkingPassSpots = new JLabel();
    private JLabel openParkingSpots = new JLabel();

    private JPanel simulationSpeedPanel = new JPanel();
    private JLabel simulationSpeed = new JLabel();

    public InfoView(){
        frame.setLayout(new FlowLayout());
        frame.setSize(400,400);
        frame.setBackground(Color.red);

        frame.add(timePanel);
        timePanel.add(daylabel);
        timePanel.add(hourlabel);
        timePanel.add(minutelabel);

        frame.add(parkingSpotsPanel);
        parkingSpotsPanel.add(openParkingPassSpots);
        parkingSpotsPanel.add(openParkingSpots);

        frame.add(simulationSpeedPanel);
        simulationSpeedPanel.add(simulationSpeed);

        //frame.setVisible(true);
    }

    public void setFrameVisible(boolean state){
        frame.setVisible(state);
    }

    public void minute(String string){
        minutelabel.setText(string);
    }

    public void hour(String string){
        hourlabel.setText(string);
    }

    public void day(String string){
        daylabel.setText(string);
    }

    public void parkingPassSpots(String string){
        openParkingPassSpots.setText(string);
    }

    public void parkingsSpots(String string){
        openParkingSpots.setText(string);
    }

    public void simulationSpeed(String string){
        simulationSpeed.setText(string);
    }

    @Override
    public void update(BaseModel model){

    }
}
