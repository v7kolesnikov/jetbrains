package cinema.models.entities;

public class RequestedToken {
    private String token;

    public RequestedToken() {
    }

    public RequestedToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}