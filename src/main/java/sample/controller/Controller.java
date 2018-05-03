package sample.controller;

import com.jcabi.aspects.Loggable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import sample.Ranks;
import sample.service.MessagingService;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

@EnableWebMvc
@RestController
@RequestMapping("/")
class Controller {
    private final static Logger logger = LoggerFactory.getLogger(Controller.class);

    @Loggable
    @RequestMapping(value="/salute", method=RequestMethod.POST, produces = "application/json; charset=UTF-8")
    public @ResponseBody String echo(@RequestParam(name = "name") String name,
                                     @RequestParam(name = "hash") String hash)
            throws UnsupportedEncodingException, NoSuchAlgorithmException {
        final String[] result = new String[]{"Unauthorized"};
        Ranks.getRank(name, hash).ifPresent(rank -> result[0] = "You are " + Ranks.getRankName(rank));
        return result[0];
    }

    @Loggable
    @RequestMapping(value="/pleaseGeneral", method=RequestMethod.POST, produces = "application/json; charset=UTF-8")
    public @ResponseBody String report(@RequestParam(name = "hash") String hash,
                                       @RequestParam(name = "name") String name,
                                       @RequestParam(name = "room") String room) {
        return "TODO";
    }
}


