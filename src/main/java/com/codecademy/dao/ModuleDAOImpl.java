package com.codecademy.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.codecademy.database.DbConnection;
import com.codecademy.domain.Module;
import com.codecademy.domain.ModuleProgress;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * 
 * This class implements the ModuleDAO interface and defines methods to perform
 * CRUD operations on the Module table in the database.
 * 
 * It uses a DbConnection object to establish a connection to the database.
 */
public class ModuleDAOImpl implements ModuleDAO {

    private DbConnection dbConnection;

    /**
     * 
     * Constructor for the ModuleDAOImpl class.
     * 
     * @param dbConnection a DbConnection object used to establish a connection to
     *                     the database.
     */
    public ModuleDAOImpl(DbConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    /**
     * 
     * Retrieves the average progress per module for a given course and student.
     * 
     * @param courseName   the name of the course for which to retrieve the average
     *                     progress.
     * @param studentEmail the email of the student for which to retrieve the
     *                     average progress.
     * @return a List of ModuleProgress objects, each representing a module with its
     *         average progress.
     */
    @Override
    public List<ModuleProgress> getAverageProgressPerModule(String courseName, String studentEmail) {
        List<ModuleProgress> modules = new ArrayList<>();
        try (Connection db = dbConnection.getConnection()) {

            // Get the average progress per module for the selected course
            String averageProgressQuery = "SELECT M.FollowNumber, M.ModuleTitle, SC.percentage AS progress " +
                    "FROM Module M " +
                    "JOIN Content C ON M.ContentID = C.ContentID " +
                    "JOIN Student_Content SC ON C.ContentID = SC.ContentID " +
                    "WHERE M.CourseName = ? " +
                    "AND SC.StudentEmail = ? " +
                    "ORDER BY M.FollowNumber;";

            PreparedStatement averageProgressStatement1 = db.prepareStatement(averageProgressQuery);
            averageProgressStatement1.setString(1, courseName);
            averageProgressStatement1.setString(2, studentEmail);

            ResultSet resultSet1 = averageProgressStatement1.executeQuery();

            System.out.println("Average progress per module for course ID " + courseName + ":" + studentEmail);
            while (resultSet1.next()) {
                int moduleId = resultSet1.getInt("FollowNumber");
                String title = resultSet1.getString("ModuleTitle");
                double averageProgress = resultSet1.getDouble("progress");
                System.out.println("Module ID: " + moduleId + ", Title: " + title + ", Average progress: "
                        + averageProgress + "%");
                ModuleProgress module = new ModuleProgress(moduleId, title, averageProgress);
                modules.add(module);
            }
        } catch (SQLException e) {
            System.out.println("Error while getting the average progress per module: " + e.getMessage());
        }
        return modules;
    }

    /**
     * 
     * Retrieves the average progress per module for a given course and all
     * students.
     * 
     * @param courseName the name of the course for which to retrieve the average
     *                   progress.
     * @return a List of ModuleProgress objects, each representing a module with its
     *         average progress.
     */
    public List<ModuleProgress> getAverageProgressPerModuleAllStudents(String courseName) {
        List<ModuleProgress> moduleProgressList = new ArrayList<>();
        try (Connection db = dbConnection.getConnection()) {
            // Get the average progress per module for the selected course and all students
            String averageProgressQueryAllStudents = "SELECT M.FollowNumber, M.ModuleTitle, AVG(SC.percentage) AS progress "
                    +
                    "FROM Module M " +
                    "JOIN Content C ON M.ContentID = C.ContentID " +
                    "JOIN Student_Content SC ON C.ContentID = SC.ContentID " +
                    "JOIN Student S ON SC.StudentEmail = S.StudentEmail " +
                    "WHERE M.CourseName = ? " +
                    "GROUP BY M.FollowNumber, M.ModuleTitle " +
                    "ORDER BY M.FollowNumber;";

            PreparedStatement averageProgressStatement = db.prepareStatement(averageProgressQueryAllStudents);
            averageProgressStatement.setString(1, courseName);

            ResultSet resultSet = averageProgressStatement.executeQuery();
            while (resultSet.next()) {
                int followNumber = resultSet.getInt("FollowNumber");
                String title = resultSet.getString("ModuleTitle");
                double averageProgress = resultSet.getDouble("progress");
                System.out.println("Module ID: " + followNumber + ", Title: " + title + ", Average progress: "
                        + averageProgress + "%");
                ModuleProgress moduleProgress = new ModuleProgress(followNumber, title, averageProgress);
                moduleProgressList.add(moduleProgress);
            }
        } catch (SQLException e) {
            System.out.println("Error while getting the average progress per module: " + e.getMessage());
        }
        return moduleProgressList;
    }

    /**
     * 
     * Retrieves all modules from the Module table.
     * 
     * @return an ObservableList of Module objects, each representing a row in the
     *         Module table.
     */
    @Override
    public ObservableList<Module> getAllModules() {
        try (Connection db = dbConnection.getConnection()) {
            PreparedStatement query = db.prepareStatement("SELECT * FROM Module");
            ResultSet result = query.executeQuery();

            ObservableList<Module> list = FXCollections.observableArrayList();

            while (result.next()) {
                list.add(new Module(result.getInt("FollowNumber"),
                        result.getString("ModuleTitle"),
                        result.getInt("contentID"),
                        result.getFloat("Version"),
                        result.getString("CourseName"),
                        result.getString("ContactpersonEmail")));
            }
            return list;
        } catch (Exception e) {
            System.err.println("Error in getStudents");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 
     * Inserts a new module into the Module table.
     * 
     * @param module the Module object to insert into the table.
     */
    @Override
    public void addModule(Module module) {
        try (Connection db = dbConnection.getConnection()) {
            PreparedStatement query = db.prepareStatement(
                    "INSERT INTO Module (ContentID, ModuleTitle, Version, ContactpersonEmail, CourseName) VALUES (?, ?, ?, ?, ?)");
            query.setInt(1, module.getContentId());
            query.setString(2, module.getModuleTitle());
            query.setFloat(3, module.getVersion());
            query.setString(4, module.getContactPersonEmail());
            query.setString(5, module.getCourseName());
            query.executeUpdate();
        } catch (Exception e) {
            System.err.println("Error in addModule");
            e.printStackTrace();
        }
    }

    /**
     * 
     * Updates a module in the Module table.
     * 
     * @param module the Module object to update in the table.
     */
    @Override
    public void updateModule(Module module) {
        try (Connection db = dbConnection.getConnection()) {
            PreparedStatement query = db.prepareStatement(
                    "UPDATE Module SET ContentID = ?, ModuleTitle = ?, Version = ?, ContactpersonEmail = ?, CourseName = ? WHERE FollowNumber = ?");
            query.setInt(1, module.getContentId());
            query.setString(2, module.getModuleTitle());
            query.setFloat(3, module.getVersion());
            query.setString(4, module.getContactPersonEmail());
            query.setString(5, module.getCourseName());
            query.setInt(6, module.getFollowNumber());
            query.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error while updating module: " + e.getMessage());
        }
    }

    /**
     * 
     * Deletes a module from the Module table.
     * 
     * @param followNumber the FollowNumber of the module to delete from the table.
     */
    @Override
    public void deleteModule(int followNumber) {
        try (Connection db = dbConnection.getConnection()) {
            PreparedStatement query = db.prepareStatement("DELETE FROM Module WHERE FollowNumber = ?");
            query.setInt(1, followNumber);
            query.executeUpdate();
        } catch (Exception e) {

        }
    }
}
