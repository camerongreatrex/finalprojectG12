package com.crescent.finalproject;

// Necessary JavaFX and utility class imports

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.opencsv.CSVWriter;
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

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

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

    /**
     * @throws IOException The exception that is thrown when an I/O error occurs
     */
    // Default constructor because FileWriter needs to handle IOException
    public ContactsApp() throws IOException {
        writer = new CSVWriter(new FileWriter("src/main/java/com/crescent/finalproject/table.csv", true));
    }

    /**
     * @param args The command line arguments
     */
    // Main method to launch the JavaFX application
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * @param stage The stage to display the GUI elements on
     * @link <a href="https://docs.oracle.com/javafx/2/ui_controls/table-view.htm">...</a>
     * @coauthor Alla Redko
     */
    // Override the start method to set up the GUI elements
    @Override
    public void start(Stage stage) {
        // Create a scene with a group to allow for multiple elements in the scene
        Scene scene = new Scene(new Group());
        stage.setTitle("Contacts App"); // Title of the window
        stage.setWidth(1200); // Width of the window
        stage.setHeight(600); // Height of the window

        final Label label = new Label("Contact Book");
        label.setFont(new Font("Georgia", 20)); // Font style for the label

        table.setEditable(true); // Allows the table to be editable

        // Configuring columns for each attribute of Person
        TableColumn<Person, String> firstNameCol = configureColumn("First Name", "firstName", 100);
        TableColumn<Person, String> lastNameCol = configureColumn("Last Name", "lastName", 100);
        TableColumn<Person, String> emailCol = configureColumn("Email", "email", 210);
        TableColumn<Person, String> phoneNumberCol = configureColumn("Phone Number", "phoneNumber", 100);
        TableColumn<Person, String> addressCol = configureColumn("Address", "address", 180);
        TableColumn<Person, String> postalCodeCol = configureColumn("Postal Code", "postalCode", 100);
        TableColumn<Person, String> networthCol = configureColumn("Networth", "networth", 100);

        // Make columns editable
        makeColumnEditable(firstNameCol, "firstName");
        makeColumnEditable(lastNameCol, "lastName");
        makeColumnEditable(emailCol, "email");
        makeColumnEditable(phoneNumberCol, "phoneNumber");
        makeColumnEditable(addressCol, "address");
        makeColumnEditable(postalCodeCol, "postalCode");
        makeColumnEditable(networthCol, "networth");

        //TODO: Add ListView to maybe create a list of addresses

        table.setItems(data); // Link data list to table
        table.getColumns().addAll(firstNameCol, lastNameCol, emailCol, phoneNumberCol, addressCol, postalCodeCol, networthCol); // Add columns to table

        // Create a new column called "Delete Contact" to hold the delete button
        TableColumn<Person, Void> deleteCol = new TableColumn<>("Delete");
        // Set the cell factory for the delete column to create a new TableCell for each row of delete buttons
        deleteCol.setCellFactory(col -> new TableCell<>() {
            // Create a delete button for each row
            private final Button deleteButton = new Button("Delete");

            {
                // Style the 'Delete" column header red when there is one or more contact
                deleteCol.setStyle("-fx-alignment: CENTER; -fx-color: #ff0000;"); // Center the button in the cell and set color
                deleteButton.setStyle("-fx-background-color: #ff0000; -fx-text-fill: white;"); // Set the button style
                // Set an action for the delete button
                deleteButton.setOnAction(event -> {
                    // Get the person associated with the current row
                    Person person = getTableView().getItems().get(getIndex());
                    // Remove the person from the table
                    data.remove(person);
                    saveContactsToCSV(); // Save changes to CSV after deleting
                });
            }

            // Override the updateItem method to update the TableCell with the delete button when a person is present
            @Override
            public void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                // If the row is empty, set the graphic to null (no button)
                if (empty) {
                    setGraphic(null);
                } else {
                    // Otherwise, set the graphic to the delete button
                    setGraphic(deleteButton);
                }
            }
        });

        // Add the delete column with buttons to the table
        table.getColumns().add(deleteCol);

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
                createContactButton() // Add button to add new entries
        );
        // Create a VBox to hold all elements in a single row and allow for adding new people to the table
        final VBox vbox = new VBox();
        vbox.setSpacing(5); // Space between elements in VBox
        vbox.setPadding(new Insets(10, 0, 0, 10)); // Padding around VBox
        vbox.getChildren().addAll(label, table, hbox); // Add label, table, and input fields to VBox

        ((Group) scene.getRoot()).getChildren().addAll(vbox); // Add VBox to the scene

        stage.setScene(scene); // Set the scene on the stage
        stage.show(); // Display the stage

        // Load contacts from CSV
        loadContactsFromCSV();
    }

    /**
     * @return A Button object that adds a new person to the table when clicked
     * @link <a href="https://docs.oracle.com/javafx/2/ui_controls/table-view.htm">...</a>
     */
    // Method to create an add button and define its event handler
    private Button createContactButton() {
        final Button addButton = new Button("Add");

        // Check if at least one text field is filled before adding a new person
        addButton.setOnAction(event -> {
            // Extract text from each text field
            String firstName = ((TextField) hbox.getChildren().get(0)).getText();
            String lastName = ((TextField) hbox.getChildren().get(1)).getText();
            String email = ((TextField) hbox.getChildren().get(2)).getText();
            String phoneNumber = ((TextField) hbox.getChildren().get(3)).getText();
            String address = ((TextField) hbox.getChildren().get(4)).getText();
            String postalCode = ((TextField) hbox.getChildren().get(5)).getText();
            String networth = ((TextField) hbox.getChildren().get(6)).getText();

            // Validate inputs
            if (validateInputs(firstName, lastName, email, phoneNumber, address, postalCode, networth)) {
                data.add(new Person(firstName, lastName, email, phoneNumber, address, postalCode, networth)); // Add new person to the table
                // Clear text fields after adding
                clearTextFields();
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Invalid Input");
                alert.setContentText("Please ensure all fields are filled in correctly.");
                alert.showAndWait();
            }
        });
        return addButton;
    }

    /**
     * @param column   The column to make editable
     * @param property The property of the Person class to bind to
     */
    // Method to make columns editable
    private void makeColumnEditable(TableColumn<Person, String> column, String property) {
        // Set the cell factory to allow for editing text fields in the table
        column.setCellFactory(TextFieldTableCell.forTableColumn());
        column.setOnEditCommit(event -> {
            Person person = event.getRowValue();
            switch (property) {
                case "firstName":
                    person.setFirstName(event.getNewValue());
                    break;
                case "lastName":
                    person.setLastName(event.getNewValue());
                    break;
                case "email":
                    person.setEmail(event.getNewValue());
                    break;
                case "phoneNumber":
                    person.setPhoneNumber(event.getNewValue());
                    break;
                case "address":
                    person.setAddress(event.getNewValue());
                    break;
                case "postalCode":
                    person.setPostalCode(event.getNewValue());
                    break;
                case "networth":
                    person.setNetworth(event.getNewValue());
                    break;
            }
            saveContactsToCSV(); // Save changes to CSV after editing
        });
    }

    /**
     * @author Cameron Greatrex
     * @link <a href="https://www.baeldung.com/opencsv">...</a>
     */
    // Save contacts to a CSV file
    private void saveContactsToCSV() {
        try (CSVWriter writer = new CSVWriter(new FileWriter("src/main/java/com/crescent/finalproject/table.csv"))) {
            // Create a temporary list to hold valid contacts
            ObservableList<Person> validContacts = FXCollections.observableArrayList();
            // Iterate through the data list and add valid contacts to the temporary list
            for (Person person : data) {
                if (!person.getFirstName().isEmpty()) {
                    validContacts.add(person);
                    writer.writeNext(new String[]{
                            person.getFirstName(),
                            person.getLastName(),
                            person.getEmail(),
                            person.getPhoneNumber(),
                            person.getAddress(),
                            person.getPostalCode(),
                            person.getNetworth()
                    });
                }
            }
            // Clear the original data list and add back only the valid contacts
            data.clear();
            data.addAll(validContacts);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @author Cameron Greatrex
     * @link <a href="https://www.baeldung.com/opencsv">...</a>
     */
    // Load contacts from a CSV file into the table
    private void loadContactsFromCSV() {
        try (CSVReader reader = new CSVReader(new FileReader("src/main/java/com/crescent/finalproject/table.csv"))) {
            List<String[]> records = reader.readAll();
            for (String[] record : records) {
                if (!record[0].equals("First Name")) { // Skip header
                    data.add(new Person(record[0], record[1], record[2], record[3], record[4], record[5], record[6]));
                }
            }
        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param firstName   The first name of the contact
     * @param lastName    The last name of the contact
     * @param email       The email of the contact
     * @param phoneNumber The phone number of the contact
     * @param address     The address of the contact
     * @param postalCode  The postal code of the contact
     * @param networth    The networth of the contact
     * @return A boolean value indicating whether the inputs are valid
     * @author Cameron Greatrex
     * @coauthor ChatGPT
     * ChatGPT helped with the regex and patterns for all the validation rules. Cameron Greatrex implemented the isEmpty() method
     * the prompt used after creating the base method was "Please ensure that the inputted Strings follow normal guidelines as seen online
     */
    // Validate inputs for a new contact entry
    private boolean validateInputs(String firstName, String lastName, String email, String phoneNumber, String address, String postalCode, String networth) {
        // Validation rules for each field
        boolean isValidFirstName = !firstName.isEmpty();
        boolean isValidLastName = !lastName.isEmpty();
        boolean isValidEmail = Pattern.matches("^[\\w.-]+@[\\w.-]+\\.\\w+$", email);
        boolean isValidPhoneNumber = Pattern.matches("^\\d{10}$", phoneNumber);
        boolean isValidAddress = !address.isEmpty();
        boolean isValidPostalCode = Pattern.matches("^\\p{Alpha}\\d\\p{Alpha} \\d\\p{Alpha}\\d$", postalCode);
        boolean isValidNetworth = Pattern.matches("^\\d+(\\.\\d{1,2})?$", networth);

        return isValidFirstName && isValidLastName && isValidEmail && isValidPhoneNumber && isValidAddress && isValidPostalCode && isValidNetworth;
    }

    /**
     * @author Cameron Greatrex
     */
    // Clear all text fields after adding a new contact
    private void clearTextFields() {
        for (int i = 0; i < hbox.getChildren().size() - 1; i++) {
            ((TextField) hbox.getChildren().get(i)).clear();
        }
    }

    /**
     * @param columnName The name of the column to display
     * @param property   The property of the Person class to bind to
     * @param width      The width of the column
     * @return A TableColumn object configured with the specified name, property, and width
     */
    // Method to configure columns
    private TableColumn<Person, String> configureColumn(String columnName, String property, double width) {
        TableColumn<Person, String> column = new TableColumn<>(columnName);
        column.setMinWidth(width);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        return column;
    }

    /**
     * @param promptText The text that will be displayed in the text field when it is empty
     * @param maxWidth   The maximum width of the text field
     * @return A TextField object with the specified prompt text and maximum width
     */
    // Method to create text fields for inputs
    private TextField createTextField(String promptText, double maxWidth) {
        TextField textField = new TextField();
        textField.setPromptText(promptText); // Placeholder text
        textField.setMaxWidth(maxWidth); // Maximum width for text field
        return textField;
    }

    /**
     * @author Alla Redko
     * @link <a href="https://docs.oracle.com/javafx/2/ui_controls/table-view.htm">...</a>
     * @coauthor Cameron Greatrex     *  added four properties to the person class including phoneNumber, address, postalCode, and networth
     */
    // Inner class with OOP to represent any person with 7 properties
    public static class Person {
        private final SimpleStringProperty firstName;
        private final SimpleStringProperty lastName;
        private final SimpleStringProperty email;
        private final SimpleStringProperty phoneNumber;
        private final SimpleStringProperty address;
        private final SimpleStringProperty postalCode;
        private final SimpleStringProperty networth;

        // Constructor for Person class
        Person(String fName, String lName, String email, String phoneNumber, String address, String postalCode, String networth) {
            this.firstName = new SimpleStringProperty(fName);
            this.lastName = new SimpleStringProperty(lName);
            this.email = new SimpleStringProperty(email);
            this.phoneNumber = new SimpleStringProperty(phoneNumber);
            this.address = new SimpleStringProperty(address);
            this.postalCode = new SimpleStringProperty(postalCode);
            this.networth = new SimpleStringProperty(networth);
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
