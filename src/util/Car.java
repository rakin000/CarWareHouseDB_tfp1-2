package util;

import java.io.Serializable;

public class Car implements Serializable {
    private String regNumber;
    private int yearMade;
    private String color[];
    private String carMake, carModel;
    private int price;
    private int quantity;
    private SerializableImage image;

    public Car(String regNumber, int yearMade, String[] color, String carMake, String carModel, int price, int quantity){
        this.regNumber = regNumber;
        this.yearMade = yearMade;
        this.color = new String[3] ;
        this.color[0] = color[0];
        this.color[1] = color[1];
        this.color[2] = color[2] ;
        this.carMake = carMake;
        this.carModel = carModel;
        this.price = price;
        this.quantity = quantity;
    }

    public Car(){
        this("",0,new String[]{"","",""},"","",0,0);
    }


    public void setImage(SerializableImage image){
        this.image = image;
    }

    public String getRegistrationNumber()   {   return regNumber;   }
    public int getYearMade()                {   return yearMade;    }
    public String[] getColors()             {   return color;     }
    public String getCarMake()              {   return carMake;     }
    public String getCarModel()             {   return carModel;    }
    public int getPrice()                   {   return price;       }
    public int getQuantity()                {   return quantity;    }
    public SerializableImage getImage()     {   return image;       }

    public void buy(){
        if( quantity > 0 )
            --quantity;
        else
            throw new IllegalStateException();
    }

    public String toString(){
        return  "Registration Number: " + regNumber + "\n" +
                "Year Made: " + yearMade + "\n" +
                "Colors: " + color[0] + ", " + color[1] + ", " + color[2] + "\n" +
                "Car Make: " + carMake+"\n" +
                "Car Model: " + carModel + "\n" +
                "Price: " + price + "\n" +
                "Quantity: " + quantity + "\n";

    }
    public String formatW(){
        return regNumber + "," + yearMade + "," + color[0] + "," + color[1] + "," + color[2] + "," + carMake + "," + carModel + "," + price + "," + quantity;
    }
}
