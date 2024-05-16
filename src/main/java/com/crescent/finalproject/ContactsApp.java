package com.crescent.finalproject;

// Necessary JavaFX and utility class imports

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.opencsv.CSVWriter;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.FileWriter;
import java.io.IOException;

// Main class for the Contacts App, extending from JavaFX Application class
public class ContactsApp extends Application {
    // TableView for displaying Person objects in a table format
    private final TableView<Person> table = new TableView<>();
    // Observable list for managing Person data; updates the TableView automatically when data changes
    private final ObservableList<Person> data = FXCollections.observableArrayList();
    // Horizontal Box for layout of input fields and button
    final HBox hbox = new HBox();
    // Declare the CSVWriter to read and write contacts to a CSV file
    CSVWriter writer;

    // FileWriter needs to handle IOException, therefore I need to make a default constructor
    public ContactsApp() throws IOException {
        writer = new CSVWriter(new FileWriter("/Users/cameron/Desktop/School/Grade 11/Computer Science/FinalProject/src/main/java/com/crescent/finalproject/output.csv", true));
    }

    // Main method to launch the JavaFX application
    public static void main(String[] args) {
        launch(args);
    }

    // Override the start method to set up the GUI elements
    @Override
    public void start(Stage stage) {
        //Create a scene with a group to allow for multiple elements in the scene
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

        // Make columns editable
        makeColumnEditable(firstNameCol, "firstName");
        makeColumnEditable(lastNameCol, "lastName");
        makeColumnEditable(emailCol, "email");
        makeColumnEditable(phoneNumberCol, "phoneNumber");
        makeColumnEditable(addressCol, "address");
        makeColumnEditable(postalCodeCol, "postalCode");
        makeColumnEditable(networthCol, "networth");

        table.setItems(data); // Link data list to table
        table.getColumns().addAll(firstNameCol, lastNameCol, emailCol, phoneNumberCol, addressCol, postalCodeCol, networthCol); // Add columns to table

        // Setting up HBox with text fields and add button to allow for adding new people to the table
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
        // Create a VBox to hold all elements in a single row and allow for adding new people to the table
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
            data.add(newPerson); // Add new person to an observable list
            // Write new person to CSV when they are created
            writer.writeNext(new String[]{newPerson.getFirstName(), newPerson.getLastName(), newPerson.getEmail(), newPerson.getPhoneNumber(), newPerson.getAddress(), newPerson.getPostalCode(), newPerson.getNetworth()});
            try {
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
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

    // Method to make a table column editable

    /**
     * @author Alla Redko
     * @link <a href="https://docs.oracle.com/javafx/2/ui_controls/table-view.htm">...</a>
     * @coauthor Cameron Greatrex     *  turned the old code chunks for each property method by adding
     * a switch statement to determine which property of the Person object should be updated.
     */
    private void makeColumnEditable(TableColumn<Person, String> column, String propertyName) {
        // Set the cell factory for the column to use a TextFieldTableCell allowing the cells to be edited as text fields
        column.setCellFactory(TextFieldTableCell.forTableColumn());

        // Set the event handler for when an edit is committed (i.e. when the user clicks off the edited cell)
        column.setOnEditCommit(
                // Removed the need for "TableColumn.CellEditEvent<Person, String" because it is already defined in the handle method
                new EventHandler<TableColumn.CellEditEvent<Person, String>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Person, String> t) {
                        // Get the Person object for the row that was edited
                        Person person = t.getTableView().getItems().get(t.getTablePosition().getRow());

                        // Use a switch statement to determine which property of the Person object should be updated
                        switch (propertyName) {
                            case "firstName":
                                // Update the first name property
                                person.setFirstName(t.getNewValue());
                                break;
                            case "lastName":
                                // Update the last name property
                                person.setLastName(t.getNewValue());
                                break;
                            case "email":
                                // Update the email property
                                person.setEmail(t.getNewValue());
                                break;
                            case "phoneNumber":
                                // Update the phone number property
                                person.setPhoneNumber(t.getNewValue());
                                break;
                            case "address":
                                // Update the address property
                                person.setAddress(t.getNewValue());
                                break;
                            case "postalCode":
                                // Update the postal code property
                                person.setPostalCode(t.getNewValue());
                                break;
                            case "networth":
                                // Update the networth property
                                person.setNetworth(t.getNewValue());
                                break;
                        }

                        // Write person to CSV when they're edited
                        writer.writeNext(new String[]{person.getFirstName(), person.getLastName(), person.getEmail(), person.getPhoneNumber(), person.getAddress(), person.getPostalCode(), person.getNetworth()});
                        try {
                            writer.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
        );
    }
    //TODO: add a method to read the CSV file and populate the table with the data


    // Inner class with OOP to represent any person with 7 properties

    /**
     * @author Alla Redko
     * @link <a href="https://docs.oracle.com/javafx/2/ui_controls/table-view.htm">...</a>
     * @coauthor Cameron Greatrex     *  added four properties to the person class including phoneNumber, address, postalCode, and networth
     */
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
