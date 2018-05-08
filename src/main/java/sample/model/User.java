package sample.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"userRooms", "messages"})
@ToString(exclude = {"userRooms", "messages"})
@Table(name = "user")
public class User {
    @Id
    @NotNull
    @NotEmpty
    private String name;
    private UUID uuid;

    @Column
    @NotNull
    private Integer rank;

    @JsonIgnore
    @OneToMany(mappedBy = "user")
    private Set<Message> messages;

    @JsonIgnore
    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Room> userRooms = Collections.emptySet();

}
