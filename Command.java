package Commands;

import Vehicle.Vehicle;

import java.io.Serializable;

public class Command implements Serializable {
    private String name;
    private Integer Key;
    private Vehicle vehicle;
    private String answer;
    private Long ID;
    private Long number;
    private Float power;
    private String file_name;
    private String login;
    private String password;
    private static final long serialVersionUID = 33L;

    public Command(String name) {
        this.name = name;
    }
    public Command(){}

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public String getLogin() {
        return login;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setKey(Integer key) {
        Key = key;
    }

    public Integer getKey() {
        return Key;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getAnswer() {
        return answer;
    }

    public void setID(Long ID) {
        this.ID = ID;
    }

    public Long getID() {
        return ID;
    }

    public void setNumber(Long number) {
        this.number = number;
    }

    public Long getNumber() {
        return number;
    }

    public void setPower(Float power) {
        this.power = power;
    }

    public Float getPower() {
        return power;
    }

    public String getFile_name() {
        return file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }
}
