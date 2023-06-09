package com.codecademy.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.codecademy.domain.Course;
import com.codecademy.domain.Difficulty;
import com.codecademy.database.DbConnection;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * 
 * Implementation of the CourseDAO interface for accessing course data in a
 * database.
 */
public class CourseDAOImpl implements CourseDAO {
    private DbConnection dbConnection;

    /**
     * 
     * Constructs a CourseDAOImpl object with a specified DbConnection.
     * 
     * @param dbConnection the DbConnection object to use for database access
     */
    public CourseDAOImpl(DbConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    /**
     * 
     * Retrieves a list of all courses in the database.
     * 
     * @return an ObservableList of Course objects
     */
    @Override
    public ObservableList<Course> getCourses() {
        try (Connection db = dbConnection.getConnection()) {
            PreparedStatement query = db.prepareStatement("SELECT * FROM Course");
            ResultSet result = query.executeQuery();

            ObservableList<Course> list = FXCollections.observableArrayList();

            while (result.next()) {
                Difficulty difficulty;
                if (result.getString("Difficulty").equals("Beginner")
                        || result.getString("Difficulty").equals("BEGINNER")) {
                    difficulty = Difficulty.BEGINNER;
                } else if (result.getString("Difficulty").equals("Advanced")
                        || result.getString("Difficulty").equals("ADVANCED")) {
                    difficulty = Difficulty.ADVANCED;
                } else {
                    difficulty = Difficulty.EXPERT;
                }
                list.add(new Course(result.getString("CourseName"), result.getString("CourseTopic"),
                        result.getString("CourseIntroText"), result.getString("CourseTag"), difficulty));
            }
            return list;
        } catch (SQLException e) {
            System.err.println("Error in getStudents");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 
     * Retrieves a list of courses for a given student email address.
     * 
     * @param emailAddress the email address of the student
     * @return a List of Course objects
     */

    @Override
    public List<Course> getCoursesByStudentEmail(String emailAddress) {
        List<Course> courses = new ArrayList<>();
        try (Connection db = dbConnection.getConnection()) {
            String query = "SELECT C.* FROM Course C " +
                    "JOIN Enrollment E ON C.CourseName = E.CourseName WHERE E.StudentEmail = ?";
            PreparedStatement statement = db.prepareStatement(query);
            statement.setString(1, emailAddress);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                String courseName = rs.getString("CourseName");
                String courseTopic = rs.getString("CourseTopic");
                String courseIntroText = rs.getString("CourseIntroText");
                String courseTag = rs.getString("CourseTag");
                Difficulty difficulty = Difficulty.valueOf(rs.getString("Difficulty"));
                Course course = new Course(courseName, courseTopic, courseIntroText, courseTag, difficulty);
                courses.add(course);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return courses;
    }

    /**
     * 
     * Retrieves a list of all course names in the database.
     * 
     * @return an ObservableList of Strings representing the course names
     */
    @Override
    public ObservableList<String> getAllCourseNames() {
        try (Connection db = dbConnection.getConnection()) {
            PreparedStatement query = db.prepareStatement("SELECT CourseName FROM Course");
            ResultSet result = query.executeQuery();
            ObservableList<String> list = FXCollections.observableArrayList();

            while (result.next()) {
                list.add(result.getString("CourseName"));
            }
            return list;
        } catch (SQLException e) {
            System.out.println("Error in getAllCourseNames");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 
     * Adds a course to the database.
     * 
     * @param course the Course object to add
     */
    @Override
    public void addCourse(Course course) {
        try (Connection db = dbConnection.getConnection()) {
            PreparedStatement query = db.prepareStatement("INSERT INTO Course VALUES(?, ?, ?, ?, ?)");
            query.setString(1, course.getCourseName());
            query.setString(2, course.getCourseTopic());
            query.setString(3, course.getCourseIntroText());
            query.setString(4, course.getCourseTag());
            query.setString(5, course.getDifficulty().toString());
            query.executeUpdate();
            System.out.println("Course added");
        } catch (SQLException e) {
            System.out.println("Error in addCourse");
            e.printStackTrace();
        }
    }

    /**
     * Updates an existing course in the database.
     * 
     * @param course The Course object representing the course to update.
     */
    @Override
    public void updateCourse(Course course) {
        try (Connection db = dbConnection.getConnection()) {
            PreparedStatement query = db.prepareStatement(
                    "UPDATE Course SET CourseTopic = ?, CourseIntroText = ?, CourseTag = ?, Difficulty = ? WHERE CourseName = ?");
            query.setString(1, course.getCourseTopic());
            query.setString(2, course.getCourseIntroText());
            query.setString(3, course.getCourseTag());
            query.setString(4, course.getDifficulty().toString());
            query.setString(5, course.getCourseName());
            query.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error in updateCourse");
            e.printStackTrace();
        }
    }

    /**
     * Deletes a course from the database. Throws an exception if the course has any
     * associated enrollments.
     * 
     * @param course The Course object representing the course to delete.
     * @throws Exception If the course has associated enrollments.
     */
    @Override
    public void deleteCourse(Course course) throws Exception {
        try (Connection db = dbConnection.getConnection()) {
            // Check if there are any enrollments for the course
            PreparedStatement enrollmentCheck = db
                    .prepareStatement("SELECT COUNT(*) FROM Enrollment WHERE CourseName = ?");
            enrollmentCheck.setString(1, course.getCourseName());
            ResultSet enrollmentResult = enrollmentCheck.executeQuery();
            enrollmentResult.next();
            int enrollmentCount = enrollmentResult.getInt(1);

            if (enrollmentCount > 0) {
                throw new Exception("Cannot delete course with existing enrollments");
            }

            // First, delete the corresponding rows from the Module table
            PreparedStatement moduleQuery = db.prepareStatement("DELETE FROM Module WHERE CourseName = ?");
            moduleQuery.setString(1, course.getCourseName());
            moduleQuery.executeUpdate();

            // Next, delete the corresponding rows from the CourseRecommendation1 table
            PreparedStatement recommendationQuery = db
                    .prepareStatement("DELETE FROM CourseRecommendation1 WHERE CourseName = ?");
            recommendationQuery.setString(1, course.getCourseName());
            recommendationQuery.executeUpdate();
            recommendationQuery = db
                    .prepareStatement("DELETE FROM CourseRecommendation1 WHERE RecommendedCourseName = ?");
            recommendationQuery.setString(1, course.getCourseName());
            recommendationQuery.executeUpdate();

            // Finally, delete the row from the Course table
            PreparedStatement courseQuery = db.prepareStatement("DELETE FROM Course WHERE CourseName = ?");
            courseQuery.setString(1, course.getCourseName());
            courseQuery.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error in deleteCourse");
            e.printStackTrace();
        }
    }

    /**
     * Retrieves a list of the top 3 courses by number of certificates issued.
     * 
     * @return A List of the names of the top 3 certified courses.
     */
    @Override
    public List<String> getTop3CertifiedCourses() {
        List<String> courses = new ArrayList<>();
        try (Connection db = dbConnection.getConnection()) {
            PreparedStatement query = db.prepareStatement(
                    "SELECT TOP 3 CourseName FROM (SELECT CourseName, COUNT(*) as numCertificates FROM Enrollment WHERE CertificateID IS NOT NULL GROUP BY CourseName) AS counts ORDER BY numCertificates DESC");
            ResultSet rs = query.executeQuery();
            while (rs.next()) {
                String courseName = rs.getString("CourseName");
                courses.add(courseName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return courses;
    }

    /**
     * Retrieves a list of recommended courses for a given course.
     * 
     * @param selectedCourse The name of the course to retrieve recommended courses
     *                       for.
     * @return A List of the names of recommended courses for the given course.
     */
    @Override
    public List<String> getRecommendedCourses(String selectedCourse) {
        List<String> recommendedCourses = new ArrayList<>();
        try (Connection conn = dbConnection.getConnection()) {
            String query = "SELECT RecommendedCourseName FROM CourseRecommendation1 WHERE CourseName = ?";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, selectedCourse);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                String recommendedCourse = rs.getString("RecommendedCourseName");
                recommendedCourses.add(recommendedCourse);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return recommendedCourses;
    }

    /**
     * Retrieves the number of students who have completed a given course.
     * 
     * @param courseName The name of the course to retrieve completion numbers for.
     * @return The number of students who have completed the given course.
     */
    @Override
    public int getNumCompletedCourses(String courseName) {
        int numCompleted = 0;
        try (Connection db = dbConnection.getConnection()) {
            PreparedStatement query = db.prepareStatement(
                    "SELECT COUNT(DISTINCT StudentEmail) AS num_completed FROM Enrollment WHERE CourseName = ? AND CertificateID IS NOT NULL");
            query.setString(1, courseName);
            ResultSet rs = query.executeQuery();
            if (rs.next()) {
                numCompleted = rs.getInt("num_completed");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return numCompleted;
    }

}