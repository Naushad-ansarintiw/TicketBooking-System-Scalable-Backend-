package org.example;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

import org.example.entities.Train;
import org.example.entities.User;
import org.example.services.UserBookingService;
import org.example.util.UserServiceUtil;

public class App {

    public static void main(String[] args) {
        System.out.println("Running Train Booking System");
        
        // Add memory monitoring
        Runtime runtime = Runtime.getRuntime();
        System.out.println("Initial memory: " + runtime.totalMemory() / (1024 * 1024) + "MB");
        
        Scanner scanner = new Scanner(System.in);
        int option = 0;
        UserBookingService userBookingService = null;
        Train trainSelectedForBooking = null;

        try {
            userBookingService = new UserBookingService();
            trainSelectedForBooking = new Train();
            // System.out.println("MAIN: Initial UserBookingService created - " + 
            //     userBookingService.hashCode()); // Debug
            System.out.println("UserBookingService initialized successfully");
        } catch (IOException ex) {
            System.out.println("Error initializing UserBookingService: " + ex.getMessage());
            ex.printStackTrace();
            return;
        }

        int loopCount = 0; // Safety counter to prevent infinite loops
        final int MAX_LOOPS = 1000; // Maximum number of menu iterations
        
        while (option != 7 && loopCount < MAX_LOOPS) {
            loopCount++;
            
            // Add garbage collection hint occasionally
            if (loopCount % 50 == 0) {
                System.gc();
            }
            
            try {
                System.out.println("\n=== Menu (Loop: " + loopCount + ") ===");
                System.out.println("1. Sign up");
                System.out.println("2. Login");
                System.out.println("3. Fetch Bookings");
                System.out.println("4. Search Trains");
                System.out.println("5. Book a Seat");
                System.out.println("6. Cancel my Booking");
                System.out.println("7. Exit the App");
                System.out.print("Choose an option (1-7): ");
                
                String input = scanner.nextLine().trim();
                
                if (input.isEmpty()) {
                    System.out.println("Empty input, please try again.");
                    continue;
                }
                
                try {
                    option = Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input '" + input + "'. Please enter a number between 1-7.");
                    continue;
                }

                switch (option) {
                    case 1:
                        handleSignUp(scanner, userBookingService);
                        break;
                    case 2:
                        // System.out.println("MAIN: Before login - userBookingService: " + 
                        // userBookingService.hashCode()); // Debug
                        handleLogin(scanner, userBookingService);
                        // System.out.println("MAIN: Before login - userBookingService: " + 
                        // userBookingService.hashCode()); // Debug
                        break;
                    case 3:
                        // System.out.println("MAIN: Fetch bookings - userBookingService: " + 
                        // userBookingService.hashCode());
                        handleFetchBookings(userBookingService);
                        // System.out.println("MAIN: Fetch bookings - userBookingService: " + 
                        // userBookingService.hashCode());
                        break;
                    case 4:
                        trainSelectedForBooking = handleSearchTrains(scanner, userBookingService);
                        break;
                    case 5:
                        handleBookSeat(scanner, userBookingService, trainSelectedForBooking);
                        break;
                    case 6:
                        handleCancelBooking();
                        break;
                    case 7:
                        System.out.println("Exiting the application. Goodbye!");
                        break;
                    default:
                        System.out.println("Invalid option. Please choose 1-7.");
                        break;
                }
                
            } catch (Exception e) {
                System.out.println("An unexpected error occurred: " + e.getMessage());
                e.printStackTrace();
                System.out.println("Returning to main menu...");
            }
        }
        
        if (loopCount >= MAX_LOOPS) {
            System.out.println("Safety limit reached. Exiting to prevent memory issues.");
        }
        
        scanner.close();
        System.out.println("Application closed successfully.");
        
        // Final memory stats
        long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
        System.out.println("Final memory usage: " + usedMemory + "MB");
    }
    
    private static void handleSignUp(Scanner scanner, UserBookingService userBookingService) {
        System.out.println("=== User Sign Up ===");
        System.out.print("Enter username: ");
        String nameToSignUp = scanner.nextLine().trim();
        System.out.print("Enter password: ");
        String passwordToSignUp = scanner.nextLine().trim();
        
        if (nameToSignUp.isEmpty() || passwordToSignUp.isEmpty()) {
            System.out.println("Username and password cannot be empty.");
            return;
        }
        
        User userToSignup = new User(nameToSignUp, passwordToSignUp, 
                UserServiceUtil.hashPassword(passwordToSignUp), 
                new ArrayList<>(), UUID.randomUUID().toString());
        userBookingService.signUp(userToSignup);
    }
    
