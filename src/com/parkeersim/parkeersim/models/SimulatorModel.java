package com.parkeersim.parkeersim.models;

import com.parkeersim.mvc.BaseModel;

import java.util.Random;

public class SimulatorModel extends BaseModel {
    private static final String AD_HOC = "1";
    private static final String PASS = "2";

    private boolean isPaused = false;

    private GarageModel garagemodel;

    private CarQueue entranceCarQueue;
    private CarQueue entrancePassQueue;
    private CarQueue paymentCarQueue;
    private CarQueue exitCarQueue;

    private int day = 0;
    private int hour = 0;
    private int minute = 0;

    private int tickPause = 100;

    private double money;

    //todo: atm komen er te veel normale auto's binnen waardoor de queue zo vol komt dat hij nooit meer omlaag gaat
    //todo: als auto's te lang in de queue staan kunnen ze weg gaan

    int weekDayArrivals= 1000; // average number of arriving cars per hour
    int weekendArrivals = 200; // average number of arriving cars per hour
    int weekDayPassArrivals= 50; // average number of arriving cars per hour
    int weekendPassArrivals = 5; // average number of arriving cars per hour

    int enterSpeed = 4; // number of cars that can enter per minute
    int paymentSpeed = 7; // number of cars that can pay per minute
    int exitSpeed = 5; // number of cars that can leave per minute

    public SimulatorModel(GarageModel garagemodel) {
        entranceCarQueue = new CarQueue();
        entrancePassQueue = new CarQueue();
        paymentCarQueue = new CarQueue();
        exitCarQueue = new CarQueue();
        this.garagemodel = garagemodel;
    }

    public void run() {
        Thread thread = new Thread(() -> {
            while (true){
                if(isPaused){
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    tick();
                }
            }
        });
        thread.start();
    }

    public void setPause(boolean state){
        isPaused = state;
    }

    public void setTickPause(int newTickPause){
        this.tickPause = newTickPause;
    }

    public int getTickPause(){
        return tickPause;
    }


    private void tick() {
        advanceTime();
        handleExit();
        updateViews();
        // Pause.
        try {
            Thread.sleep(tickPause);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        handleEntrance();
    }

    public int getDay(){
        return day;
    }

    public int getHour(){
        return hour;
    }

    public int getMinute(){
        return minute;
    }

    public int getOpenParkingPassSpots(){
        return garagemodel.getNumberOfOpenParkingPassSpots();
    }

    public int getOpenSpots(){
        return garagemodel.getNumberOfOpenSpots();
    }

    public int getAllSpots(){
        return garagemodel.getNumberOfRows() * garagemodel.getNumberOfFloors() * garagemodel.getNumberOfPlaces();
    }

    public int getNumberOfAdHocCars(){
        return garagemodel.getNumberOfAdHocCars();
    }

    public int getNumberOfParkingPassCars(){
        return garagemodel.getNumberOfParkingPassCars();
    }

    public int getNumberOfReservationCars(){
        return garagemodel.getNumberOfReservationCars();
    }

    private void advanceTime(){
        // Advance the time by one minute.
        minute++;
        while (minute > 59) {
            minute -= 60;
            hour++;
        }
        while (hour > 23) {
            hour -= 24;
            day++;
        }
        while (day > 6) {
            day -= 7;
        }

    }

    private void handleEntrance(){
        carsArriving();
        carsEntering(entrancePassQueue);
        carsEntering(entranceCarQueue);
    }

    private void handleExit(){
        carsReadyToLeave();
        carsPaying();
        carsLeaving();
    }

    private void updateViews(){
        garagemodel.tick();
        // Update the car park view.
        garagemodel.updateView();
    }

    /**
     * Method for adding new arriving cars
     */
    private void carsArriving(){
        int numberOfCars=getNumberOfCars(weekDayArrivals, weekendArrivals);
        addArrivingCars(numberOfCars, AD_HOC);
        numberOfCars=getNumberOfCars(weekDayPassArrivals, weekendPassArrivals);
        addArrivingCars(numberOfCars, PASS);
    }

    /**
     * This method takes the first car in the queue and assigns it to a parking space
     * @param queue
     */
    private void carsEntering(CarQueue queue){
        int i=0;
        // Remove car from the front of the queue and assign to a parking space.
        while (queue.carsInQueue()>0 && i<enterSpeed) {
            Car car = queue.removeCar();
            if(car.getHasToPay() == false && garagemodel.getNumberOfOpenParkingPassSpots()>0){
                Location freeLocation = garagemodel.getFirstFreeParkingPassLocation();
                garagemodel.setCarAt(freeLocation, car);
                i++;
            } else if (garagemodel.getNumberOfOpenSpots()>0){
                Location freeLocation = garagemodel.getFirstFreeLocation();
                garagemodel.setCarAt(freeLocation, car);
                i++;
            }
        }
    }

    private void carsReadyToLeave(){
        // Add leaving cars to the payment queue.
        Car car = garagemodel.getFirstLeavingCar();
        while (car!=null) {
            if (car.getHasToPay()){
                car.setIsPaying(true);
                paymentCarQueue.addCar(car);
            }
            else {
                carLeavesSpot(car);
            }
            car = garagemodel.getFirstLeavingCar();
        }
    }

    private void carsPaying(){
        // Let cars pay.
        int i=0;
        while (paymentCarQueue.carsInQueue()>0 && i < paymentSpeed){
            Car car = paymentCarQueue.removeCar();
            double priceRounded = Math.round(car.getStayTime() * 0.025 * 100);
            double price = priceRounded/100;
            money += price;
            carLeavesSpot(car);
            i++;
        }
    }

    private void carsLeaving(){
        // Let cars leave.
        int i=0;
        while (exitCarQueue.carsInQueue()>0 && i < exitSpeed){
            exitCarQueue.removeCar();
            i++;
        }
    }

    private int getNumberOfCars(int weekDay, int weekend){
        Random random = new Random();

        // Get the average number of cars that arrive per hour.
        int averageNumberOfCarsPerHour = day < 5
                ? weekDay
                : weekend;

        // Calculate the number of cars that arrive this minute.
        double standardDeviation = averageNumberOfCarsPerHour * 0.3;
        double numberOfCarsPerHour = averageNumberOfCarsPerHour + random.nextGaussian() * standardDeviation;
        return (int)Math.round(numberOfCarsPerHour / 60);
    }

    /**
     * Method that adds new cars and puts them in the corresponding queue
     * @param numberOfCars
     * @param type
     */
    private void addArrivingCars(int numberOfCars, String type){
        // Add the cars to the back of the queue.
        switch(type) {
            case AD_HOC:
                for (int i = 0; i < numberOfCars; i++) {
                    entranceCarQueue.addCar(new AdHocCar());
                }
                break;
            case PASS:
                for (int i = 0; i < numberOfCars; i++) {
                    entrancePassQueue.addCar(new ParkingPassCar());
                }
                break;
        }
    }

    private void carLeavesSpot(Car car){
        garagemodel.removeCarAt(car.getLocation());
        exitCarQueue.addCar(car);
    }
}
