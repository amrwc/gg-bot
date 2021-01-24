package me.rename.renameme.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@ToString
public class WelcomeMessageRequest {

    @NotBlank(message = "Welcome message content cannot be blank")
    private String content;
}
