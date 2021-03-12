package app.service;

import app.controller.DefaultController;
import app.entity.Message;
import app.entity.User;
import app.exception.UserNotFoundException;
import app.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

@SpringBootTest
@Profile("test")
@ExtendWith(SpringExtension.class)
@Sql({"classpath:/data/messages.sql"})
public class DefaultControllerTest {

    //https://www.baeldung.com/mockito-annotations
    //https://www.baeldung.com/junit-5

    @Autowired
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @Mock
    private MessageService messageService;

    @InjectMocks
    private DefaultController defaultController;

    @Test
    public void testGetMessages() {
        // 1. init mock data
        ArrayList<Message> messages = new ArrayList<>();
        messages.add(new Message());
        messages.add(new Message());

        Mockito.doReturn(new ArrayList<Message>()).when(messageService).get(any());
        Mockito.doReturn(messages).when(messageService).get("main");

        // 2. test for empty channel
        ResponseEntity<List<Message>> r1 = defaultController.getMessages("some channel");
        assertFalse(r1.getStatusCode().isError());
        assertNotNull(r1.getBody());
        assertEquals(0, r1.getBody().size());

        // 3. test for channel with 2 messages
        ResponseEntity<List<Message>> r2 = defaultController.getMessages("main");
        assertFalse(r2.getStatusCode().isError());
        assertNotNull(r2.getBody());
        assertEquals(2, r2.getBody().size());
    }

    @Test
    public void testPostMessage() {
        // 1. init users
        Optional<User> oa = userRepository.findById(1L);
        Optional<User> ow = userRepository.findById(2L);

        assertTrue(oa.isPresent());
        assertTrue(ow.isPresent());

        User alice = oa.get();
        User wally = ow.get();

        // 2. init mock data
        try {
            Mockito
                    .doThrow(new UserNotFoundException())
                    .when(messageService)
                    .create(anyLong(), eq("some channel"), any());
            Mockito
                    .doReturn(new Message(wally, "some channel", "msg w1"))
                    .when(messageService)
                    .create(eq(2L), eq("some channel"), any());
            Mockito
                    .doReturn(new Message(alice, "some channel", "msg a1"))
                    .when(messageService)
                    .create(alice.getId(), "some channel", "msg a1");
        } catch (UserNotFoundException e) {
            e.printStackTrace();
            fail();
        }

        // 3. test for message from alice
        ResponseEntity<Message> r1 = defaultController.postMessage("some channel", 1, "msg a1");
        assertFalse(r1.getStatusCode().isError());
        assertNotNull(r1.getBody());
        assertEquals("alice", r1.getBody().getSender().getName());
        assertEquals("msg a1", r1.getBody().getText());

        // 4. test for message from wally
        ResponseEntity<Message> r2 = defaultController.postMessage("some channel", 2, "not msg w1");
        assertFalse(r2.getStatusCode().isError());
        assertNotNull(r2.getBody());
        assertEquals("wally", r2.getBody().getSender().getName());
        assertEquals("msg w1", r2.getBody().getText());

        // 5. test for missing user
        ResponseEntity<Message> r3 = defaultController.postMessage("some channel", 17, "msg d1");
        assertTrue(r3.getStatusCode().isError());
        assertNull(r3.getBody());
    }

    @Test
    public void testPostUser() {
        // 1. init mock data
        Mockito.doReturn(new User("dogbert")).when(userService).create(any());
        Mockito.doReturn(new User("dilbert")).when(userService).create("dilbert");

        // 2. create dilbert
        ResponseEntity<User> r1 = defaultController.postUser("dilbert");
        assertFalse(r1.getStatusCode().isError());
        assertNotNull(r1.getBody());
        assertEquals("dilbert", r1.getBody().getName());

        // 3. create dogbert by trying to create catbert (due to mock service)
        ResponseEntity<User> r2 = defaultController.postUser("catbert");
        assertFalse(r2.getStatusCode().isError());
        assertNotNull(r2.getBody());
        assertEquals("dogbert", r2.getBody().getName());
    }
}
