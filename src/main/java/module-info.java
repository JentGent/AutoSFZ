module io.github.jentgent.autosfz {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires TarsosDSP.jvm;
    requires TarsosDSP.core;


    opens io.github.jentgent.autosfz to javafx.fxml;
    exports io.github.jentgent.autosfz;
}