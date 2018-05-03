package sample.model;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "rooms")
public class Room {
    @Id
    private String name;
    @OneToMany(mappedBy = "room")
    private Set<Message> messages;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Message> getMessages() {
        return messages;
    }

    public void setMessages(Set<Message> messages) {
        this.messages = messages;
    }
}
