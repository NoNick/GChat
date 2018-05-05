package sample.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Receiver {

    private Long messageId;
    private List<String> recipients;
}
