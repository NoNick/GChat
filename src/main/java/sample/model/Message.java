package sample.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.simple.JSONObject;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "messages")
public class Message {
    @Id
    @GeneratedValue
    private Long id;
    private String text;
    @JsonFormat(pattern = "dd-MM-yyyy hh:mm")
    private LocalDateTime created;
    private String roomName;
    private String userName;
    private boolean secret;

    @ManyToOne
    @JsonIgnore
    private User user;

    @ManyToOne
    @JsonIgnore
    private Room room;


    public JSONObject toJSON() {
        JSONObject result = new JSONObject();
        result.put("action", "message");
        result.put("text", text);
        result.put("created", created);
        result.put("room", roomName);
        result.put("author", userName);
        result.put("secret", secret);
        return result;
    }

}
