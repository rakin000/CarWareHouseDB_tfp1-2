package server;


import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import java.net.ServerSocket;
import java.net.Socket;

import javafx.scene.image.Image;
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
                networkUtil.write(car.getImage()); ///
                cnt += 1;
            }
        }

        networkUtil.write(NetworkCipher.SUCCESS);
    }

    private synchronized void add(Car newCar) throws IOException, ClassNotFoundException {
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

    private synchronized void buy(String regN) throws IOException, ClassNotFoundException{
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

    private synchronized void delete(String regN) throws IOException, ClassNotFoundException {
        int searchIndex = -1;

        for (int i = 0; i < Server.carList.size(); i++) {
            Car t = Server.carList.get(i);
            if (t.getRegistrationNumber().toLowerCase().equals(regN.toLowerCase()) ) {
                searchIndex = i;
                break;
            }
        }
        if (searchIndex != -1) {
            networkUtil.write(NetworkCipher.SUCCESS);
            try {
                String fpath = "\\src\\server\\images\\" + Server.carList.get(searchIndex).getRegistrationNumber() + ".img";
                File img = new File(fpath);
                img.delete();
            } catch (Exception e) {
                System.out.println("Failed to delete!!!\n"+ e);
            }
            Server.carList.remove(searchIndex);
        }
        else {
            networkUtil.write(NetworkCipher.FAILED);
        }
    }

    private synchronized void modify(String regN, Car newCar) throws IOException, ClassNotFoundException {
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
                User user = (User) networkUtil.read();

                if (user.username == null && user.password == null){
                    networkUtil.write(NetworkCipher.FAILED) ;
                    continue;
                }
                int uType = checkUser(user);
                if( uType == -1 ){
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
                        SerializableImage img = (SerializableImage) networkUtil.read();
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
                        SerializableImage img = (SerializableImage) networkUtil.read();
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

                Car t = new Car(attr[0], Integer.parseInt(attr[1]), new String[]{attr[2],attr[3],attr[4]}, attr[5], attr[6], Integer.parseInt(attr[7]), Integer.parseInt(attr[8]) ) ;
                String imgPath = "src\\server\\images\\" + t.getRegistrationNumber() + ".img";
                try {
                    FileInputStream input = new FileInputStream(imgPath);
                    Image image = new Image(input);
                    t.setImage(new SerializableImage(image));
                }catch(Exception e) {
                    System.out.println(e+"\n"+"No image file found!!" + " " + imgPath);
                }

                Server.carList.add(t);
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


                String imgPath = "src\\server\\images\\"+car.getRegistrationNumber()+".img";
                try{
                    car.getImage().saveImage(imgPath);
                } catch(Exception e){
                    System.out.println(e);
                    System.out.println("couldn't write image!!"+" "+car.getRegistrationNumber());
                }
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

            Timeline fiveSecondsWonder = new Timeline(
                    new KeyFrame(Duration.seconds(5),
                            new EventHandler<ActionEvent>() {

                                @Override
                                public void handle(ActionEvent event) {
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