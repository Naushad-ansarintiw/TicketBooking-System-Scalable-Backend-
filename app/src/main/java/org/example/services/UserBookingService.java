package org.example.services;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

import org.example.entities.Train;
import org.example.entities.User;
import org.example.util.UserServiceUtil;

public class UserBookingService {
    private User user;
    private List<User> userList; 
    private ObjectMapper objectMapper = new ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

    // Path relative to your package structure
    private static final String USERS_PATH = "src/main/java/org/example/localDb/users.json";
    
    public UserBookingService(User user1) throws IOException {
        this.user = user1;
        loadUserListFromFile();
    }

    public UserBookingService() throws IOException {
        loadUserListFromFile();
    }

    private void loadUserListFromFile() throws IOException {
        File usersFile = new File(USERS_PATH);
        
        System.out.println("Loading users from: " + usersFile.getAbsolutePath());
        
        // If file doesn't exist, create empty list
        if (!usersFile.exists()) {
            System.out.println("Users file not found, creating new one...");
            userList = new ArrayList<>();
            // Create directory if it doesn't exist
            usersFile.getParentFile().mkdirs();
            saveUserListToFile();
            return;
        }
        
        // If file exists but is empty, create empty list
        if (usersFile.length() == 0) {
            userList = new ArrayList<>();
            return;
        }
        
        try {
            userList = objectMapper.readValue(usersFile, new TypeReference<List<User>>() {});
            System.out.println("Loaded " + userList.size() + " users from file.");
        } catch (IOException e) {
            System.out.println("Error reading users file: " + e.getMessage());
            userList = new ArrayList<>();
        }
    }

    public Boolean loginUser() {
        Optional<User> foundUser = userList.stream().filter(user1 -> {
            return user1.getName().equals(user.getName()) && 
                   UserServiceUtil.checkPassword(user.getPassword(), user1.getHashedPassword());
        }).findFirst();
        
        boolean loggedIn = foundUser.isPresent();
        if (loggedIn) {
            System.out.println("Login successful for user: " + user.getName());
            // Update the current user with the one from the file to get their tickets
            this.user = foundUser.get();
        } else {
            System.out.println("Login failed for user: " + user.getName());
        }
        return loggedIn;
    }

    public Boolean signUp(User user1) {
        try {
            // Check if user already exists
            boolean userExists = userList.stream()
                .anyMatch(u -> u.getName().equals(user1.getName()));
            
            if (userExists) {
                System.out.println("User " + user1.getName() + " already exists!");
                return Boolean.FALSE;
            }
            
            userList.add(user1);
            saveUserListToFile();
            System.out.println("User " + user1.getName() + " signed up successfully!");
            return Boolean.TRUE;
        } catch (IOException ex) {
            System.out.println("Error saving user: " + ex.getMessage());
            return Boolean.FALSE;
        }
    }

    private void saveUserListToFile() throws IOException {
        File usersFile = new File(USERS_PATH);
        
        // Create parent directories if they don't exist
        usersFile.getParentFile().mkdirs();
        
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(usersFile, userList);
        System.out.println("Saved " + userList.size() + " users to file: " + usersFile.getAbsolutePath());
    }

    // ... rest of your methods remain the same
    public void fetchBooking() {
        if (user != null) {
            user.printTickets();
        } else {
            System.out.println("No user logged in.");
        }
    }

    public Boolean cancelBooking(String ticketId) {
        if (user == null) {
            System.out.println("No user logged in.");
            return false;
        }

        Scanner s = new Scanner(System.in);
        System.out.print("Enter the ticket id to cancel: ");
        String inputTicketId = s.nextLine().trim();

        if (inputTicketId.isEmpty()) {
            System.out.println("Ticket ID cannot be empty.");
            s.close();
            return false;
        }

        boolean removed = user.getTicketsBooked()
            .removeIf(ticket -> inputTicketId.equals(ticket.getTicketId()));

        if (removed) {
            System.out.println("Ticket " + inputTicketId + " canceled successfully!");
            try {
                saveUserListToFile();
            } catch (IOException e) {
                System.out.println("Error saving changes: " + e.getMessage());
            }
        } else {
            System.out.println("No ticket found with ID " + inputTicketId);
        }

        s.close();
        return removed;
    }

    public List<Train> getTrains(String source, String destination) {
        try {
            TrainService trainService = new TrainService();
            return trainService.searchTrains(source, destination);
        } catch (IOException ex) {
            System.out.println("Error searching trains: " + ex.getMessage());
            return new ArrayList<>();
        }
    }

    public List<List<Integer>> fetchSeats(Train train) {
        return train.getSeats();
    }

    public Boolean bookTrainSeat(Train train, int row, int seat) {
        try {
            TrainService trainService = new TrainService();
            List<List<Integer>> seats = train.getSeats();
            if (row >= 0 && row < seats.size() && seat >= 0 && seat < seats.get(row).size()) {
                if (seats.get(row).get(seat) == 0) {
                    seats.get(row).set(seat, 1);
                    train.setSeats(seats);
                    trainService.updateTrain(train);
                    System.out.println("Seat booked successfully at row " + row + ", seat " + seat);
                    
                    // Add ticket to user and save
                    // You might want to create a Ticket object and add it to user's tickets
                    // user.getTicketsBooked().add(newTicket);
                    saveUserListToFile();
                    
                    return true;
                } else {
                    System.out.println("Seat is already booked!");
                    return false;
                }
            } else {
                System.out.println("Invalid seat selection!");
                return false;
            }
        } catch (IOException ex) {
            System.out.println("Error booking seat: " + ex.getMessage());
            return Boolean.FALSE;
        }
    }
}