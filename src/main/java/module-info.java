module org.example.librarymanagmentsystem {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.librarymanagmentsystem to javafx.fxml;
    exports org.example.librarymanagmentsystem;
    exports org.example.librarymanagmentsystem.aplication;
    opens org.example.librarymanagmentsystem.aplication to javafx.fxml;
    exports org.example.librarymanagmentsystem.controller;
    opens org.example.librarymanagmentsystem.controller to javafx.fxml;
}