package app.service;

import app.entity.User;
import app.exception.UserNotFoundException;
import app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    public User create(String name) {
        throw new RuntimeException();
        /*User user = new User(name);
        return userRepository.save(user);*/
    }

    public void delete(long id) throws UserNotFoundException {
        if (userRepository.findById(id).isPresent())
            userRepository.deleteById(id);
        else
            throw new UserNotFoundException(String.format("User %d was not found", id));
    }
}
