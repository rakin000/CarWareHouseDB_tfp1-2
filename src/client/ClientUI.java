package client;

import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import util.NetworkUtil;
import util.User;
import util.Command;
import util.NetworkCipher;
import util.Car;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.text.*;
import javafx.fxml.FXMLLoader;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;

import javafx.scene.layout.VBox;
import javafx.scene.layout.AnchorPane;

import javafx.scene.Group;


class carInput{
    @FXML Button cancelButton,okButton,imageChoiceButton;
    @FXML TextField carMake,carModel,yearMade,regNumber,price,color1,color2,color3,quantity;
    @FXML ImageView carImage;

    Car car;
    public carInput(Car initial) throws Exception {
        Stage carIn = new Stage();
        carIn.setTitle("Car Input");

        FXMLLoader loader = new FXMLLoader(getClass().getResource("car_input.fxml"));
        loader.setController(this);

        Scene scene = new Scene(loader.load());

        carMake.setText(initial.getCarMake());
        carModel.setText(initial.getCarModel());
        yearMade.setText(initial.getYearMade()+"");
        regNumber.setText(initial.getRegistrationNumber());
        price.setText(initial.getPrice()+"");
        color1.setText(initial.getColors()[0]);
        color2.setText(initial.getColors()[1]);
        color3.setText(initial.getColors()[2]);
        quantity.setText(initial.getQuantity()+"");

        cancelButton.setOnAction(e ->{
            car = null;
            carIn.close();
        });

        okButton.setOnAction(e -> {
            try {
                car = new Car(regNumber.getText(),
                        Integer.parseInt(yearMade.getText()),
                        new String[]{color1.getText(), color2.getText(), color3.getText()},
                        carMake.getText(),
                        carModel.getText(),
                        Integer.parseInt(price.getText()),
                        Integer.parseInt(quantity.getText())
                );
            }
            catch( Exception error){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText(error.toString()+"\n\n"+"Invalid values!!!");
                alert.setTitle("Error");

                car = null;
                alert.showAndWait();

            }

            carIn.close();
        });



        imageChoiceButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                final FileChooser fileChooser = new FileChooser();
                File file = fileChooser.showOpenDialog(carIn);
              /*  if (file != null) {
                    openFile(file);
                }String filepath = file.getPath();
                 */
                if( file != null){
                    try{
                        FileInputStream input = new FileInputStream(file.getPath());
                        Image image = new Image(input);
                        carImage.setImage(image);
                    } catch( Exception ex){
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error!");
                        alert.setContentText(ex+"Not an image file!!");
                        alert.showAndWait();
                    }
                }
            }
        });
        carIn.setScene(scene);
        carIn.showAndWait();
    }

    public Car getCar(){
        if( car == null)
            throw new NullPointerException();
        else return car;
    }

}

public class ClientUI extends Application{
    final String serverAddress = "127.0.0.1";
    final int serverPort = 420 ;

    NetworkUtil networkUtil ;

    Stage myStage;

    Scene login;
    Scene menu;

    int userType = User.VIEWER;
    //login
    @FXML
    Button resetButton, loginButton;
    @FXML
    TextField userField;
    @FXML
    PasswordField passwordField;

    //menu
    @FXML
    Button viewAll,search,logout,addButton;
    @FXML
    ScrollPane viewScrollPane;
    @FXML TextField regN, carMake, carModel;

