import java.util.ArrayList;
import java.util.Scanner;

/**
 * Student Grade Tracker
 * 
 * An interactive console application to input and manage student grades,
 * calculate statistics (average, highest, lowest), and display structured formatting.
 */
public class StudentGradeTracker {

    // Inner class representing a Student and their grade
    public static class Student {
        private String name;
        private double score;

        public Student(String name, double score) {
            this.name = name;
            this.score = score;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public double getScore() {
            return score;
        }

        public void setScore(double score) {
            this.score = score;
        }

        // Returns grade letter based on standard US high school system
        public String getLetterGrade() {
            if (score >= 90) return "A";
            else if (score >= 80) return "B";
            else if (score >= 70) return "C";
            else if (score >= 60) return "D";
            else return "F";
        }
    }

    private static final ArrayList<Student> studentList = new ArrayList<>();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        // Pre-populate with some sample student grades for a quick start
        prepopulateSampleData();

        boolean running = true;
        showWelcomeBanner();

        while (running) {
            showMenu();
            System.out.print("\nChoose an option (1-6): ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    addStudent();
                    break;
                case "2":
                    displayAllStudents();
                    break;
                case "3":
                    showGradeStatistics();
                    break;
                case "4":
                    searchStudent();
                    break;
                case "5":
                    clearAllGrades();
                    break;
                case "6":
                    running = false;
                    System.out.println("\nThank you for using the Java Student Grade Tracker! Goodbye.");
                    break;
                default:
                    System.out.println("Invalid selection. Please enter a number between 1 and 6.");
            }
        }
        scanner.close();
    }

    private static void prepopulateSampleData() {
        studentList.add(new Student("Alice Smith", 88.5));
        studentList.add(new Student("Bob Miller", 94.0));
        studentList.add(new Student("Charlie Davis", 72.0));
        studentList.add(new Student("Diana Jenkins", 61.5));
        studentList.add(new Student("Evan Wright", 95.5));
    }

    private static void showWelcomeBanner() {
        System.out.println("=================================================================");
        System.out.println("                  STUDENT GRADE TRACKER (JAVA)                   ");
        System.out.println("=================================================================");
        System.out.println("This program allows you to manage student rosters, input scores, ");
        System.out.println("and view real-time statistics (average, highest, lowest).        ");
        System.out.println("Prepopulated with " + studentList.size() + " samples.");
    }

    private static void showMenu() {
        System.out.println("\n---------------------------- MENU ----------------------------");
        System.out.println("1. Add a Student and Grade");
        System.out.println("2. Display Summary Report (All Students)");
        System.out.println("3. Show Grade Statistics (Average, High, Low)");
        System.out.println("4. Search Student and Update Score");
        System.out.println("5. Clear All Student Records");
        System.out.println("6. Exit Program");
        System.out.println("--------------------------------------------------------------");
    }

