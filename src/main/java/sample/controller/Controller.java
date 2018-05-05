package sample.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import sample.dto.Receiver;
import sample.model.Message;
import sample.model.User;
import sample.service.MessageService;
import sample.service.RoomService;
import sample.utils.Ranks;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

    @PostMapping(value = "/salute", produces = "application/json; charset=UTF-8")
    public @ResponseBody
    String echo(@RequestParam(name = "name") String name, @RequestParam(name = "hash") String hash)
            throws UnsupportedEncodingException, NoSuchAlgorithmException {
        final String[] result = new String[]{"Unauthorized"};
        Ranks.getRank(name, hash).ifPresent(rank -> result[0] = "You are " + Ranks.getRankName(rank));
        return result[0];
    }

    @PostMapping(value = "/pleaseGeneral", produces = "application/json; charset=UTF-8")
    public List<Receiver> getReceivedMessages() {
        return constructReceiversResponse(messageService.getReceivers());
    }

    @PostMapping(value = "/rooms", produces = "application/json; charset=UTF-8")
    public Map<String, Integer> getRooms() {
        return roomService.getMessagesCountInAllRooms();
    }

    private List<Receiver> constructReceiversResponse(Map<Message, Set<User>> receivers) {
        List<Receiver> receiversList = new ArrayList<>();

        receivers.forEach((message, users) -> {
            Receiver receiver = Receiver.builder()
                    .messageId(message.getId())
                    .recipients(users.stream().map(User::getName).collect(Collectors.toList()))
                    .build();
            receiversList.add(receiver);
        });

        return receiversList;
    }
}


