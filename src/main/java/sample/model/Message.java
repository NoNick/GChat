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
        result.put("author", user.getName());
        result.put("secret", secret);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Message message = (Message) o;

        if (id != message.id) return false;
        if (epoch != message.epoch) return false;
        if (secret != message.secret) return false;
        if (text != null ? !text.equals(message.text) : message.text != null) return false;
        if (user != null ? !user.equals(message.user) : message.user != null) return false;
        return room != null ? room.equals(message.room) : message.room == null;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (text != null ? text.hashCode() : 0);
        result = 31 * result + (int) (epoch ^ (epoch >>> 32));
        result = 31 * result + (secret ? 1 : 0);
        result = 31 * result + (user != null ? user.getName().hashCode() : 0);
        result = 31 * result + (room != null ? room.getName().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", text='" + text + '\'' +
                ", epoch=" + epoch +
                ", secret=" + secret +
                ", user=" + user.getName() +
                ", room=" + room.getName() +
                '}';
    }
}
