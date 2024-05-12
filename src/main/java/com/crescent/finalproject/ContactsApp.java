package com.crescent.finalproject;

// Necessary JavaFX and utility class imports

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

// Main class for the Contacts App, extending from JavaFX Application class
public class ContactsApp extends Application {

    // TableView for displaying Person objects in a table format
    private TableView<Person> table = new TableView<>();
    // Observable list for managing Person data; updates the TableView automatically when data changes
    private final ObservableList<Person> data = FXCollections.observableArrayList(
            new Person("John", "Doe", "john.doe@example.com", "1234567890", "1234 Main St", "12345", "$100,000")
    );
    // Horizontal Box for layout of input fields and button
    final HBox hbox = new HBox();

    // Main method to launch the JavaFX application
    public static void main(String[] args) {
        launch(args);
    }

    // Override the start method to set up the GUI elements
    @Override
    public void start(Stage stage) {
        Scene scene = new Scene(new Group());
        stage.setTitle("Contacts App"); // Title of the window
        stage.setWidth(1000); // Width of the window
        stage.setHeight(550); // Height of the window

        final Label label = new Label("Contact Book");
        label.setFont(new Font("Georgia", 20)); // Font style for the label

        table.setEditable(true); // Allows the table to be editable

        // Configuring columns for each attribute of Person
        TableColumn firstNameCol = configureColumn("First Name", "firstName", 100);
        TableColumn lastNameCol = configureColumn("Last Name", "lastName", 100);
        TableColumn emailCol = configureColumn("Email", "email", 200);
        TableColumn phoneNumberCol = configureColumn("Phone Number", "phoneNumber", 100);
        TableColumn addressCol = configureColumn("Address", "address", 100);
        TableColumn postalCodeCol = configureColumn("Postal Code", "postalCode", 100);
        TableColumn networthCol = configureColumn("Networth", "networth", 100);

        table.setItems(data); // Link data list to table
        table.getColumns().addAll(firstNameCol, lastNameCol, emailCol, phoneNumberCol, addressCol, postalCodeCol, networthCol); // Add columns to table

        // Setting up HBox with text fields and add button
        hbox.setSpacing(3); // Space between elements in HBox
        hbox.getChildren().addAll(
                createTextField("First Name", firstNameCol.getPrefWidth()),
                createTextField("Last Name", lastNameCol.getPrefWidth()),
                createTextField("Email", emailCol.getPrefWidth()),
                createTextField("Phone Number", phoneNumberCol.getPrefWidth()),
                createTextField("Address", addressCol.getPrefWidth()),
                createTextField("Postal Code", postalCodeCol.getPrefWidth()),
                createTextField("Networth", networthCol.getPrefWidth()),
                createAddButton() // Add button to add new entries
        );

        final VBox vbox = new VBox();
        vbox.setSpacing(5); // Space between elements in VBox
        vbox.setPadding(new Insets(10, 0, 0, 10)); // Padding around VBox
        vbox.getChildren().addAll(label, table, hbox); // Add label, table, and input fields to VBox

        ((Group) scene.getRoot()).getChildren().addAll(vbox); // Add VBox to the scene

        stage.setScene(scene); // Set the scene on the stage
        stage.show(); // Display the stage
    }

    // Method to create text fields for input
    private TextField createTextField(String promptText, double maxWidth) {
        TextField textField = new TextField();
        textField.setPromptText(promptText); // Placeholder text
        textField.setMaxWidth(maxWidth); // Maximum width for text field
        return textField;
    }

    // Method to create an add button and define its event handler
    private Button createAddButton() {
        final Button addButton = new Button("Add");
        addButton.setOnAction(event -> {
            // Creates a new Person object from input fields and adds it to the data list
            Person newPerson = new Person(
                    ((TextField) hbox.getChildren().get(0)).getText(),
                    ((TextField) hbox.getChildren().get(1)).getText(),
                    ((TextField) hbox.getChildren().get(2)).getText(),
                    ((TextField) hbox.getChildren().get(3)).getText(),
                    ((TextField) hbox.getChildren().get(4)).getText(),
                    ((TextField) hbox.getChildren().get(5)).getText(),
                    ((TextField) hbox.getChildren().get(6)).getText()
            );
            data.add(newPerson); // Add new person to observable list
            // Clear all text fields after adding new entry
            hbox.getChildren().stream()
                    .filter(node -> node instanceof TextField)
                    .forEach(node -> ((TextField) node).clear());
        });
        return addButton;
    }

    // Method to configure and create table columns
    private TableColumn configureColumn(String columnName, String propertyName, double minWidth) {
        TableColumn column = new TableColumn(columnName);
        column.setMinWidth(minWidth); // Minimum width of the column
        column.setCellValueFactory(new PropertyValueFactory<Person, String>(propertyName)); // Property to be displayed in this column
        return column;
    }

    // Inner class to represent a person with properties for data binding
    public static class Person {
        private final SimpleStringProperty firstName, lastName, email, phoneNumber, address, postalCode, networth;

        // Constructor to initialize properties of the Person
        private Person(String fName, String lName, String email, String phoneNumber, String Address, String PostalCode, String Networth) {
            this.firstName = new SimpleStringProperty(fName);
            this.lastName = new SimpleStringProperty(lName);
            this.email = new SimpleStringProperty(email);
            this.phoneNumber = new SimpleStringProperty(phoneNumber);
            this.address = new SimpleStringProperty(Address);
            this.postalCode = new SimpleStringProperty(PostalCode);
            this.networth = new SimpleStringProperty(Networth);
        }

        // Getter and setter methods for each property
        public String getFirstName() {
            return firstName.get();
        }

        public void setFirstName(String fName) {
            firstName.set(fName);
        }

        public String getLastName() {
            return lastName.get();
        }

        public void setLastName(String lName) {
            lastName.set(lName);
        }

        public String getEmail() {
            return email.get();
        }

        public void setEmail(String email) {
            this.email.set(email);
        }

        public String getPhoneNumber() {
            return phoneNumber.get();
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber.set(phoneNumber);
        }

        public String getAddress() {
            return address.get();
        }

        public void setAddress(String address) {
            this.address.set(address);
        }

        public String getPostalCode() {
            return postalCode.get();
        }

        public void setPostalCode(String postalCode) {
            this.postalCode.set(postalCode);
        }

        public String getNetworth() {
            return networth.get();
        }

        public void setNetworth(String networth) {
            this.networth.set(networth);
        }
    }
}
