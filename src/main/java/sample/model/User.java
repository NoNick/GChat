package sample.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User {

    @Id
    private String name;
    @Column
    private Integer rank;

    @JsonIgnore
    @OneToMany(mappedBy = "user")
    private List<Message> messages;

    @ManyToMany
    private Set<Room> userRooms;

}
