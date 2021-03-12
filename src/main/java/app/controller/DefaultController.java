package app.controller;

import app.entity.Message;
import app.entity.User;
import app.exception.UserNotFoundException;
import app.service.MessageService;
import app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class DefaultController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @GetMapping("/messages/{channel}")
    public ResponseEntity<List<Message>> getMessages(@PathVariable String channel) {
        return new ResponseEntity<>(messageService.get(channel), HttpStatus.OK);
    }

    @PostMapping("/messages/{channel}")
    public ResponseEntity<Message> postMessage(@PathVariable String channel, @RequestParam long from, @RequestParam String text) {
        try {
            Message msg = messageService.create(from, channel, text);
            return new ResponseEntity<>(msg, HttpStatus.OK);
        } catch (UserNotFoundException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/users")
    public ResponseEntity<User> postUser(@RequestParam String name) {
        return new ResponseEntity<>(userService.create(name), HttpStatus.OK);
    }
}
