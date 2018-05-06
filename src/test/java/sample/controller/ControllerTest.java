package sample.controller;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import sample.configuration.AppConfig;
import sample.configuration.AppInitializer;
import sample.configuration.HibernateConfiguration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebAppConfiguration
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {AppConfig.class, AppInitializer.class, HibernateConfiguration.class})
public class ControllerTest {

    @Autowired
    private WebApplicationContext context;
    @Autowired
    private WebSocketHandler handler;

    private MockMvc mvc;

    private static final String SIMON_HASH = "E+pl1T31nObs76mdbZORgQ==";
    private static final String VAVILEN_HASH = "fYKrKtbVblnWFo/4EfVGmg==";

    @Before
    public void setup() throws IOException {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .build();
    }

    @Test
    public void authorize() throws Exception {
        mvc.perform(post("/salute")
                .param("name", "Simon")
                .param("hash", SIMON_HASH))
                .andExpect(result -> {
                    assertEquals("You are Soldier", result.getResponse().getContentAsString());
                });
        mvc.perform(post("/salute")
                .param("name", "Peter")
                .param("hash", "iHDxWFurtv+PN6akU31KqQ=="))
                .andExpect(result -> {
                    assertEquals("You are Sergeant", result.getResponse().getContentAsString());
                });
        mvc.perform(post("/salute")
                .param("name", "Evlampiy")
                .param("hash", "ABXHq+PAFyH+73LS+DvPuw=="))
                .andExpect(result -> {
                    assertEquals("You are Lieutenant", result.getResponse().getContentAsString());
                });
        mvc.perform(post("/salute")
                .param("name", "Vavilen")
                .param("hash", VAVILEN_HASH))
                .andExpect(result -> {
                    assertEquals("You are General", result.getResponse().getContentAsString());
                });
    }

    @Test
    public void failAuthentication() throws Exception {
        mvc.perform(post("/salute")
                .param("name", "Vavilen")
                .param("hash", "aB8ge3tAVNTKzQM2gZB2=="))
                .andExpect(result -> {
                    assertEquals("Unauthorized", result.getResponse().getContentAsString());
                });
    }

    @Test
    public void testStat() throws Exception {
        final String SIMON_MSG1 = constructMessage(
                "Simon msg#1", false,"room0", "Simon", SIMON_HASH
        ).toJSONString();
        final String SIMON_MSG2 = constructMessage(
                "Simon msg#2", true,"room0", "Simon", SIMON_HASH
        ).toJSONString();
        final String VAVILEN_MSG1 = constructMessage(
                "Vavilen msg#1", false,"room0", "Vavilen", VAVILEN_HASH
        ).toJSONString();
        final String VAVILEN_MSG2 = constructMessage(
                "Vavilen msg#2", true,"room0", "Vavilen", VAVILEN_HASH
        ).toJSONString();
        final Map<Long, Integer> recipientsSizeByMessageId = new HashMap<>();
        recipientsSizeByMessageId.put(1L, 1);
        recipientsSizeByMessageId.put(2L, 1);
        recipientsSizeByMessageId.put(3L, 2);
        recipientsSizeByMessageId.put(4L, 2);
        recipientsSizeByMessageId.put(5L, 2);
        recipientsSizeByMessageId.put(6L, 1);

        handler.handleMessage(Mockito.mock(WebSocketSession.class), new TextMessage(SIMON_MSG1));
        handler.handleMessage(Mockito.mock(WebSocketSession.class), new TextMessage(VAVILEN_MSG1));
        handler.handleMessage(Mockito.mock(WebSocketSession.class), new TextMessage(SIMON_MSG2));
        handler.handleMessage(Mockito.mock(WebSocketSession.class), new TextMessage(VAVILEN_MSG2));

        mvc.perform(post("/rooms")
                .param("name", "Vavilen")
                .param("hash", VAVILEN_HASH))
                .andExpect(result -> {
                    JSONObject resultJSON =
                            (JSONObject) new JSONParser().parse(result.getResponse().getContentAsString());
                    assertEquals(1, resultJSON.size());
                    assertEquals(6L, resultJSON.get("room0")); // 2 subscriptions and 4 reports
                });
        mvc.perform(post("/pleaseGeneral")
                .param("name", "Vavilen")
                .param("hash", VAVILEN_HASH))
                .andExpect(result -> {
                    ((JSONArray) new JSONParser().parse(result.getResponse().getContentAsString())).forEach(obj -> {
                        JSONObject json = (JSONObject) obj;
                        int recipientsN = ((JSONArray) json.get("recipients")).size();
                        assertEquals((int) recipientsSizeByMessageId.get(((JSONObject) obj).get("id")), recipientsN);
                    });
                });
    }

    private JSONObject constructMessage(String text, boolean secret, String roomName, String username, String hash) {
        JSONObject msg = new JSONObject();
        msg.put("action", "report");
        msg.put("message", text);
        msg.put("room", roomName);
        msg.put("secret", secret);
        msg.put("name", username);
        msg.put("hash", hash);
        return msg;
    }
}