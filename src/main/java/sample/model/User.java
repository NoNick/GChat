package sample.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = "userRooms")
@ToString(exclude = {"userRooms", "messages"})
@Table(name = "user")
public class User {
    @Id
    private String name;
    private UUID uuid;
    @Column
    private Integer rank;

    @JsonIgnore
    @OneToMany(mappedBy = "user")
    private List<Message> messages;

    @JsonIgnore
    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Room> userRooms = Collections.emptySet();

}
