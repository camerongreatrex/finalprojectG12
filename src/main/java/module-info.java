module com.crescent.finalproject {
    requires javafx.controls;
    requires javafx.fxml;
    //added for csv file reading
    requires com.opencsv;


    opens com.crescent.finalproject to javafx.fxml;
    exports com.crescent.finalproject;
}