    private static void handleLogin(Scanner scanner, UserBookingService userBookingService) {
        System.out.println("=== User Login ===");
        System.out.print("Enter username: ");
        String nameToLogin = scanner.nextLine().trim();
        System.out.print("Enter password: ");
        String passwordToLogin = scanner.nextLine().trim();
        
        if (nameToLogin.isEmpty() || passwordToLogin.isEmpty()) {
            System.out.println("Username and password cannot be empty.");
            return;
        }
        
        try {
            Boolean loginSuccess = userBookingService.loginUser(nameToLogin, passwordToLogin);
            if (loginSuccess) {
                System.out.println("Login successful!");
            } else {
                System.out.println("Login failed!");
            }
        } catch (Exception ex) {
            System.out.println("Login failed: " + ex.getMessage());
        }
    }
    
    private static void handleFetchBookings(UserBookingService userBookingService) {
        System.out.println("=== Your Bookings ===");
        System.out.println("FETCHING: Before creating new - userBookingService: " + 
                userBookingService.hashCode()); // Debug
        userBookingService.fetchBooking();
    }
    
    private static Train handleSearchTrains(Scanner scanner, UserBookingService userBookingService) {
        System.out.println("=== Search Trains ===");
        System.out.print("Enter source station: ");
        String source = scanner.nextLine().trim();
        System.out.print("Enter destination station: ");
        String dest = scanner.nextLine().trim();
        
        if (source.isEmpty() || dest.isEmpty()) {
            System.out.println("Source and destination cannot be empty.");
            return null;
        }
        
        List<Train> trains = userBookingService.getTrains(source, dest);
        
        if (trains == null || trains.isEmpty()) {
            System.out.println("No trains found for the given route.");
            return null;
        }
        
        System.out.println("\nAvailable Trains:");
        int index = 1;
        for (Train t : trains) {
            System.out.println(index + ". Train ID: " + t.getTrainId());
            for (Map.Entry<String, String> entry : t.getStationTimes().entrySet()) {
                System.out.println("   Station: " + entry.getKey() + " | Time: " + entry.getValue());
            }
            System.out.println();
            index++;
        }
        
        System.out.print("Select a train (1-" + trains.size() + "): ");
        String trainInput = scanner.nextLine().trim();
        try {
            int trainChoice = Integer.parseInt(trainInput) - 1;
            if (trainChoice >= 0 && trainChoice < trains.size()) {
                Train selectedTrain = trains.get(trainChoice);
                System.out.println("Selected train: " + selectedTrain.getTrainId());
                return selectedTrain;
            } else {
                System.out.println("Invalid train selection.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number.");
        }
        return null;
    }
    
    private static void handleBookSeat(Scanner scanner, UserBookingService userBookingService, Train train) {
        if (train == null) {
            System.out.println("Please search and select a train first (option 4)");
            return;
        }
        
        System.out.println("=== Book a Seat ===");
        System.out.println("Available seats for Train " + train.getTrainId() + ":");
        
        List<List<Integer>> seats = userBookingService.fetchSeats(train);
        if (seats == null || seats.isEmpty()) {
            System.out.println("No seats available or error fetching seats.");
            return;
        }
        
        for (int i = 0; i < seats.size(); i++) {
            System.out.print("Row " + i + ": ");
            for (Integer val : seats.get(i)) {
                System.out.print(val + " ");
            }
            System.out.println();
        }
        
        try {
            System.out.print("Enter row number: ");
            int row = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Enter column number: ");
            int col = Integer.parseInt(scanner.nextLine().trim());
            
            System.out.println("Booking your seat....");
            Boolean booked = userBookingService.bookTrainSeat(train, row, col);
            if (booked.equals(Boolean.TRUE)) {
                System.out.println("Booked! Enjoy your journey");
            } else {
                System.out.println("Can't book this seat. It might be already taken.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Please enter valid numbers for row and column.");
        } catch (Exception e) {
            System.out.println("Error booking seat: " + e.getMessage());
        }
    }
    
    private static void handleCancelBooking() {
        System.out.println("=== Cancel Booking ===");
        System.out.println("This feature is not implemented yet.");
    }
}