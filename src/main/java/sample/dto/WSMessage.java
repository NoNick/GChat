package sample.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WSMessage {

    private String action;
    private String name;
    private String hash;
    private String room;
    private String message;
    private boolean isSecret;

}
