package com.crescent.finalproject;

// Necessary JavaFX and utility class imports

import com.opencsv.CSVReader;
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
import java.nio.file.Paths;
import java.util.List;

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
     * @author Cameron Greatrex
     * @coauthor Alla Redko
     */
    // Override the start method to set up the GUI elements
    @Override
    public void start(Stage stage) {
        //Create a scene with a group to allow for multiple elements in the scene
        Scene scene = new Scene(new Group());
        stage.setTitle("Contacts App"); // Title of the window
        stage.setWidth(1200); // Width of the window
        stage.setHeight(600); // Height of the window

        final Label label = new Label("Contact Book");
        label.setFont(new Font("Georgia", 20)); // Font style for the label

        table.setEditable(true); // Allows the table to be editable

        // Configuring columns for each attribute of Person
        TableColumn firstNameCol = configureColumn("First Name", "firstName", 100);
        TableColumn lastNameCol = configureColumn("Last Name", "lastName", 100);
        TableColumn emailCol = configureColumn("Email", "email", 210);
        TableColumn phoneNumberCol = configureColumn("Phone Number", "phoneNumber", 100);
        TableColumn addressCol = configureColumn("Address", "address", 180);
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

        // Create a new column called "Delete Contact" to hold the delete button
        TableColumn<Person, Void> deleteCol = new TableColumn<>("Delete");
        // Set the cell factory for the delete column to create a new TableCell for each row
        deleteCol.setCellFactory(col -> new TableCell<Person, Void>() {
            // Create a delete button for each row
            private Button deleteButton = new Button("Delete");

            {
                // Colour the 'Delete" column header red when there is one or more contact
                deleteCol.setStyle("-fx-alignment: CENTER;"); // Center the button in the cell
                deleteCol.setStyle("-fx-color: #ff0000;"); // Set the color of the button to red
                deleteButton.setStyle("-fx-background-color: #ff0000; -fx-text-fill: white;"); // Set the button style
                // Set an action for the delete button
                deleteButton.setOnAction(event -> {
                    // Get the person associated with the current row
                    Person person = getTableView().getItems().get(getIndex());
                    // Call the deletePerson method to remove the person from the data list and update the CSV
                    deletePerson(person);
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

        // Add the delete column to the table
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

        // Load contacts from CSV
        loadContactsFromCSV();
    }

    /**
     * @param promptText The text that will be displayed in the text field when it is empty
     * @param maxWidth   The maximum width of the text field
     * @return A TextField object with the specified prompt text and maximum width
     * @author Cameron Greatrex
     */
    // Method to create text fields for inputs
    private TextField createTextField(String promptText, double maxWidth) {
        TextField textField = new TextField();
        textField.setPromptText(promptText); // Placeholder text
        textField.setMaxWidth(maxWidth); // Maximum width for text field
        return textField;
    }

    /**
     * @return A Button object that adds a new person to the table when clicked
     * @link <a href="https://docs.oracle.com/javafx/2/ui_controls/table-view.htm">...</a>
     * @author Cameron Greatrex
     */
    // Method to create an add button and define its event handler
    private Button createAddButton() {
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

            // Check if at least one field is not empty
            if (!firstName.isEmpty()) {
                // Create a new Person object with the input data
                Person newPerson = new Person(firstName, lastName, email, phoneNumber, address, postalCode, networth);
                data.add(newPerson); // Add new person to the observable list
                saveContactsToCSV(); // Save the entire list to the CSV file

                // Clear all text fields after adding new entry
                hbox.getChildren().stream()
                        .filter(node -> node instanceof TextField)
                        .forEach(node -> ((TextField) node).clear());
            } else {
                // Show an alert if all fields are empty
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Warning");
                alert.setHeaderText(null);
                alert.setContentText("At least the first name must be filled to add a new contact.");
                alert.showAndWait();
            }
        });
        return addButton;
    }


    /**
     * @param columnName   The name of the column to be displayed in the table
     * @param propertyName The name of the property in the Person objects to be displayed in this column
     * @param minWidth     The minimum width of the column
     * @return A TableColumn object with the specified name, property, and minimum width
     * @link <a href="https://docs.oracle.com/javafx/2/ui_controls/table-view.htm">...</a>
     * @author Cameron Greatrex
     */
    // Method to configure and create table columns
    private TableColumn configureColumn(String columnName, String propertyName, double minWidth) {
        TableColumn column = new TableColumn(columnName);
        column.setMinWidth(minWidth); // Minimum width of the column
        column.setCellValueFactory(new PropertyValueFactory<Person, String>(propertyName)); // Property to be displayed in this column
        return column;
    }

    /**
     * @author Alla Redko
     * @link <a href="https://docs.oracle.com/javafx/2/ui_controls/table-view.htm">...</a>
     * @coauthor Cameron Greatrex     *  turned the old code chunks for each property method by adding
     */
    // Method to determine which property of the Person object should be updated and update it
    private void makeColumnEditable(TableColumn<Person, String> column, String propertyName) {
        // Set the cell factory for the column to use a TextFieldTableCell allowing the cells to be edited as text fields
        column.setCellFactory(TextFieldTableCell.forTableColumn());
        // Set the event handler for when an edit is committed (i.e. when the user clicks off the edited cell)
        column.setOnEditCommit(t -> {
            Person person = t.getTableView().getItems().get(t.getTablePosition().getRow());
            // Switch statement to determine which property of the Person object should be updated
            switch (propertyName) {
                case "firstName":
                    person.setFirstName(t.getNewValue());
                    break;
                case "lastName":
                    person.setLastName(t.getNewValue());
                    break;
                case "email":
                    person.setEmail(t.getNewValue());
                    break;
                case "phoneNumber":
                    person.setPhoneNumber(t.getNewValue());
                    break;
                case "address":
                    person.setAddress(t.getNewValue());
                    break;
                case "postalCode":
                    person.setPostalCode(t.getNewValue());
                    break;
                case "networth":
                    person.setNetworth(t.getNewValue());
                    break;
            }
            saveContactsToCSV(); // Save the entire list to the CSV file
        });
    }

    /**
     * @param person The person to be deleted from the table
     * @author Cameron Greatrex
     */
    // Method to delete a person from the table and save the updated list to the CSV file
    public void deletePerson(Person person) {
        data.remove(person);
        saveContactsToCSV();
    }

    /**
     * @link <a href="https://opencsv.sourceforge.net/">...</a>
     * @author Cameron Greatrex
     */
    // Method to load contacts from a CSV file and populate the table with the new person
    private void loadContactsFromCSV() {
        data.clear(); // Clear existing data
        try {
            String csvFilePath = Paths.get("src/main/java/com/crescent/finalproject/table.csv").toAbsolutePath().toString();
            CSVReader csvReader = new CSVReader(new FileReader(csvFilePath));

            List<String[]> allData = csvReader.readAll();
            for (String[] row : allData) {
                if (row.length == 7) {
                    Person person = new Person(row[0], row[1], row[2], row[3], row[4], row[5], row[6]);
                    data.add(person);
                }
            }
            csvReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @link <a href="https://opencsv.sourceforge.net/">...</a>
     * @author Cameron Greatrex
     */
    // Method to save the entire list of contacts to a CSV file
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
     * @author Alla Redko
     * @link <a href="https://docs.oracle.com/javafx/2/ui_controls/table-view.htm">...</a>
     * @coauthor Cameron Greatrex     *  added four properties to the person class including phoneNumber, address, postalCode, and networth
     */
    // Inner class with OOP to represent any person with 7 properties
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