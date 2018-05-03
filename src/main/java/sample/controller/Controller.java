package sample.controller;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import sample.Ranks;
import sample.model.Message;
import sample.model.Room;
import sample.model.User;
import sample.service.MessagingService;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Set;

@EnableWebMvc
@RestController
@RequestMapping("/")
class Controller {
    @Autowired
    private MessagingService messagingService;

    @RequestMapping(value="/salute", method=RequestMethod.POST, produces = "application/json; charset=UTF-8")
    public @ResponseBody String echo(@RequestParam(name = "name") String name,
                                     @RequestParam(name = "hash") String hash)
            throws UnsupportedEncodingException, NoSuchAlgorithmException {
        final String[] result = new String[]{"Unauthorized"};
        Ranks.getRank(name, hash).ifPresent(rank -> result[0] = "You are " + Ranks.getRankName(rank));
        return result[0];
    }

    @RequestMapping(value="/pleaseGeneral", method=RequestMethod.POST, produces = "application/json; charset=UTF-8")
    public @ResponseBody String getReceivedMessages(@RequestParam(name = "hash") String hash,
                                       @RequestParam(name = "name") String name,
                                       @RequestParam(name = "room") String room) {
        return constructReceiversResponse(messagingService.getReceivers()).toJSONString();
    }

    @RequestMapping(value="/rooms", method=RequestMethod.POST, produces = "application/json; charset=UTF-8")
    public @ResponseBody String getRooms(@RequestParam(name = "hash") String hash,
                                       @RequestParam(name = "name") String name,
                                       @RequestParam(name = "room") String room) {
        return constructRoomsResponse(messagingService.getRoomMessagesNumber()).toJSONString();
    }

    private JSONObject constructRoomsResponse(Map<Room, Integer> numberByRoom) {
        JSONObject result = new JSONObject();
        numberByRoom.forEach((room, n) -> {
            result.put(room.getName(), n);
        });
        return result;
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


