package sample.model;

import org.json.simple.JSONObject;

import javax.persistence.*;

@Entity
public class Message {
    @Id
    @GeneratedValue
    @Column
    private int id;
    @Column
    private String text;
    @Column
    private long epoch;
    @Column
    private boolean secret;

    @OneToOne
    private User user;

    @ManyToOne
    private Room room;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public long getEpoch() {
        return epoch;
    }

    public void setEpoch(long epoch) {
        this.epoch = epoch;
    }

    public boolean isSecret() {
        return secret;
    }

    public void setSecret(boolean secret) {
        this.secret = secret;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public JSONObject toJSON() {
        JSONObject result = new JSONObject();
        result.put("action", "message");
        result.put("text", text);
        result.put("epoch", epoch);
        result.put("room", getRoom().getName());
        return result;
    }
}
