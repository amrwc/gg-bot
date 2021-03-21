package dev.amrw.ggbot.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.UUID;

/**
 * Entity holding a configuration property.
 */
@Data
@Entity
@NoArgsConstructor
@Table(name = "CONFIG")
public class Config {

    @Id
    @GeneratedValue
    @Column(name = "ID", unique = true, nullable = false, insertable = false, updatable = false)
    private UUID id;

    @NotBlank
    @Column(name = "NAME", unique = true, nullable = false, updatable = false)
    private String name;

    @NotNull
    @Column(name = "VALUE", nullable = false, columnDefinition = "TEXT")
    private String value;
}
