package ServerSide;


import Commands.Command;
import Vehicle.EnginePowerException;
import Vehicle.FuelTypeException;
import Vehicle.NumberOfWheelsException;
import Vehicle.VehicleTypeException;
import org.omg.IOP.TAG_JAVA_CODEBASE;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class ServerConnection {
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    Scanner console=new Scanner(System.in);
    private Boolean isworking = true;
    private static Logger logger=Logger.getLogger(ServerConnection.class.getName());
    private ExecutorService reader = Executors.newFixedThreadPool(3);
    private ExecutorService worker = Executors.newCachedThreadPool();
    private ExecutorService sender = Executors.newCachedThreadPool();
    public static final String DB_URL = "jdbc:postgresql://pg/studs";
    public static final String DB_Driver = "org.postgresql.Driver";
    CollectionManager collectionManager;


    public void work() throws ClassNotFoundException, SQLException, IOException, InterruptedException {
        Class.forName(DB_Driver); //Проверяем наличие JDBC драйвера для работы с БД
        String fileName = "1.txt";
        Path path = Paths.get(fileName);
        Scanner in = new Scanner(path);
        String l=in.next();
        String p=in.next();
        Thread keyBoard = new Thread(this::keyBoardWork);
        keyBoard.setDaemon(false);
        Connection connection = DriverManager.getConnection(DB_URL, l, p);//соединениесБД
        System.out.println("Соединение с СУБД выполнено.");
        Statement stat = connection.createStatement();
        collectionManager=new CollectionManager(connection);

        stat.execute("CREATE TABLE IF NOT EXISTS M_A_USERS(User_name text unique, password_hash TEXT );");
        //stat.execute("TRUNCATE TABLE M_A_USERS");

        stat.execute("CREATE SEQUENCE IF NOT EXISTS M_A_id_seq START 1 INCREMENT 1;");

        stat.execute("CREATE TABLE IF NOT EXISTS M_A_VEHICLE\n" +
                "(\n" +
                "Key_ INT,\n" +
                "    Id integer DEFAULT nextval('M_A_id_seq'),\n" +
                "    Name TEXT,\n" +
                "    Coordinate_x DOUBLE PRECISION,\n" +
                "    Coordinate_y BIGINT,\n" +
                "    Creating_Date TIMESTAMP,\n" +
                "    Engine_Power FLOAT, \n" +
                "    Number_of_wheels BIGINT ,\n" +
                "    Type_ TEXT,\n" +
                "    Fuel_Type TEXT,\n" +
                "    User_name TEXT\n" +
                ");");
        System.out.println("Enter port:");
        Scanner scanner=new Scanner(System.in);
        int port = -1;
        while (port == -1){
            try{
                int a = Integer.valueOf(scanner.nextLine().trim());
                if (a<0 || a > 65535){
                    logger.info("Wrong port was entered. Port should be a number from 0 to 65535");
                }else{
                    port = a;
                    logger.info("Port is now: "+port);
                }
            }catch (NumberFormatException e){
                logger.info("Entered value is not a number");
            }

        }

            try {

                Command command=new Command();
                collectionManager.read_collection(stat,command);
                System.out.println(command.getAnswer());
                keyBoard.start();

                    while (true) {
                        ServerSocket ss = new ServerSocket(port);
                        try {
                            while (true){

                                read(ss.accept());
                            }

                        } catch (EOFException e) {
                            logger.info("Cannot reading");
                            isworking=false;
                        }
                    }

            } catch (IOException e) {
                e.printStackTrace();
            }  catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NumberFormatException e) {
                e.printStackTrace();
            } catch (FuelTypeException e) {
                e.printStackTrace();
            } catch (EnginePowerException e) {
                e.printStackTrace();
            } catch (NumberOfWheelsException e) {
                e.printStackTrace();
            } catch (VehicleTypeException e) {
                e.printStackTrace();
            }

    }


    public void read(Socket socket) throws  IOException, ClassNotFoundException {
if(isworking){
            reader.execute(() -> {
                try {
                    Command command = new Command();

                    logger.info("ServerSocket awaiting connections...");

                    logger.info("Connection from " + socket + "!");
                    OutputStream out = socket.getOutputStream();
                    while (command.getName() == null) {
                        logger.info("Server reading command from client");
                        InputStream inputStream = socket.getInputStream();
                        byte[] bytes = new byte[163840];
                        inputStream.read(bytes);
                        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
                        Object object = objectInputStream.readObject();
                        Command com = (Command) object;
                        command.setID(com.getID());
                        command.setName(com.getName());
                        command.setKey(com.getKey());
                        command.setVehicle(com.getVehicle());
                        command.setNumber(com.getNumber());
                        command.setPower(com.getPower());
                        command.setFile_name(com.getFile_name());
                        command.setLogin(com.getLogin());
                        command.setPassword(com.getPassword());
                        byteArrayInputStream.close();
                        objectInputStream.close();
                    }
                    exe(command, out);
                } catch (IOException | ClassNotFoundException e) {
                    //e.printStackTrace();
                    isworking = false;
                }

            });}

    }

    public void send(Command command,OutputStream out) throws IOException {
       sender.execute(()-> {
           try {
               byte[] bytes = new byte[163840];
               ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
               ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
               objectOutputStream.writeObject(command);
               objectOutputStream.flush();
               bytes = byteArrayOutputStream.toByteArray();
               byteArrayOutputStream.flush();
               out.write(bytes);
               objectOutputStream.close();
               byteArrayOutputStream.close();
               logger.info("Server send a message");
           } catch (IOException e) {
               logger.info("MISTAKE ! Server cannot send a message");
               e.printStackTrace();
           }
       });
    }
    public void exe(Command comin,OutputStream out){
        worker.execute(()->{
            Command command=new Command();
            switch (comin.getName()) {
                case ("avtor"):
                    logger.info("User authorization is performed");
                    collectionManager.avtor(command, comin.getLogin(), comin.getPassword());
                    break;
                case ("reg"):
                    logger.info("A new user is being added");
                    try {
                        collectionManager.reg(command, comin.getLogin(), comin.getPassword());
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                    break;

                case ("info"):
                    if(collectionManager.check_user(comin.getLogin(),comin.getPassword())){
                    logger.info("The server executes the command: info");
                    command.setAnswer(collectionManager.toString());
                    }else{
                        command.setAnswer("У Вас нет прав выполнять команды.");
                    }
                    break;
                case ("clear"):
                    if(collectionManager.check_user(comin.getLogin(),comin.getPassword())){
                    logger.info("The server executes the command: clear");
                    try {
                        collectionManager.clear(comin, command);
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                    }else{
                        command.setAnswer("У Вас нет прав выполнять команды.");
                    }
                    break;

                case ("remove_greater"):
                    if(collectionManager.check_user(comin.getLogin(),comin.getPassword())){
                    logger.info("The server executes the command: remove_greater");
                    try {
                        collectionManager.remove_greater(comin.getVehicle(), command);
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                    }else{
                        command.setAnswer("У Вас нет прав выполнять команды.");
                    }
                    break;
                case ("insert"):
                    if(collectionManager.check_user(comin.getLogin(),comin.getPassword())){
                    logger.info("The server executes the command: insert");
                    collectionManager.insert(comin.getKey(), comin.getVehicle(), command);
                    }else{
                        command.setAnswer("У Вас нет прав выполнять команды.");
                    }
                    break;
                case ("show"):
                    if(collectionManager.check_user(comin.getLogin(),comin.getPassword())){
                    logger.info("The server executes the command: show");
                    collectionManager.show(command);
                    }else{
                        command.setAnswer("У Вас нет прав выполнять команды.");
                    }
                    break;
                case ("remove_key"):
                    if(collectionManager.check_user(comin.getLogin(),comin.getPassword())){
                    logger.info("The server executes the command: remove_key");
                    try {
                        collectionManager.remove_key(comin.getKey(), command, comin);
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                    }else{
                        command.setAnswer("У Вас нет прав выполнять команды.");
                    }
                    break;
                case ("replace_if_greater"):
                    if(collectionManager.check_user(comin.getLogin(),comin.getPassword())){
                    logger.info("The server executes the command: replace_if_greater");
                    try {
                        collectionManager.replace_if_greater(comin.getKey(), comin.getVehicle(), command);
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                    }else{
                        command.setAnswer("У Вас нет прав выполнять команды.");
                    }
                    break;
                case ("remove_any_by_number_of_wheels"):
                    if(collectionManager.check_user(comin.getLogin(),comin.getPassword())){
                    logger.info("The server executes the command: remove_any_by_number_of_wheels");
                    try {
                        collectionManager.remove_any_by_number_of_wheels(comin.getNumber(), command, comin);
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                    }else{
                        command.setAnswer("У Вас нет прав выполнять команды.");
                    }
                    break;
                case ("count_less_than_engine_power"):
                    if(collectionManager.check_user(comin.getLogin(),comin.getPassword())){
                    logger.info("The server executes the command: count_less_than_engine_power");
                    collectionManager.count_less_than_engine_power(comin.getPower(), command);
                    }else{
                        command.setAnswer("У Вас нет прав выполнять команды.");
                    }
                    break;
                case ("update"):
                    if(collectionManager.check_user(comin.getLogin(),comin.getPassword())){
                    logger.info("The server executes the command: update");
                    try {
                        collectionManager.update(comin.getID(), comin.getVehicle(), command);
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                    }else{
                        command.setAnswer("У Вас нет прав выполнять команды.");
                    }
                    break;
                default:
                    logger.info("This is an unknown command.");
                    break;
            }

            try {
                send(command,out);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
    public void keyBoardWork() {
        try (Scanner input = new Scanner(System.in)) {
            while (true) {
                System.out.print("//: ");
                if (input.hasNextLine()) {
                    String inputString = input.nextLine();
                    if ("exit".equals(inputString)) {
                        logger.info("Command 'exit' from console.");
                        isworking=false;
                        Thread.sleep(1000);
                        reader.shutdown();
                        worker.shutdown();
                        sender.shutdown();
                        System.exit(0);
                    } else {
                        logger.info("Available command: 'exit'.");
                    }
                } else {
                    System.exit(0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
