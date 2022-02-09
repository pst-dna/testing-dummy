
package app.service;

import app.entity.Message;
import app.entity.User;
import app.exception.UserNotFoundException;
import app.repository.MessageRepository;
import app.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Profile("test")
@ExtendWith(SpringExtension.class)
public class MessageServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private MessageRepository messageRepository;

    @InjectMocks
    private MessageService messageService;

    @Test
    public void createMessageTest() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // 1. init message
        Message message = objectMapper.readValue(getClass().getClassLoader().getResource("data/json/message.json"), Message.class);

        // 2. test create messages with users
        Message msg = new Message(message.getSender(), message.getChannel(), message.getText());
        Mockito.doReturn(message).when(messageRepository).save(ArgumentMatchers.refEq(msg, "id", "date"));
        try (MockedStatic<LocalDateTime> dt = Mockito.mockStatic(LocalDateTime.class)) {
            dt.when(LocalDateTime::now).thenReturn(message.getDate());
            assertEquals(message, messageService.create(message.getSender(), message.getChannel(), message.getText()));
        }

        // 3. test create messages with user ids
        Mockito.doReturn(Optional.of(message.getSender())).when(userRepository).findById(message.getSender().getId());
        try {
            assertEquals(message, messageService.create(message.getSender().getId(), message.getChannel(), message.getText()));
        } catch (UserNotFoundException e) {
            e.printStackTrace();
            fail();
        }

        // 4. test create message for missing user
        assertThrows(UserNotFoundException.class, () -> messageService.create(3, "default", "msg3"));
    }

    @Test
    public void getMessagesTest() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        List<Message> messages = Arrays.asList(objectMapper.readValue(getClass().getClassLoader().getResource("data/json/wally-covers-for-alice.json"), Message[].class));
        Mockito.doReturn(messages).when(messageRepository).findAllByChannel("default");
        Mockito.doReturn(Collections.emptyList()).when(messageRepository).findAllByChannel("none");

        assertIterableEquals(messages, messageService.get("default"));
        assertEquals(0, messageService.get("none").size());
    }

    @Test
    public void getMessagesInOrderTest() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        List<Message> messages = Arrays.asList(objectMapper.readValue(getClass().getClassLoader().getResource("data/json/catbert-cubicle.json"), Message[].class));
        Mockito
                .doReturn(messages.stream().sequential().filter(m -> m.getChannel().equals("default")).collect(Collectors.toList()))
                .when(messageRepository)
                .findAllByChannel("default");
        Mockito
                .doReturn(messages.stream().sequential().filter(m -> m.getChannel().equals("management")).collect(Collectors.toList()))
                .when(messageRepository)
                .findAllByChannel("management");

        List<Message> messagesInDefault = messageService.get("default");
        for (int i = 1; i < messagesInDefault.size(); i++)
            assertTrue(messagesInDefault.get(i - 1).getDate().isBefore(messagesInDefault.get(i).getDate()));

        List<Message> messagesInManagement = messageService.get("management");
        for (int i = 1; i < messagesInManagement.size(); i++)
            assertFalse(messagesInManagement.get(i - 1).getDate().isBefore(messagesInManagement.get(i).getDate()));
    }
}
