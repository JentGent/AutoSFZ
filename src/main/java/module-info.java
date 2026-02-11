module io.github.jentgent.autosfz {
    requires javafx.controls;
    requires javafx.fxml;


    opens io.github.jentgent.autosfz to javafx.fxml;
    exports io.github.jentgent.autosfz;
}