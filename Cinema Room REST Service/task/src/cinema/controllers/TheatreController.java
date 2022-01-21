package cinema.controllers;

import cinema.models.*;
import cinema.models.entities.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
public class TheatreController {
    static int theatreRows = 9;
    static int theatreColumns = 9;

    List<PurchasedTicket> purchasedTickets;
    TheatreInfo seats;

    public TheatreController() {
        InitData();
    }

    private void InitData() {
        List<Ticket> tickets = new ArrayList<>();
        for (int r = 1; r <= theatreRows; r++) {
            for (int c = 1; c <= theatreColumns; c++) {
                tickets.add(new Ticket(r, c, (r <= 4 ? 10 : 8)));
            }
        }

        seats = new TheatreInfo(theatreRows, theatreColumns, tickets);
        purchasedTickets = new ArrayList<>();
    }

    @GetMapping("/seats")
    public TheatreInfo getSeats() {
        return seats;
    }

    @PostMapping(value = "/purchase", consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> postPurchase(@RequestBody Seat requestedSeat) {
        int r = requestedSeat.getRow();
        int c = requestedSeat.getColumn();
        if (r <= 0 || r > theatreRows || c <= 0 || c > theatreColumns) {
            return new ResponseEntity<>(new ErrorMessage("The number of a row or a column is out of bounds!"), HttpStatus.BAD_REQUEST);
        }

        Ticket availableTicket = seats.getAvailable_seats().stream().filter(s -> s.getRow() == r && s.getColumn() == c).findAny().orElse(null);
        if (availableTicket == null) {
            return new ResponseEntity<>(new ErrorMessage("The ticket has been already purchased!"), HttpStatus.BAD_REQUEST);
        }

        PurchasedTicket purchasedTicket = new PurchasedTicket(UUID.randomUUID().toString(), availableTicket);
        purchasedTickets.add(purchasedTicket);

        List<Ticket> tickets = seats.getAvailable_seats().stream().filter(t -> !t.equals(availableTicket)).collect(Collectors.toList());
        seats.setAvailable_seats(tickets);

        return new ResponseEntity<>(purchasedTicket, HttpStatus.OK);
    }

    @PostMapping(value = "/return", consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> postPurchase(@RequestBody RequestedToken requestedToken) {
        String t = requestedToken.getToken();

        PurchasedTicket purchasedTicket = purchasedTickets.stream().filter(s -> s.getToken().equals(t)).findAny().orElse(null);
        if (purchasedTicket == null) {
            return new ResponseEntity<>(new ErrorMessage("Wrong token!"), HttpStatus.BAD_REQUEST);
        }

        purchasedTickets.remove(purchasedTicket);

        List<Ticket> tickets = seats.getAvailable_seats();
        Ticket ticket = purchasedTicket.getTicket();
        tickets.add(ticket);
        seats.setAvailable_seats(tickets);

        ReturnedTicket returnedTicket = new ReturnedTicket(ticket);

        return new ResponseEntity<>(returnedTicket, HttpStatus.OK);
    }

    @PostMapping(value = "/stats", consumes = {MediaType.ALL_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> postPurchase(@RequestParam(required = false) String password) {
        if (password == null || !password.equals("super_secret")) {
            return new ResponseEntity<>(new ErrorMessage("The password is wrong!"), HttpStatus.UNAUTHORIZED);
        }

        int income = purchasedTickets.stream().mapToInt(t -> t.getTicket().getPrice()).sum();
        int availableSeats = theatreRows * theatreColumns - purchasedTickets.size();
        Statistics statistics = new Statistics(income, availableSeats, purchasedTickets.size());

        return new ResponseEntity<>(statistics, HttpStatus.OK);
    }
}