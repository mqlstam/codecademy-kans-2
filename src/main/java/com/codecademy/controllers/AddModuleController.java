package com.codecademy.controllers;

import com.codecademy.dao.ContactPersonDAO;
import com.codecademy.dao.ContactPersonDAOimpl;
import com.codecademy.dao.ContentDAO;
import com.codecademy.dao.ContentDAOimpl;
import com.codecademy.dao.CourseDAO;
import com.codecademy.dao.CourseDAOImpl;
import com.codecademy.dao.ModuleDAO;
import com.codecademy.dao.ModuleDAOImpl;
import com.codecademy.database.DbConnection;
import com.codecademy.domain.ContactPerson;
import com.codecademy.domain.Content;
import com.codecademy.domain.Course;
import com.codecademy.domain.Module;

import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * The AddModuleController class is responsible for displaying a window for
 * adding a new module to the system.
 * It allows the user to enter the necessary information about the new module,
 * including the content ID, module title, version,
 * contact person email, and course name. The user can then save the new module
 * to the system or go back to the Module window.
 * This class uses the ModuleDAOImpl, ContentDAOimpl, ContactPersonDAOimpl, and
 * CourseDAOImpl classes to interact with the database.
 */
public class AddModuleController {

    /**
     * Displays the add module window.
     * The user can enter information about the new module such as the content,
     * module title, version, contact person and course.
     * The information is then validated and stored in the database.
     * If the information is not valid, an error message will be displayed to the
     * user.
     * Once the module has been added, the module overview window will be displayed.
     */
    public static void display() {
        ModuleDAO moduleDAO = new ModuleDAOImpl(new DbConnection());

        Stage stage = new Stage();
        stage.setTitle("Anhtuan Nguyen(2192526), Luuk beks(2192527), Miquel Stam(2192528)");
        stage.setWidth(1000);
        stage.setHeight(800);
        stage.setResizable(false);

        FlowPane root = new FlowPane();

        HBox contentBox = new HBox();
        Label contentLabel = new Label("Choose content");
        ChoiceBox<Integer> contentId = new ChoiceBox<Integer>();
        ContentDAO contentDAO = new ContentDAOimpl(new DbConnection());
        ObservableList<Content> contents = contentDAO.getContents();
        ObservableList<Integer> contentIds = FXCollections.observableArrayList();
        for (Content content : contents) {
            contentIds.add(content.getContentItemId());
        }
        contentId.setItems(contentIds);

        contentBox.getChildren().addAll(contentLabel, contentId);
        contentBox.setSpacing(10);

        TextField moduleTitle = new TextField();

        TextField version = new TextField();

        HBox contactBox = new HBox();
        Label contactPersonLabel = new Label("Choose contact person: ");
        ChoiceBox<String> contactPersonEmail = new ChoiceBox<>();
        ContactPersonDAO contactPersonDao = new ContactPersonDAOimpl(new DbConnection());
        ObservableList<ContactPerson> contactPersons = contactPersonDao.getContactPersons();
        ObservableList<String> contactPersonEmails = FXCollections.observableArrayList();
        for (ContactPerson contactPerson : contactPersons) {
            contactPersonEmails.add(contactPerson.getContactPersonEmail());
        }
        contactPersonEmail.setItems(contactPersonEmails);
        contactBox.getChildren().addAll(contactPersonLabel, contactPersonEmail);
        contactBox.setSpacing(10);

        HBox courseBox = new HBox();
        Label courseLabel = new Label("Choose course: ");
        ChoiceBox<String> courseName = new ChoiceBox<String>();
        CourseDAO courseDao = new CourseDAOImpl(new DbConnection());
        ObservableList<Course> courses = courseDao.getCourses();
        ObservableList<String> courseNames = FXCollections.observableArrayList();
        for (Course course : courses) {
            courseNames.add(course.getCourseName());
        }
        courseName.setItems(courseNames);
        courseBox.getChildren().addAll(courseLabel, courseName);
        courseBox.setSpacing(10);

        moduleTitle.setPromptText("Module title");
        version.setPromptText("Version");

        Button back = new Button("Back");
        Button save = new Button("Save");

        HBox hBox = new HBox();
        hBox.getChildren().addAll(save, back);
        hBox.setSpacing(70);
        back.setPrefSize(50, 30);
        save.setPrefSize(50, 30);

        VBox vBox = new VBox();
        vBox.getChildren().addAll(contentBox, moduleTitle, version, contactBox, courseBox, hBox);
        vBox.setSpacing(25);

        root.setAlignment(Pos.CENTER);
        root.getChildren().addAll(vBox);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();

        save.setOnAction(e -> {
            float versionFloatValue = 0.0f;
            try {
                versionFloatValue = Float.parseFloat(version.getText());
            } catch (NumberFormatException ex) {
                // Show an error message and return without submitting the form
                Alert alert = new Alert(AlertType.ERROR);
                alert.setHeaderText("Invalid input");
                alert.setContentText("Please enter a valid floating point number for the version field.");
                alert.showAndWait();
                return;
            }
            Module module = new Module();
            module.setContentId(contentId.getValue());
            module.setModuleTitle(moduleTitle.getText());
            module.setVersion(versionFloatValue);
            module.setContactPersonEmail(contactPersonEmail.getValue());
            module.setCourseName(courseName.getValue());
            moduleDAO.addModule(module);
            stage.close();
            ModuleController.display();
        });

        back.setOnAction(e -> {
            ModuleController.display();
            stage.close();
        });
    }
}
