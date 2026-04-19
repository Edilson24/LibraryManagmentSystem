module org.example.librarymanagmentsystem {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.librarymanagmentsystem to javafx.fxml;
    exports org.example.librarymanagmentsystem;
}