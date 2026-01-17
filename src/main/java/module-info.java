module com.secretsofgreenery {
    requires javafx.controls;
    requires javafx.fxml;
    requires vecmath;
    requires java.desktop;

    opens com.secretsofgreenery to javafx.fxml;
    exports com.secretsofgreenery;
    exports com.secretsofgreenery.ui;
    opens com.secretsofgreenery.ui to javafx.fxml;
}
//
//module com.cgvsu {
//    requires javafx.controls;
//    requires javafx.fxml;
//    requires vecmath;
//    requires java.desktop;
//
//    opens com.cgvsu to javafx.fxml;
//    exports com.cgvsu;
//}