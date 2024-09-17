module fx {
    requires javafx.controls;
    requires javafx.fxml;
    requires batik.all;
    requires javafx.swing;
    requires javafx.web;

    exports fx;
    opens fx to javafx.fxml;
    opens mj.impl to javafx.base;
    opens mj.impl.Expr to javafx.base;
    opens mj.impl.Statement to javafx.base;
}