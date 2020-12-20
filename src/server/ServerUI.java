package server;


import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Scanner;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javafx.stage.WindowEvent;
import util.*;


import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.text.*;
import javafx.fxml.FXMLLoader;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;

class ServiceThread implements Runnable{
    private Thread thr ;
    private NetworkUtil networkUtil;

    public ServiceThread(NetworkUtil netu){
        this.networkUtil = netu;
        this.thr = new Thread(this);
        thr.start();
    }

    private int checkUser(User user){
        int userType = -1;
        for(User usr : Server.userList ){
            if( (usr.username).equals(user.username) && (usr.password).equals(user.password) ){
                userType = usr.type;
                break;
            }
        }
        return userType;
    }

    private void search(String regN, String carMake, String carModel ) throws IOException, ClassNotFoundException {
        int cnt = 0;
        for(Car car: Server.carList ){
            if( (regN.toLowerCase().equals("") || car.getRegistrationNumber().toLowerCase().equals(regN.toLowerCase()) ) &&
                    (carMake.toLowerCase().equals("") || car.getCarMake().toLowerCase().equals(carMake.toLowerCase())) &&
                    (carModel.toLowerCase().equals("") || car.getCarModel().toLowerCase().equals(carModel.toLowerCase()))
            ){
                networkUtil.write(car);
                cnt += 1;
            }
        }

        networkUtil.write(NetworkCipher.SUCCESS);
       // if( cnt == 0) networkUtil.write("No such car with this Registration Number") ;
    }

    private void add(Car newCar) throws IOException, ClassNotFoundException {
       /* networkUtil.write("Registration Number: ");
        String regNum = (String) networkUtil.read();
        networkUtil.write("Year Made: ");
        String yearMade = (String) networkUtil.read();
        networkUtil.write("Colors[Color1,Color2,Color3]: ");
        String color1 = (String) networkUtil.read();
        String color2 = (String) networkUtil.read();
        String color3 = (String) networkUtil.read();
        networkUtil.write("Car Make: ");
        String carMake = (String) networkUtil.read();
        networkUtil.write("Car Model: ");
        String carModel = (String) networkUtil.read();
        networkUtil.write("Price: ");
        String price = (String) networkUtil.read();
        networkUtil.write("Quantity: ");
        String quantity = (String) networkUtil.read();

        Car temp = new Car(regNum,Integer.parseInt(yearMade),new String[]{color1,color2,color3},carMake,carModel,Integer.parseInt(price),Integer.parseInt(quantity));
       */
        boolean canAdd = true;
        for(Car car: Server.carList)
            if ( newCar.getRegistrationNumber().toLowerCase().equals(car.getRegistrationNumber().toLowerCase()) ) {
                canAdd = false;
                break;
            }

        if( canAdd ){
            Server.carList.add(newCar);
            networkUtil.write(NetworkCipher.SUCCESS);
        }
        else {
            networkUtil.write(NetworkCipher.FAILED);
        }
    }

    private void buy(String regN) throws IOException, ClassNotFoundException{
        int searchIndex = -1;

        for (int i = 0; i < Server.carList.size(); i++) {
            Car t = Server.carList.get(i);
            if (t.getRegistrationNumber().toLowerCase().equals(regN.toLowerCase())) {
                searchIndex = i;
                break;
            }
        }

        if( searchIndex == -1 ){
            networkUtil.write(NetworkCipher.FAILED);
        }
        else {
            try{
                Server.carList.get(searchIndex).buy();
                networkUtil.write(NetworkCipher.SUCCESS);
            } catch (IllegalStateException e){
                networkUtil.write(NetworkCipher.FAILED);
            }
        }

    }

    private void delete(String regN) throws IOException, ClassNotFoundException {
        int searchIndex = -1;

        for (int i = 0; i < Server.carList.size(); i++) {
            Car t = Server.carList.get(i);
            if (t.getRegistrationNumber().toLowerCase().equals(regN.toLowerCase()) ) {
                searchIndex = i;
                break;
            }
        }
        if (searchIndex != -1){
           /* System.out.println("Deleted : ") ;
            System.out.println(carList.get(searchIndex));

            */
            networkUtil.write(NetworkCipher.SUCCESS);
            Server.carList.remove(searchIndex);
        }
        else {
          //  System.out.println("No such car with Registration Number" + regN ) ;
            networkUtil.write(NetworkCipher.FAILED);
        }
    }

    private void modify(String regN, Car newCar) throws IOException, ClassNotFoundException {
        int searchIndex = -1;

        for (int i = 0; i < Server.carList.size(); i++) {
            Car t = Server.carList.get(i);
            if (t.getRegistrationNumber().toLowerCase().equals(regN.toLowerCase())) {
                searchIndex = i;
                break;
            }
        }

        if (searchIndex == -1) {
            networkUtil.write(NetworkCipher.FAILED);
        }
        else {
            boolean regExists = false;

            for(int i=0;i<Server.carList.size();i++){
                if( i == searchIndex) continue;

                Car t = Server.carList.get(i);
                if( t.getRegistrationNumber().toLowerCase().equals(newCar.getRegistrationNumber().toLowerCase()) ){
                    regExists = true;
                    break;
                }
            }

            if( !regExists ) {
                Server.carList.set(searchIndex, newCar);
                networkUtil.write(NetworkCipher.SUCCESS);
            }
            else
                networkUtil.write(NetworkCipher.FAILED);
        }
    }

