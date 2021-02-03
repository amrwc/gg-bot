package dev.amrw.ggbot.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.util.Date;
import java.util.UUID;

/**
 * Entity holding information about user credits.
 */
@Data
@Entity
@NoArgsConstructor
@Table(name = "USER_CREDITS")
public class UserCredit {

    @Id
    @GeneratedValue
    @Column(name = "ID", unique = true, nullable = false, insertable = false, updatable = false)
    private UUID id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "USER_ID", referencedColumnName = "ID")
    private User user;

    @NotNull
    @PositiveOrZero
    @Column(name = "CREDITS", nullable = false)
    private Long credits;

    /** When the daily credits were successfully claimed last time. */
    @Column(name = "LAST_DAILY")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastDaily;
}
