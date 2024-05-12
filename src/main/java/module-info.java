module com.crescent.finalproject {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.crescent.finalproject to javafx.fxml;
    exports com.crescent.finalproject;
}