package sample.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.util.Collections;
import java.util.Set;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"messages", "users"})
@ToString(exclude = {"messages", "users"})
@Table(name = "rooms")
public class Room {
    @Id
    private String name;

    @OneToMany(mappedBy = "room")
    @JsonIgnore
    private Set<Message> messages;

    @ManyToMany(mappedBy = "userRooms")
    @JsonIgnore
    private Set<User> users = Collections.emptySet();

}
