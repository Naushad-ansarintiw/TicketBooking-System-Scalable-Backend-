package org.example.services;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.example.entities.Train;
import org.example.entities.User;
import org.example.util.UserServiceUtil;

public class UserBookingService {
    private User user;
    private List<User> userList; 
    private ObjectMapper objectMapper = new ObjectMapper();


    private static final String USERS_PATH = "app/src/main/java/org/example/localDb/users.json";
    
    public UserBookingService(User user1) throws IOException
    {
        this.user = user1;
        loadUserListFromFile();
    }

    public UserBookingService() throws IOException {
        loadUserListFromFile();
    }

    private void loadUserListFromFile() throws IOException {
        userList = objectMapper.readValue(new File(USERS_PATH), new TypeReference<List<User>>() {});
    }

    public Boolean loginUser(){
        Optional<User> foundUser = userList.stream().filter(user1 -> {
            return user1.getName().equals(user.getName()) && UserServiceUtil.checkPassword(user.getPassword(), user1.getHashedPassword());
        }).findFirst();
        return foundUser.isPresent();
    }

    public Boolean signUp(User user1){
        try{
            userList.add(user1);
            saveUserListToFile();
            return Boolean.TRUE;
        }catch (IOException ex){
            return Boolean.FALSE;
        }
    }

    private void saveUserListToFile() throws IOException {
        File usersFile = new File(USERS_PATH); 
        objectMapper.writeValue(usersFile, userList);
    }

    public void fetchBooking(){
        user.printTickets();
    }

    public Boolean cancelBooking(String tickedId) {
        Scanner s = new Scanner(System.in);
        System.out.print("Enter the ticket id to cancel: ");
        String ticketId = s.next();

        if (ticketId.isEmpty()) {
            System.out.println("Ticket ID cannot be empty.");
            s.close();
            return false;
        }

        boolean removed = user.getTicketsBooked()
            .removeIf(ticket -> ticketId.equals(ticket.getTicketId()));

        if (removed) {
            System.out.println("Ticket " + ticketId + " canceled successfully!");
        } else {
            System.out.println("No ticket found with ID " + ticketId);
        }

        s.close();
        return removed;
    }

    public List<Train> getTrains(String source, String destination) {
        try {
            TrainService trainService = new TrainService();
            return trainService.searchTrains(source, destination);
        } catch (IOException ex) {
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
            if(row >= 0 && row < seats.size() && seat >= 0 && seat<seats.get(row).size()) {
                seats.get(row).set(seat, 1);
                train.setSeats(seats);
                trainService.addTrain(train);
                return true;
            }
            else {
                return false;
            }
        } catch (IOException ex) {
            return Boolean.FALSE;
        }
    }

}