    private static void addStudent() {
        System.out.println("\n---> ADD NEW STUDENT <---");
        String name = "";
        while (name.isEmpty()) {
            System.out.print("Enter Student Name: ");
            name = scanner.nextLine().trim();
            if (name.isEmpty()) {
                System.out.println("Error: Student name cannot be empty.");
            }
        }

        double score = -1;
        while (score < 0 || score > 100) {
            System.out.print("Enter Student Score (0.0 to 100.0): ");
            String scoreInput = scanner.nextLine().trim();
            try {
                score = Double.parseDouble(scoreInput);
                if (score < 0 || score > 100) {
                    System.out.println("Error: Score must be between 0.0 and 100.0.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: Please enter a valid decimal number.");
            }
        }

        Student student = new Student(name, score);
        studentList.add(student);
        System.out.printf("Success: %s with score %.1f (%s) has been added!\n", 
                student.getName(), student.getScore(), student.getLetterGrade());
    }

    private static void displayAllStudents() {
        System.out.println("\n---> STUDENT SUMMARY REPORT <---");
        if (studentList.isEmpty()) {
            System.out.println("No records found in the database.");
            return;
        }

        System.out.printf("%-5s | %-25s | %-12s | %-10s\n", "ID", "Student Name", "Score (100)", "Letter Grade");
        System.out.println("-----------------------------------------------------------------");
        for (int i = 0; i < studentList.size(); i++) {
            Student s = studentList.get(i);
            System.out.printf("%-5d | %-25s | %-12.2f | %-10s\n", (i + 1), s.getName(), s.getScore(), s.getLetterGrade());
        }
        System.out.println("-----------------------------------------------------------------");
        System.out.println("Total Students: " + studentList.size());
    }

    private static void showGradeStatistics() {
        System.out.println("\n---> GRADE STATISTICS <---");
        if (studentList.isEmpty()) {
            System.out.println("No records found. Cannot compute statistics.");
            return;
        }

        double sum = 0;
        double highest = Double.NEGATIVE_INFINITY;
        double lowest = Double.POSITIVE_INFINITY;
        
        Student topStudent = null;
        Student bottomStudent = null;

        for (Student s : studentList) {
            double score = s.getScore();
            sum += score;

            if (score > highest) {
                highest = score;
                topStudent = s;
            }
            if (score < lowest) {
                lowest = score;
                bottomStudent = s;
            }
        }

        double average = sum / studentList.size();

        System.out.printf("Average Score: %.2f\n", average);
        if (topStudent != null) {
            System.out.printf("Highest Score: %.2f (%s, Grade %s)\n", highest, topStudent.getName(), topStudent.getLetterGrade());
        }
        if (bottomStudent != null) {
            System.out.printf("Lowest Score:  %.2f (%s, Grade %s)\n", lowest, bottomStudent.getName(), bottomStudent.getLetterGrade());
        }

        // Percentage distributions
        int aCount = 0, bCount = 0, cCount = 0, dCount = 0, fCount = 0;
        for (Student s : studentList) {
            String letter = s.getLetterGrade();
            switch (letter) {
                case "A": aCount++; break;
                case "B": bCount++; break;
                case "C": cCount++; break;
                case "D": dCount++; break;
                default: fCount++; break;
            }
        }

        System.out.println("\nLetter Grade Distribution:");
        System.out.print("  A (" + aCount + "): "); printBar(aCount);
        System.out.print("  B (" + bCount + "): "); printBar(bCount);
        System.out.print("  C (" + cCount + "): "); printBar(cCount);
        System.out.print("  D (" + dCount + "): "); printBar(dCount);
        System.out.print("  F (" + fCount + "): "); printBar(fCount);
    }

    private static void printBar(int count) {
        for (int i = 0; i < count; i++) {
            System.out.print("█");
        }
        System.out.println();
    }

    private static void searchStudent() {
        System.out.println("\n---> SEARCH AND UPDATE GRADE <---");
        if (studentList.isEmpty()) {
            System.out.println("No student records available.");
            return;
        }

        System.out.print("Enter student name to search for: ");
        String term = scanner.nextLine().trim().toLowerCase();

        if (term.isEmpty()) {
            System.out.println("Search term cannot be empty.");
            return;
        }

        ArrayList<Integer> matchedIndices = new ArrayList<>();
        for (int i = 0; i < studentList.size(); i++) {
            if (studentList.get(i).getName().toLowerCase().contains(term)) {
                matchedIndices.add(i);
            }
        }

        if (matchedIndices.isEmpty()) {
            System.out.println("No matching student found.");
            return;
        }

        System.out.println("\nMatches found:");
        for (int idx : matchedIndices) {
            Student s = studentList.get(idx);
            System.out.printf("[%d] %s - Current Score: %.1f\n", idx + 1, s.getName(), s.getScore());
        }

        System.out.print("\nEnter ID to update (or type anything else to cancel): ");
        String selectionInput = scanner.nextLine().trim();
        try {
            int selectedId = Integer.parseInt(selectionInput) - 1;
            if (matchedIndices.contains(selectedId)) {
                double newScore = -1;
                while (newScore < 0 || newScore > 100) {
                    System.out.print("Enter NEW score for " + studentList.get(selectedId).getName() + ": ");
                    String valueInput = scanner.nextLine().trim();
                    try {
                        newScore = Double.parseDouble(valueInput);
                        if (newScore < 0 || newScore > 100) {
                            System.out.println("Error: Score must be between 0.0 and 100.0.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Error: Please input a valid decimal number.");
                    }
                }
                studentList.get(selectedId).setScore(newScore);
                System.out.println("Success! Updated grade for " + studentList.get(selectedId).getName());
            } else {
                System.out.println("Update canceled or ID not matching shown selections.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Update canceled.");
        }
    }

    private static void clearAllGrades() {
        System.out.print("Are you sure you want to clear all " + studentList.size() + " records? (y/n): ");
        String answer = scanner.nextLine().trim().toLowerCase();
        if (answer.equals("y") || answer.equals("yes")) {
            studentList.clear();
            System.out.println("All grade records deleted successfully.");
        } else {
            System.out.println("Operation canceled.");
        }
    }
}
