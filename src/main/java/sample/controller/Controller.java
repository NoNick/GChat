package sample.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import sample.dto.Receiver;
import sample.service.MessageService;
import sample.service.RoomService;
import sample.utils.Ranks;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@EnableWebMvc
@RestController
@RequestMapping("/")
class Controller {

    private final MessageService messageService;
    private final RoomService roomService;

    @Autowired
    public Controller(MessageService messageService, RoomService roomService) {
        this.messageService = messageService;
        this.roomService = roomService;
    }

    @PostMapping(value = "/salute")
    public @ResponseBody
    String echo(@RequestParam(name = "name") String name,
                @RequestParam(name = "hash") String hash) {

        Optional<Integer> rank = Ranks.getRank(name, hash);
        String result = "Unauthorized";

        if (rank.isPresent()) {
            result = "You are " + Ranks.getRankName(rank.get());
        }
        return result;
    }

    @PostMapping(value = "/pleaseGeneral")
    public List<Receiver> getReceivedMessages() {
        return messageService.getReceivers();
    }

    @PostMapping(value = "/rooms")
    public Map<String, Integer> getRooms() {
        return roomService.getMessagesCountInAllRooms();
    }

}


