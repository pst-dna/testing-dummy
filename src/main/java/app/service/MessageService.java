package app.service;

import app.entity.Message;
import app.entity.User;
import app.exception.UserNotFoundException;
import app.repository.MessageRepository;
import app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    public Message create(User user, String channel, String text) {
        Message m = new Message(user, channel, text);
        return messageRepository.save(m);
    }

    public Message create(long userId, String channel, String text) throws UserNotFoundException {
        return create(userRepository.findById(userId).orElseThrow(UserNotFoundException::new), channel, text);
    }

    public List<Message> get(String channel) {
        return messageRepository.findAllByChannel(channel);
    }
}
