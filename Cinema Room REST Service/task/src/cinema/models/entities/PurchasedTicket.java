package cinema.models.entities;

public class PurchasedTicket {
    private String token;
    private Ticket ticket;

    public PurchasedTicket(String token, Ticket ticket) {
        this.token = token;
        this.ticket = ticket;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}