    public void run() {
        try {
            while (true) {
             //   String temp[] = ((String) networkUtil.read()).split(",",2);
              //  User user = new User(temp[0],temp[1]);
                User user = (User) networkUtil.read();

                if (user.username == null && user.password == null){
                    // networkUtil.write("Username and password don't match or not registered!!") ;
                    networkUtil.write(NetworkCipher.FAILED) ;
                    continue;
                }
                int uType = checkUser(user);
                if( uType == -1 ){
                    // networkUtil.write("Username and password don't match or not registered!!") ;
                    networkUtil.write(NetworkCipher.FAILED);
                    continue;
                }
                networkUtil.write(uType);


                while( true ){
                    int command = (Integer) networkUtil.read();
                    if( command == NetworkCipher.LOGOUT)
                        break;

                    if( command == Command.ADD) {
                        Car car = (Car) networkUtil.read();
                        add(car);
                    }
                    else if( command == Command.BUY) {
                        String regN = (String) networkUtil.read();
                        buy(regN);
                    }
                    else if( command == Command.VIEWALL) {
                        search("", "", "");
                    }
                    else if(command == Command.SEARCH) {
                        String regN = (String) networkUtil.read();
                        String carMake = (String) networkUtil.read();
                        String carModel = (String) networkUtil.read();
                        search(regN, carMake, carModel);
                    }
                    else if(command == Command.DELETE) {
                        String regN = (String) networkUtil.read();
                        delete(regN);
                    }
                    else if(command == Command.EDIT) {
                        String regN = (String) networkUtil.read();
                        Car car = (Car) networkUtil.read();
                        modify(regN, car);
                    };
                }
            }

        } catch (Exception e) {
            System.out.println("In ServiceThread: " + e);
            System.out.println("Closing....");
        } finally {
            try {
                networkUtil.closeConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}



class Server implements Runnable {
    public static final List<Car> carList = new ArrayList<Car>();
    public static final List<User> userList = new ArrayList<User>();
    private static final String userListFile = "src\\server\\user.txt" ;
    private static final String carListFile = "src\\server\\car.txt" ;

    private static ServerSocket serverSocket;
    //   private static Socket clientSocket;

    private static Scanner input = new Scanner(System.in);

    private Thread thr;
    public  static boolean serverRunning = true;

    //  public static TextFlow message;
    public static StringBuilder message = new StringBuilder();

    public Server(){
        thr = new Thread(this);
        thr.start();
    }

    public static void loadAll(){
        try{
            String line ;
            BufferedReader br = new BufferedReader(new FileReader(userListFile)) ;

            while (true){
                line = br.readLine();
                if( line == null ) break;

                String usr[] = line.split(",",3);

                userList.add(new User(usr[0],usr[1],Integer.parseInt(usr[2])) ) ;
            }
            br.close();
        } catch(Exception e){
            e.printStackTrace();
        }

        try{
            String line ;
            BufferedReader br = new BufferedReader(new FileReader(carListFile)) ;

            while (true){
                line = br.readLine();
                if( line == null ) break;

                String attr[] = line.split(",") ;

                carList.add(new Car(attr[0], Integer.parseInt(attr[1]), new String[]{attr[2],attr[3],attr[4]}, attr[5], attr[6], Integer.parseInt(attr[7]), Integer.parseInt(attr[8]) ) ) ;
            }
            br.close();
        }
        catch (Exception e){
            e.printStackTrace();
            System.out.println("Loading failed...closing");
            System.exit(-1);
        }

        System.out.println("Loading successful");
    }

    public static void saveAll(){
        try{
            BufferedWriter bw = new BufferedWriter(new FileWriter(userListFile));
            for(User user: userList){
                bw.write(user.toString());
                bw.write("\n");
            }
            bw.close();
        } catch (Exception e){
            e.printStackTrace();
        }

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(carListFile));
            for(Car car: carList){
                bw.write(car.formatW()) ;
                bw.write("\n");
            }
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Saving failed....closing");
            System.exit(-1);
        }

        System.out.println("Saving successful...");
    }



    public void run(){
        try{
            serverSocket = new ServerSocket(420);
            while(serverRunning == true){
                Socket clientSocket = serverSocket.accept();
                // message.getChildren().add(new Text("New Connection : " + clientSocket.toString())) ;
                message.append("New Connection: "+ clientSocket.toString() );
                NetworkUtil netu = new NetworkUtil(clientSocket);
                new ServiceThread(netu);
            }
        } catch(Exception e){
            e.printStackTrace();
        } finally{
            try{
                serverSocket.close();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}

public class ServerUI extends Application {
    @FXML
    public TextFlow textFlow;
    FXMLLoader loader = new FXMLLoader(getClass().getResource("messages_server.fxml") );
    public Server server;

    public void start(Stage primaryStage)  {
        try{
            loader.setController(this);

            Scene scene = new Scene(loader.load());
            textFlow.getChildren().add(new Text("Server started......\n\n"));
            //Server.message = textFlow;

            Timeline fiveSecondsWonder = new Timeline(
                    new KeyFrame(Duration.seconds(5),
                            new EventHandler<ActionEvent>() {

                                @Override
                                public void handle(ActionEvent event) {
                                    //System.out.println("this is called every 5 seconds on UI thread");
                                    if( Server.message.length() != 0 )
                                        textFlow.getChildren().add(new Text(Server.message.toString()+"\n"));
                                    Server.message = new StringBuilder();
                                }
                            }));

            fiveSecondsWonder.setCycleCount(Timeline.INDEFINITE);
            fiveSecondsWonder.play();

            primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent windowEvent) {
                    Server.serverRunning = false;
                    Server.saveAll();
                    System.exit(0);
                }
            });
            primaryStage.setTitle("Car Warehouse Server");
            primaryStage.setScene(scene);
            primaryStage.show();

        }
        catch (Exception e){
            e.printStackTrace();
            System.exit(0);
        }
    }

    public void init(){
        Server.loadAll();
        server = new Server();
    }
    public static void main(String args[]){
        launch(args);
    }
}