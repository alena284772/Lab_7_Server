package Vehicle;

import Commands.Id;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Scanner;

public class Vehicle implements Comparable<Vehicle>, Serializable {

    private Long id;

    private String name;

    private Coordinates coordinates;

    private Timestamp creationDate;

    private Float enginePower;

    private Long numberOfWheels;

    private VehicleType type;

    private FuelType fuelType;
    private String User;
    private static final long serialVersionUID = 10L;


    @Override
    public String toString() {
        return " Vehicle Name: "+getName()+" "+" Vehicle coordinates x y: "+getCoordinates(this.c)+" "+" Vehicle id: "+getId()+" "+" Vehicle Creation Date: "+getCreationDate()+" Engine Power: "+getEnginePower()+" Number Of Wheels: "+getNumberOfWheels()
                +" Vehicle type: "+getType()+" FuelType: "+getFuelType();
    }

public Vehicle(){}
    public Vehicle(String name,double x,long y,String creationdate ,float enginePower,long numberOfWheels, String type, String fuelType,String User) throws VehicleTypeException, FuelTypeException {

        this.name=name;
        this.setUser(User);
        this.setCoordinates(x,y);
        this.creationDate= Timestamp.valueOf(LocalDateTime.parse(creationdate));
        try {
            this.setEnginePower(enginePower);
        } catch (EnginePowerException e) {
            System.err.println(e);
        }
        try {
            this.setNumberOfWheels(numberOfWheels);
        } catch (NumberOfWheelsException e) {
            System.err.println(e);
        }
        this.setType(type);
        this.setFuelType(fuelType);
    }


    public Vehicle(Scanner scanner){


        System.out.println("Name:");
        String name = scanner.next();
        double x = -10000;
        long y=-10000;
        this.setName(name);
        while (x <=-815||y<=-774) {
            System.out.println("Coordinate x:");
            while (scanner.hasNextDouble()==false){
                System.out.println("X is a double variable. Try again");
                scanner.nextLine();
            }
            x = scanner.nextDouble();
            scanner.nextLine();
            System.out.println("Coordinate y:");
            while (scanner.hasNextLong()==false){
                System.out.println("Y is a long variable. Try again");
                scanner.nextLine();
            }
            y = scanner.nextLong();
            this.setCoordinates(x, y);
        }
        this.setCreationDate();
        while (this.enginePower==null) {
            System.out.println("Engine Power:");
            while (scanner.hasNextFloat()==false){
                System.out.println("Engine Power is a float variable. Try again");
                scanner.nextLine();
            }
            Float Engine = scanner.nextFloat();
            try {this.setEnginePower(Engine);} catch (EnginePowerException E){System.err.println(E);}
        }
        while (this.numberOfWheels==null) {
            System.out.println("Number of wheels:");
            while (scanner.hasNextLong()==false){
                System.out.println("Number of wheels is a long variable. Try again");
                scanner.nextLine();
            }
            long number = scanner.nextLong();
            try {
                this.setNumberOfWheels(number);
            } catch (NumberOfWheelsException e) {
                System.err.println(e);
            }
        }
        while (this.getType()==null) {
            System.out.println("Vehicle types:" +
                    " CAR,\n" +
                    "    PLANE,\n" +
                    "    BICYCLE,\n" +
                    "    MOTORCYCLE,\n" +
                    "    HOVERBOARD");
            String Type = scanner.next();
            try {
                this.setType(Type);
            } catch (VehicleTypeException e) {
                System.out.println("Try again");
            }
        }
        while (this.getFuelType()==null) {
            System.out.println("Fuel types:" +
                    "KEROSENE,\n" +
                    "    ELECTRICITY,\n" +
                    "    NUCLEAR,\n" +
                    "    ANTIMATTER");
            String type = scanner.next();
            try {
                this.setFuelType(type);
            } catch (FuelTypeException e) {
                System.out.println("Try again");
            }
        }
        scanner.nextLine();
    }

    public String getUser() {
        return User;
    }

    public void setUser(String user) {
        User = user;
    }

    public void setId(long id1) {
        this.id=id1;

    }

    public long getId() {
        return id;
    }
    public double getCoordinate_x(){
        return c.getX();
    }
    public long getCoordinate_y(){
        return c.getY();
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }

    public Coordinates c=new Coordinates();


    public void setCoordinates (double x, long y) {

        try {
            this.c.setX(x);
        } catch (CoordinateException e) {
            System.err.println(e);
        }
        try {
            this.c.setY(y);
        } catch (CoordinateException e) {
            System.err.println(e);
        }
    }
    public String getCoordinates(Coordinates c) {
        return c.getX()+" "+c.getY();
    }


    public void setCreationDate() {
        this.creationDate = Timestamp.valueOf(LocalDateTime.now());
    }
    public Timestamp getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Timestamp creationDate) {
        this.creationDate = creationDate;
    }

    public void setEnginePower(Float enginePower) throws EnginePowerException {
        if (enginePower<0){
            throw new EnginePowerException("Field value must be greater than 0");
        } else
        {this.enginePower=enginePower;
        }
    }

    public Float getEnginePower() {
        return enginePower;
    }


    public void setFuelType (String fueltype) throws FuelTypeException {
        boolean exist = true;
        try {
            FuelType.valueOf(fueltype);
        } catch (Exception E){
            exist=false;
        }
        if(exist==false){
            throw new FuelTypeException("Fuel type is false");

        } else {this.fuelType = FuelType.valueOf(fueltype);}
    }
    public FuelType getFuelType() {
        return fuelType;
    }


    public void setNumberOfWheels(Long numberOfWheels) throws NumberOfWheelsException {
        if (numberOfWheels<0){
            throw new NumberOfWheelsException("Field value must be greater than 0");
        } else
        {this.numberOfWheels=numberOfWheels;
        }
    }
    public Long getNumberOfWheels() {
        return numberOfWheels;
    }


    public void setType (String type) throws VehicleTypeException {
        boolean exist = true;
        try {
            VehicleType.valueOf(type);
        } catch (Exception E){
            exist=false;
        }
        if(exist==false){
            throw new VehicleTypeException("Type is false");
        } else {this.type = VehicleType.valueOf(type);}
    }
    public VehicleType getType() {
        return type;
    }


    @Override
    public int compareTo(Vehicle o) {
        int result=this.name.length()-o.name.length();
        if(result==0){
            result=this.enginePower.compareTo(o.enginePower);
        }
        return  result;
    }
}