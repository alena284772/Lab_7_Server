package ServerSide;


import Vehicle.*;
import Commands.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class CollectionManager {

    private static String Type="LinkedHashMap";
    private static Logger logger=Logger.getLogger(CollectionManager.class.getName());

    private static Date date;
    public String answer;

    public LinkedHashMap<Integer, Vehicle> map;
    private File file_with_collection;
    private Connection connection;
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    Comparator<Vehicle> comparator = new Comparator<Vehicle>() {
        @Override
        public int compare(Vehicle o1, Vehicle o2) {
            return o1.getName().compareTo(o2.getName());
        }
    };

    public CollectionManager(Connection connection) throws SQLException {
        this.map = new LinkedHashMap<Integer,Vehicle>();
        CollectionManager.date=new Date();
        this.connection=connection;
    }

    public static Date getDate() {
        return date;
    }


    public static String getType() {
        return Type;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    @Override
    public String toString() {
        return "Type of collection: "+CollectionManager.getType()+" Map size: "+map.size();
    }

    public LinkedHashMap<Integer, Vehicle> getMap() {
        return map;
    }

    public void setMap(LinkedHashMap<Integer, Vehicle> map) {
        this.map = map;
    }

    public String getAnswer() {
        return answer;
    }
    public void clear(Command comin,Command command) throws SQLException {
        lock.writeLock().lock();

        int b= (int) map.entrySet().stream().filter(Vehicle->Vehicle.getValue().getUser().equals(comin.getLogin())).count();

        if(b>0){
            PreparedStatement pr=connection.prepareStatement("DELETE FROM M_A_VEHICLE WHERE User_name=(?)");
            pr.setString(1,comin.getLogin());
            pr.execute();
            pr.close();
            map.entrySet().stream().filter(Vehicle->Vehicle.getValue().getUser().equals(comin.getLogin())).forEach(Vehicle->map.remove(Vehicle.getKey()));
            command.setAnswer("Все принадлежащие Вам объекты были удалены");
        }else{
            command.setAnswer("В коллекции нет принадлежащих Вам объектов");
        }
        lock.writeLock().unlock();

    }

    public void remove_greater(Vehicle vehicle, Command command) throws SQLException {
        lock.writeLock().lock();

        int a= (int) map.entrySet().stream().filter(Vehicle-> Vehicle.getValue().compareTo(vehicle)>0).count();
        int b= (int) map.entrySet().stream().filter(Vehicle-> Vehicle.getValue().compareTo(vehicle)>0).filter(Vehicle->Vehicle.getValue().getUser().equals(vehicle.getUser())).count();
        List<Integer> k =new ArrayList<>();
        map.entrySet().stream().filter(Vehicle-> Vehicle.getValue().compareTo(vehicle)>0).filter(Vehicle->Vehicle.getValue().getUser().equals(vehicle.getUser())).forEach(Vehicle->k.add(Vehicle.getKey()));
        if(a>0) {
            if(b>0) {
                PreparedStatement preparedStatement=connection.prepareStatement("DELETE FROM M_A_VEHICLE WHERE Key_=(?);");
                for(int i=0;i<k.size();i++) {
                    preparedStatement.setInt(1,k.get(i));
                    preparedStatement.execute();

                }
                preparedStatement.close();
                k.stream().forEach(integer -> map.remove(integer));
                command.setAnswer("Objects exceeding this have been deleted.");
            }else{
                command.setAnswer("Objects exceeding this have another users.");
            }
        }else{
            command.setAnswer("Elements exceeding the given one do not exist.");
        }
        lock.writeLock().unlock();

    }

    public void remove_key(Integer Key, Command command, Command coming) throws SQLException {
        lock.writeLock().lock();
        int a= (int) map.entrySet().stream().filter(Vehicle -> Vehicle.getKey().equals(Key)).count();
        int b= (int) map.entrySet().stream().filter(Vehicle -> Vehicle.getKey().equals(Key)).filter(Vehicle->Vehicle.getValue().getUser().equals(coming.getLogin())).count();

        if(a>0) {
            if(b>0){
                PreparedStatement preparedStatement=connection.prepareStatement("DELETE FROM M_A_VEHICLE WHERE Key_=(?)");
                preparedStatement.setInt(1,Key);
                preparedStatement.execute();
                preparedStatement.close();

            map.remove(Key);
            command.setAnswer("Element was deleted");}
            else{
                command.setAnswer("Element was not deleted, because it has another user");
            }
        }else{
            command.setAnswer("Element was not deleted, because it doesn't exist");
        }
        lock.writeLock().unlock();

    }


    public void replace_if_greater(Integer Key, Vehicle vehicle, Command command) throws SQLException {
        lock.writeLock().lock();
        int a= (int) map.entrySet().stream().filter(Vehicle -> Vehicle.getKey().equals(Key)).filter(Vehicle-> Vehicle.getValue().compareTo(vehicle)<0).count();
        int b= (int) map.entrySet().stream().filter(Vehicle -> Vehicle.getKey().equals(Key)).filter(Vehicle-> Vehicle.getValue().compareTo(vehicle)<0).filter(Vehicle->Vehicle.getValue().getUser().equals(vehicle.getUser())).count();

        if(a>0){
            if(b>0) {
                PreparedStatement prs =connection.prepareStatement(
                        "UPDATE M_A_VEHICLE SET Name=(?), Coordinate_x=(?), Coordinate_y=(?), Creating_Date=(?), Engine_Power=(?), Number_of_wheels=(?), Type_=(?), Fuel_Type=(?) WHERE Key_=(?);");
                prs.setString(1, vehicle.getName());
                prs.setDouble(2, vehicle.getCoordinate_x());
                prs.setLong(3, vehicle.getCoordinate_y());
                prs.setTimestamp(4, vehicle.getCreationDate());
                prs.setFloat(5, vehicle.getEnginePower());
                prs.setLong(6, vehicle.getNumberOfWheels());
                prs.setString(7, String.valueOf(vehicle.getType()));
                prs.setString(8, String.valueOf(vehicle.getFuelType()));
                prs.setInt(9, Key);
                prs.execute();
                prs.close();
                Long [] Id=new Long[1];
                map.entrySet().stream().filter(Vehicle -> Vehicle.getKey().equals(Key)).forEach(Vehicle -> Id[0] =Vehicle.getValue().getId());
                vehicle.setId(Id[0]);
                map.entrySet().stream().filter(Vehicle -> Vehicle.getKey().equals(Key)).forEach(Vehicle -> map.replace(Key,vehicle));
                command.setAnswer("Element value has been changed");
            }else{
                command.setAnswer("Element value has NOT been changed because it has another user");
            }
        }else{
            command.setAnswer("Element value has NOT been changed because element with this key doesn't exist");
        }
        lock.writeLock().unlock();

    }


    public void remove_any_by_number_of_wheels(Long number, Command command, Command comin) throws SQLException {
        lock.writeLock().lock();
        int a= (int) map.entrySet().stream().filter(Vehicle -> Vehicle.getValue().getNumberOfWheels().equals(number)).count();
        int b= (int) map.entrySet().stream().filter(Vehicle -> Vehicle.getValue().getNumberOfWheels().equals(number)).filter(Vehicle->Vehicle.getValue().getUser().equals(comin.getLogin())).count();
        if(a>0){
            if(b>0){
                Integer[] l=new Integer[1];
                map.entrySet().stream().filter(Vehicle -> Vehicle.getValue().getNumberOfWheels().equals(number)).filter(Vehicle->Vehicle.getValue().getUser().equals(comin.getLogin())).limit(1).forEach(Vehicle -> l[0]=Vehicle.getKey());
                PreparedStatement preparedStatement=connection.prepareStatement("DELETE FROM M_A_VEHICLE WHERE Key_=(?);");
                preparedStatement.setInt(1,l[0]);
                preparedStatement.execute();
                preparedStatement.close();

            map.remove(l[0]);
            command.setAnswer("Object is deleted");}
            else {
                command.setAnswer("The object was not deleted, because an object with this number of wheels has another user");
            }
        }else {
            command.setAnswer("The object was not deleted, because an object with this number of wheels does not exist.");
        }
        lock.writeLock().unlock();


    }


    public void count_less_than_engine_power(Float power, Command command){
        lock.readLock().lock();
        int count= (int) map.entrySet().stream().filter(Vehicle-> Vehicle.getValue().getEnginePower()<power).count();
        command.setAnswer("Number of objects is " + count);
        lock.readLock().unlock();
    }

    public void update(Long ID, Vehicle vehicle, Command command) throws SQLException{
        lock.writeLock().lock();
        vehicle.setId(ID);
        int a= (int) map.entrySet().stream().filter(Vehicle-> map.get(Vehicle.getKey()).getId()==ID).count();

        int b= (int) map.entrySet().stream().filter(Vehicle-> map.get(Vehicle.getKey()).getId()==ID).filter(Vehicle->map.get(Vehicle.getKey()).getUser().equals(vehicle.getUser())).count();

        if(a>0){
            if(b>0){
                PreparedStatement ps=connection.prepareStatement("UPDATE M_A_VEHICLE SET Name=?, Coordinate_x=?, Coordinate_y=?, Creating_Date=?, Engine_Power=?, Number_of_wheels=?, Type_=?, Fuel_Type=?, User_name=? WHERE Id=?");
                ps.setString(1, vehicle.getName());
                ps.setDouble(2, vehicle.getCoordinate_x());
                ps.setLong(3, vehicle.getCoordinate_y());
                ps.setTimestamp(4, vehicle.getCreationDate());
                ps.setFloat(5, vehicle.getEnginePower());
                ps.setLong(6, vehicle.getNumberOfWheels());
                ps.setString(7, String.valueOf(vehicle.getType()));
                ps.setString(8, String.valueOf(vehicle.getFuelType()));
                ps.setString(9, vehicle.getUser());
                ps.setLong(10, ID);
                ps.execute();
                ps.close();

            vehicle.setId(ID);
            map.entrySet().stream().filter(Vehicle-> map.get(Vehicle.getKey()).getId()==ID).forEach(Vehicle->map.put(Vehicle.getKey(),vehicle));
            command.setAnswer("Object with this id is update");}else{
                command.setAnswer("The item was not updated because the item with this ID has another user");
            }
        }else {
            command.setAnswer("The item was not updated because the item with this ID does not exist.");
        }
        lock.writeLock().unlock();

    }


    public void insert(Integer Key, Vehicle vehicle, Command command) {
        lock.writeLock().lock();
        int a= (int) map.entrySet().stream().filter(Vehicle -> Vehicle.getKey().equals(Key)).count();
        if(a!=0){
            command.setAnswer("An object with this key already exists");
        }else {
            try {
                PreparedStatement ps = connection.prepareStatement("INSERT INTO M_A_VEHICLE(Key_, Name, Coordinate_x, Coordinate_y, Creating_Date, Engine_Power, Number_of_wheels, Type_, Fuel_Type, User_name) " +

                        "VALUES ((?),(?),(?),(?),(?),(?),(?),(?),(?),(?))");
                ps.setInt(1, Key);

                ps.setString(2, vehicle.getName());
                ps.setDouble(3, vehicle.getCoordinate_x());
                ps.setLong(4, vehicle.getCoordinate_y());
                ps.setTimestamp(5, vehicle.getCreationDate());
                ps.setFloat(6, vehicle.getEnginePower());
                ps.setLong(7, vehicle.getNumberOfWheels());
                ps.setString(8, String.valueOf(vehicle.getType()));
                ps.setString(9, String.valueOf(vehicle.getFuelType()));
                ps.setString(10, vehicle.getUser());
                ps.execute();
                ps.close();

                Statement stmt = connection.createStatement();
                ResultSet resultSet = stmt.executeQuery("SELECT * FROM M_A_VEHICLE WHERE Id=CURRVAL('M_A_id_seq')");
                while (resultSet.next()) {
                    Vehicle veh = new Vehicle();
                    int key = resultSet.getInt("Key_");
                    veh.setId(resultSet.getLong("Id"));
                    veh.setName(resultSet.getString("Name"));
                    veh.setCoordinates(resultSet.getDouble("Coordinate_x"), resultSet.getLong("Coordinate_y"));
                    veh.setCreationDate(resultSet.getTimestamp("Creating_Date"));
                    veh.setEnginePower(resultSet.getFloat("Engine_Power"));
                    veh.setNumberOfWheels(resultSet.getLong("Number_of_wheels"));
                    veh.setType(resultSet.getString("Type_"));
                    veh.setFuelType(resultSet.getString("Fuel_Type"));
                    veh.setUser(resultSet.getString("User_name"));
                    map.put(key, veh);

                    logger.info(veh.getName() + " добавлена");

                }
                resultSet.close();
                stmt.close();
                command.setAnswer("Object was insert");

            } catch (SQLException | EnginePowerException | NumberOfWheelsException | VehicleTypeException | FuelTypeException throwables) {
                throwables.printStackTrace();
            }
        }
        lock.writeLock().unlock();


    }


    public void show(Command command){
lock.readLock().lock();
        orderByValue(map,comparator);
        ArrayList<String> Str = new ArrayList<>();
        map.entrySet().stream().forEach(map->Str.add("Key="+map.getKey()+"Value="+map.getValue().toString()));
        command.setAnswer(String.valueOf(Str));
lock.readLock().unlock();

    }
    static <K, V> void orderByValue(
            LinkedHashMap<K, V> m, final Comparator<? super V> c) {
        List<Map.Entry<K, V>> entries = new ArrayList<>(m.entrySet());

        Collections.sort(entries, new Comparator<Map.Entry<K, V>>() {
            @Override
            public int compare(Map.Entry<K, V> lhs, Map.Entry<K, V> rhs) {
                return c.compare(lhs.getValue(), rhs.getValue());
            }
        });

        m.clear();
        for(Map.Entry<K, V> e : entries) {
            m.put(e.getKey(), e.getValue());
        }
    }


    public void read_collection(Statement stat, Command command) throws SQLException, EnginePowerException, NumberOfWheelsException, VehicleTypeException, FuelTypeException {
        lock.readLock().lock();
        ResultSet resultSet = stat.executeQuery("SELECT * FROM M_A_VEHICLE");
        while (resultSet.next()) {
            Vehicle vehicle = new Vehicle();
            int key = resultSet.getInt("Key_");
            vehicle.setId(resultSet.getLong("Id"));
            vehicle.setName(resultSet.getString("Name"));
            vehicle.setCoordinates(resultSet.getDouble("Coordinate_x"), resultSet.getLong("Coordinate_y"));
            vehicle.setCreationDate(resultSet.getTimestamp("Creating_Date"));
            vehicle.setEnginePower(resultSet.getFloat("Engine_Power"));
            vehicle.setNumberOfWheels(resultSet.getLong("Number_of_wheels"));
            vehicle.setType(resultSet.getString("Type_"));
            vehicle.setFuelType(resultSet.getString("Fuel_Type"));
            vehicle.setUser(resultSet.getString("User_name"));
            map.put(key, vehicle);

        }
        command.setAnswer("Collection was read");
        lock.readLock().unlock();
    }
    public void avtor(Command command,String login,String password){
        lock.readLock().lock();
    if(suchUserExists(login)){
       if(passwordIsCorrect(login, password)){
           command.setAnswer("Авторизация прошла успешно.");
       }else{
           command.setAnswer("Password is uncorrect");
       }
} else{
        command.setAnswer("A user with this username does not exist");
    }
    lock.readLock().unlock();
    }
    public boolean suchUserExists(String login){
        boolean bool = false;
        try{
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM M_A_USERS WHERE User_name = (?)");
            ps.setString(1, login);
            ResultSet rs = ps.executeQuery();
            while (rs.next()){
                bool = true;
            }
            rs.close();
        } catch (SQLException e) {
            logger.severe(e.getMessage());
            System.exit(0);
        }
        return bool;
    }
    public boolean passwordIsCorrect(String login, String password){
        boolean bool = false;
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM M_A_USERS WHERE user_name = (?)");
            ps.setString(1, login);
            ResultSet rs = ps.executeQuery();
            while (rs.next()){
                String p=rs.getString("password_hash");
                if (p.equals(password)){
                    bool = true;
                }
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            logger.severe(e.getMessage());
            System.exit(0);
        }
        return bool;
    }
    public void reg(Command command,String login,String password) throws SQLException {
        lock.writeLock().lock();
        if(suchUserExists(command.getLogin())){
            command.setAnswer("Пользователь с таким именем уже существует");
        }else {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO M_A_USERS(User_name,password_hash) VALUES ((?), (?))");
            ps.setString(1, login);
            ps.setString(2, password);
            ps.execute();
            ps.close();
            command.setAnswer("Пользователь добавлен");
        }
        lock.writeLock().unlock();
    }
    public boolean check_user(String login, String password){
        lock.readLock().lock();
        boolean agr=false;
        if(suchUserExists(login)) {
            if (passwordIsCorrect(login, password)) {
                agr = true;
            }
        }
        lock.readLock().unlock();
        return agr;

    }


}