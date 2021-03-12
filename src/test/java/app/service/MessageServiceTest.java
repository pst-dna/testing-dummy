
package app.service;


import app.entity.Message;
import app.entity.User;
import app.exception.UserNotFoundException;
import app.repository.MessageRepository;
import app.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Profile("test")
@ExtendWith(SpringExtension.class)
@Sql({"classpath:/data/messages.sql"})
public class MessageServiceTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    MessageService messageService;

    @Test
    public void createMessageTest() {
        // 1. init users
        Optional<User> oa = userRepository.findById(1L);
        Optional<User> ow = userRepository.findById(2L);

        assertTrue(oa.isPresent());
        assertTrue(ow.isPresent());

        User alice = oa.get();
        User wally = ow.get();

        // 2. test create messages with users
        assertEquals("msg a1", messageService.create(alice, "default", "msg a1").getText());
        assertEquals("wally", messageService.create(wally, "default", "msg w1").getSender().getName());

        // 3. test create messages with user ids
        try {
            assertEquals("msg a2", messageService.create(1, "default", "msg a2").getText());
            assertEquals("default", messageService.create(2, "default", "msg w2").getChannel());
        } catch (UserNotFoundException e) {
            e.printStackTrace();
            fail();
        }

        // 4. test create message for missing user
        assertThrows(UserNotFoundException.class, () -> messageService.create(3, "default", "msg3"));
        assertEquals(7, messageRepository.findAll().size());
    }

    @Test
    public void getMessagesTest() {
        // 1. test length of the list
        List<Message> messagesMain = messageService.get("main");
        assertEquals(3, messagesMain.size());

        // 2. test origin channel
        for (Message m : messagesMain)
            assertEquals("main", m.getChannel());

        // 3. test date sorting
        for (int i = 1; i < messagesMain.size(); i++)
            assertTrue(messagesMain.get(i).getDate().getTimeInMillis() >= messagesMain.get(i - 1).getDate().getTimeInMillis());

        // 4. test for empty channel
        assertEquals(0, messageService.get("none").size());
    }
}
