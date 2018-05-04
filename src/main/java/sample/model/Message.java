package sample.model;

import lombok.Data;
import org.json.simple.JSONObject;

import javax.persistence.*;

@Entity
@Data
public class Message {
    @Id
    @GeneratedValue
    private int id;
    private String text;
    private long epoch;
    private boolean secret;

    @OneToOne
    private User user;

    @ManyToOne
    private Room room;


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

}
