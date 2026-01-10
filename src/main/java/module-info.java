module com.secretsofgreenery {
    requires javafx.controls;
    requires javafx.fxml;
    requires vecmath;
    requires java.desktop;

    opens com.secretsofgreenery to javafx.fxml;
    exports com.secretsofgreenery;
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