module BOGO {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires com.microsoft.sqlserver.jdbc;

    opens org.BOGO to javafx.fxml;
    opens org.BOGO.simulation to javafx.fxml;
    exports org.BOGO;
    exports org.BOGO.UI;
    exports org.BOGO.config;
    exports org.BOGO.controller;
    exports org.BOGO.domain.booking;
    exports org.BOGO.domain.common;
    exports org.BOGO.domain.communication;
    exports org.BOGO.domain.transport;
    exports org.BOGO.domain.user;
    exports org.BOGO.repository;
    exports org.BOGO.service;
    exports org.BOGO.simulation;
}