    private AnchorPane carView(Car car){
        VBox textInfo = new VBox(10) ;
        Text text = new Text("Car Model: "+car.getCarModel() + "\n" +
                                "Car Make: "+car.getCarMake() + "\n" +
                                "Year Made: "+car.getYearMade() + "\n" +
                                "Colors: "+ car.getColors()[0] + ", " + car.getColors()[1] + ", " + car.getColors()[2] + "\n" +
                                "Price: "+ car.getPrice() + "\n" +
                                "Registration Number: "+car.getRegistrationNumber() + "\n" +
                                "\n" +
                                "Available Amount: "+ car.getQuantity()
        );
        text.setFont(Font.font("verdana", FontWeight.SEMI_BOLD, FontPosture.REGULAR, 16));

        textInfo.getChildren().add(text);
        AnchorPane cView = new AnchorPane();
        Button edit = new Button("Edit"), buy = new Button("Buy"), del = new Button("Delete");

        cView.getChildren().addAll(textInfo,buy,del,edit);
        textInfo.setLayoutX(500.0);
        textInfo.setLayoutY(30.0);
        textInfo.setPrefSize(300.0, 300.0);

        buy.setLayoutX(300.0);
        buy.setLayoutY(360.0);
        buy.setPrefSize(100.0,50.0);

        edit.setLayoutX(500.0);
        edit.setLayoutY(360.0);
        edit.setPrefSize(100.0,50.0);

        del.setLayoutX(700.0);
        del.setLayoutY(360.0);
        del.setPrefSize(100.0,50.0);


        buy.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                try{
                    networkUtil.write(Command.BUY);
                    networkUtil.write(car.getRegistrationNumber());

                    int message = NetworkCipher.FAILED;
                    message = (int) networkUtil.read();
                    if( message == NetworkCipher.SUCCESS){
                        Alert success = new Alert(Alert.AlertType.CONFIRMATION);
                        success.setTitle("Buying successful!!");
                        success.setContentText("Congrats!!");
                        success.showAndWait();
                    }
                    else {
                        Alert fail = new Alert(Alert.AlertType.ERROR);
                        fail.setTitle("Failed!");
                        fail.setContentText("The car is unavailable.");
                        fail.showAndWait();
                    }
                } catch(Exception ex){
                    ex.printStackTrace();
                    System.exit(-1);
                }
            }
        });

        del.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent ae){
                try{
                    networkUtil.write(Command.DELETE);
                    networkUtil.write(car.getRegistrationNumber());

                    int message = NetworkCipher.FAILED;
                    message = (int) networkUtil.read();
                    if( message == NetworkCipher.SUCCESS){
                        Alert success = new Alert(Alert.AlertType.CONFIRMATION);
                        success.setTitle("Success!!");
                        success.setContentText("Successfully deleted the car");
                        success.showAndWait();
                    }
                    else {
                        Alert fail = new Alert(Alert.AlertType.ERROR);
                        fail.setTitle("Failed!");
                        fail.setContentText("The car is unavailable.");
                        fail.showAndWait();
                    }
                } catch(Exception ex){
                    ex.printStackTrace();
                    System.exit(-1);
                }
            }
        });

        edit.setOnAction(e -> {
            try{
                // networkUtil.write(Command.ADD);
                carInput adder = new carInput(car);

                try{
                    Car newCar = adder.getCar();
                    networkUtil.write(Command.EDIT);
                    networkUtil.write(car.getRegistrationNumber());
                    networkUtil.write(newCar);

                    int message = NetworkCipher.FAILED;
                    message = (int) networkUtil.read();
                    if( message == NetworkCipher.SUCCESS){
                        Alert success = new Alert(Alert.AlertType.CONFIRMATION);
                        success.setTitle("Success!!");
                        success.setContentText("Successfully modified the car");
                        success.showAndWait();
                    }
                    else {
                        Alert fail = new Alert(Alert.AlertType.ERROR);
                        fail.setTitle("Failed!");
                        fail.setContentText("A car with this Registration Number already exists or the car is not available!!");
                        fail.showAndWait();
                    }
                } catch(NullPointerException ne){
                    //do nothing
                }

            } catch(Exception ex){
                ex.printStackTrace();
                System.exit(-1);
            }
        });

        if( userType == User.VIEWER ){
            edit.setDisable(true);
            del.setDisable(true);
            buy.setDisable(false);
        }
        else {
            edit.setDisable(false);
            del.setDisable(false);
            buy.setDisable(true);
        }

        return cView;
    }


    public void init(){
        try{
            networkUtil = new NetworkUtil(serverAddress,serverPort);
        } catch(java.net.ConnectException ec){
            System.out.println(ec);
            System.out.println("No server found running!!") ;
            System.exit(-1);
        }
        catch (Exception e) {
            System.out.println(e);
            System.exit(-1);
        }

        // new ReadThread(networkUtil);

        try{
            FXMLLoader loginfxml = new FXMLLoader(getClass().getResource("login_client.fxml") );
            FXMLLoader menufxml = new FXMLLoader(getClass().getResource("menu_client.fxml") );

            loginfxml.setController(this);
            menufxml.setController(this);

            login = new Scene(loginfxml.load());
            menu = new Scene(menufxml.load());
        }
        catch(Exception E){
            E.printStackTrace();
            System.exit(-1);
        }


        resetButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e){
                userField.setText("");
                passwordField.setText("");
            }
        });

        loginButton.setOnAction(new EventHandler<ActionEvent>(){
            public void handle(ActionEvent e){
                String uname = userField.getText();
                String pass = passwordField.getText();
                try{
                    networkUtil.write( (new User(uname,pass,User.VIEWER)) ) ;
                }
                catch(java.net.SocketException ex){
                    Alert err = new Alert(Alert.AlertType.ERROR);
                    err.setTitle("Login Error");
                    err.setContentText("No server found running!!");
                    System.exit(-1);
                }
                catch(Exception ex){
                    ex.printStackTrace();
                    System.exit(-1);
                }

                int msg = NetworkCipher.FAILED;
                try{
                    msg = (Integer) networkUtil.read();
                }catch(Exception ex){
                    System.out.println("Login: ");
                    ex.printStackTrace();
                    System.exit(-1);
                }

                if( msg == NetworkCipher.FAILED){
                    Alert failed = new Alert(Alert.AlertType.ERROR);
                    failed.setTitle("Wrong credentials!");
                    failed.setContentText("Please enter correct user info.");
                    failed.showAndWait();
                }
                else{ // success
                    //  Stage window = (Stage) ((Node) e.getSource()).getScene().getWindow() ;
                    // window.setScene(menu);
                    // window.show();
                    userType = msg;
                    if( msg == User.VIEWER)
                        addButton.setDisable(true);
                    else
                        addButton.setDisable(false);

                    viewScrollPane.setContent(new VBox());
                    myStage.setScene(menu);
                    myStage.show();
                }
            }
        });

        addButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                try{
                   // networkUtil.write(Command.ADD);
                    carInput adder = new carInput(new Car());

                    try{
                        Car car = adder.getCar();
                        networkUtil.write(Command.ADD);
                        networkUtil.write(car);

                        int message = NetworkCipher.FAILED;
                        message = (int) networkUtil.read();
                        if( message == NetworkCipher.SUCCESS){
                            Alert success = new Alert(Alert.AlertType.CONFIRMATION);
                            success.setTitle("Success!!");
                            success.setContentText("Successfully added the car");
                            success.showAndWait();
                        }
                        else {
                            Alert fail = new Alert(Alert.AlertType.ERROR);
                            fail.setTitle("Failed!");
                            fail.setContentText("A car with this Registration Number already added!!");
                            fail.showAndWait();
                        }
                    } catch(NullPointerException ne){
                        //do nothing
                    } catch(Exception ex){
                        ex.printStackTrace();
                        System.exit(-1);
                    }

                } catch(Exception ex){
                    System.out.println("Add: ");
                    ex.printStackTrace();
                    System.exit(-1);
                }


            }
        });

        viewAll.setOnAction(new EventHandler<ActionEvent>(){
            public void handle(ActionEvent e){
                try{
                    networkUtil.write(Command.VIEWALL) ;
                } catch(Exception ex){
                    ex.printStackTrace();
                    System.exit(-1);
                }

                viewScrollPane.setPannable(true);
                viewScrollPane.fitToWidthProperty().set(true);
                viewScrollPane.fitToHeightProperty().set(true);

                VBox cars = new VBox(15);
                try{
                    while(true){
                        Object car = (Object) networkUtil.read();
                        if( car instanceof Car)
                            // viewScrollPane.getChildren().add(carView((Car) car)) ;
                            // viewScrollPane.setContent(carView((Car) car));
                            cars.getChildren().add(carView((Car) car));
                        else break;
                    }

                    viewScrollPane.setContent(cars);

                }
                catch(Exception ex){
                    ex.printStackTrace();
                    System.exit(-1);
                }
            }
        }) ;

        search.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                try{
                    networkUtil.write(Command.SEARCH);
                    networkUtil.write(regN.getText());
                    networkUtil.write(carMake.getText());
                    networkUtil.write(carModel.getText());

                } catch(Exception ex){
                    ex.printStackTrace();
                }

                viewScrollPane.setPannable(true);
                viewScrollPane.fitToWidthProperty().set(true);
                viewScrollPane.fitToHeightProperty().set(true);

                VBox cars = new VBox(15);
                try{
                    while(true){
                        Object car = (Object) networkUtil.read();
                        if( car instanceof Car)
                            // viewScrollPane.getChildren().add(carView((Car) car)) ;
                            // viewScrollPane.setContent(carView((Car) car));
                            cars.getChildren().add(carView((Car) car));
                        else break;
                    }

                    viewScrollPane.setContent(cars);

                }
                catch(Exception ex){
                    ex.printStackTrace();
                    System.exit(-1);
                }
            }
        });

        logout.setOnAction(new EventHandler<ActionEvent>(){
            public void handle(ActionEvent e){
                try{
                    networkUtil.write(NetworkCipher.LOGOUT);
                }catch(Exception ex){
                    ex.printStackTrace();
                    System.exit(-1);
                }

                myStage.setScene(login);
                myStage.show();
            }
        });
    }



    public void start(Stage primaryStage) throws Exception {
        myStage = primaryStage;
        myStage.setTitle("Car Warehouse System");

        myStage.setScene(login);
        myStage.show();
    }


    public static void main(String args[]){
        launch(args);
    }
}