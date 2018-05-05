package sample.controller;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import sample.Ranks;
import sample.model.Message;
import sample.model.User;
import sample.service.MessageService;
import sample.service.MessagingService;
import sample.service.RoomService;
import sample.service.UserService;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Set;

@EnableWebMvc
@RestController
@RequestMapping("/")
class Controller {

    private final MessagingService messagingService;
    private final MessageService messageService;
    private final UserService userService;
    private final RoomService roomService;

    @Autowired
    public Controller(MessagingService messagingService, MessageService messageService, UserService userService, RoomService roomService) {
        this.messagingService = messagingService;
        this.messageService = messageService;
        this.userService = userService;
        this.roomService = roomService;
    }


    @PostMapping(value = "/salute", produces = "application/json; charset=UTF-8")
    public @ResponseBody String echo(@RequestParam(name = "name") String name,
                                     @RequestParam(name = "hash") String hash)
            throws UnsupportedEncodingException, NoSuchAlgorithmException {
        final String[] result = new String[]{"Unauthorized"};
        Ranks.getRank(name, hash).ifPresent(rank -> result[0] = "You are " + Ranks.getRankName(rank));
        return result[0];
    }

    @PostMapping(value = "/pleaseGeneral", produces = "application/json; charset=UTF-8")
    public @ResponseBody
    String getReceivedMessages() {
        return constructReceiversResponse(messagingService.getReceivers()).toJSONString();
    }

    @PostMapping(value = "/rooms", produces = "application/json; charset=UTF-8")
    public Map<String, Integer> getRooms() {
        return roomService.getMessagesCountInAllRooms();
    }

    private JSONArray constructReceiversResponse(Map<Message, Set<User>> receivers) {
        JSONArray result = new JSONArray();
        receivers.forEach((message, users) -> {
            JSONArray userNames = new JSONArray();
            users.stream().map(User::getName).forEach(userNames::add);

            JSONObject msgInfo = new JSONObject();
            msgInfo.put("id", message.getId());
            msgInfo.put("recipients", userNames);
            result.add(msgInfo);
        });
        return result;
    }
}


