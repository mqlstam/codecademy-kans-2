package com.codecademy.controllers;

import com.codecademy.MainMenu;
import com.codecademy.dao.CertificateDAO;
import com.codecademy.dao.CertificateDAOimpl;
import com.codecademy.database.DbConnection;
import com.codecademy.domain.Certificate;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * 
 * This class represents a controller for displaying and managing certificates
 * in a TableView.
 * 
 * It provides methods for displaying the certificates, adding a new
 * certificate, editing an existing certificate, and deleting a certificate.
 */

public class CertificateController {

    /**
     * 
     * Displays a TableView with a list of certificates, along with buttons for
     * adding, editing, and deleting certificates.
     * 
     * Allows the user to navigate back to the main menu.
     */

    public static void display() {
        Stage stage = new Stage();
        stage.setTitle("Anhtuan Nguyen(2192526), Luuk beks(2192527), Miquel Stam(2192528)");
        stage.setWidth(1000);
        stage.setHeight(800);
        stage.setResizable(false);

        FlowPane root = new FlowPane();
        TableView<Certificate> table = new TableView();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Label certificateOverview = new Label("Certificates overview");
        certificateOverview.setFont(Font.font("Arial", FontWeight.BOLD, 30));

        Button add = new Button("Add");
        Button edit = new Button("Edit");
        Button delete = new Button("Delete");
        Button back = new Button("Back");

        HBox hBox = new HBox();

        hBox.getChildren().addAll(add, edit, delete, back);
        hBox.setSpacing(25);

        add.setPrefSize(50, 30);
        edit.setPrefSize(50, 30);
        delete.setPrefSize(80, 30);
        back.setPrefSize(50, 30);
        table.setEditable(false);

        CertificateDAO certificateDAO = new CertificateDAOimpl(new DbConnection());
        ObservableList list = certificateDAO.getCertificates();

        table.setItems(list);

        TableColumn<Certificate, Integer> certificateIDCol = new TableColumn("certificateID");
        certificateIDCol.setCellValueFactory(new PropertyValueFactory<Certificate, Integer>("certificateID"));
        TableColumn<Certificate, Double> gradeCol = new TableColumn("Grade");
        gradeCol.setCellValueFactory(new PropertyValueFactory<Certificate, Double>("Grade"));
        TableColumn<Certificate, String> employeeCol = new TableColumn("Employee");
        employeeCol.setCellValueFactory(new PropertyValueFactory<Certificate, String>("Employee"));

        table.getColumns().addAll(certificateIDCol, gradeCol, employeeCol);

        VBox vbox = new VBox();
        vbox.setSpacing(10);
        vbox.setPadding(new Insets(10, 0, 0, 10));
        vbox.getChildren().addAll(certificateOverview, table, hBox);

        root.setAlignment(Pos.CENTER);
        root.getChildren().addAll(vbox);

        Scene scene = new Scene(root);

        add.setOnAction(e -> {
            AddCertificateController.display();
            stage.close();
        });

        edit.setOnAction(e -> {
            Certificate certificate = table.getSelectionModel().getSelectedItem();
            if (certificate != null) {
                EditCertificateController certificateController = new EditCertificateController(certificate);
                EditCertificateController.display(certificate);
                stage.close();
            } else {
                System.out.println("No certificate selected");
            }
        });

        delete.setOnAction(e -> {
            Certificate certificate = table.getSelectionModel().getSelectedItem();
            if (certificate != null) {
                certificateDAO.deleteCertificate(certificate);
                stage.close();
                display();
            } else {
                System.out.println("No certificate selected");
            }
        });

        back.setOnAction(e -> {
            MainMenu.display();
            stage.close();
        });

        stage.setScene(scene);
        stage.show();
    }
}
