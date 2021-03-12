package app.repository;

import app.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query(value = "select * from message where channel = :channel order by date desc", nativeQuery = true)
    List<Message> findAllByChannel(String channel);
}
