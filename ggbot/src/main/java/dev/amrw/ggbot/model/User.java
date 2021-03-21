package dev.amrw.ggbot.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.UUID;

/**
 * Entity holding information about a user.
 */
@Data
@Entity
@NoArgsConstructor
@Table(name = "USERS")
public class User {

    @Id
    @GeneratedValue
    @Column(name = "ID", unique = true, nullable = false, insertable = false, updatable = false)
    private UUID id;

    /** Discord-native user ID. */
    @NotBlank
    @Size(max = 20)
    @Column(name = "DISCORD_USER_ID", unique = true, nullable = false, updatable = false, length = 20)
    private String discordUserId;

    /** Discord username at the time of creating the entity. Note that it may change over time. */
    @NotBlank
    @Column(name = "DISCORD_USERNAME", nullable = false, columnDefinition = "TEXT")
    private String discordUsername;

    @OneToOne(mappedBy = "user")
    private UserCredit userCredit;
}
