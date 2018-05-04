package sample.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import sample.configuration.AppConfig;
import sample.configuration.AppInitializer;
import sample.configuration.HibernateConfiguration;

import java.io.IOException;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebAppConfiguration
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {AppConfig.class, AppInitializer.class, HibernateConfiguration.class})
public class ControllerTest {
    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

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
                .param("hash", "E+pl1T31nObs76mdbZORgQ=="))
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
                .param("hash", "fYKrKtbVblnWFo/4EfVGmg=="))
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
}