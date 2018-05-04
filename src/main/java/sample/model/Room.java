package sample.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Set;

@Entity
@Data
@Table(name = "rooms")
public class Room {
    @Id
    private String name;
    @OneToMany(mappedBy = "room")
    private Set<Message> messages;

}
