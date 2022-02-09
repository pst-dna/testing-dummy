package app.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "message")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "sender", nullable = false)
    private User sender;

    @Column(nullable = false)
    private String channel;

    @Column(nullable = false, length = 512)
    private String text;

    @Column(nullable = false)
    private LocalDateTime date;

    public Message(User user, String channel, String text) {
        this(0, user, channel, text, LocalDateTime.now());
    }
}
