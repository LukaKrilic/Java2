module hr.algebra.brassbirmingham {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.logging;
    requires java.rmi;
    requires java.naming;
    requires java.xml;
    requires java.desktop;


    opens hr.algebra.brassbirmingham to javafx.fxml;
    exports hr.algebra.brassbirmingham;
    exports hr.algebra.brassbirmingham.controller;
    opens hr.algebra.brassbirmingham.controller to javafx.fxml;
    exports hr.algebra.brassbirmingham.rmi;
}