package me.rename.renameme.service;

import me.rename.renameme.model.WelcomeMessage;
import me.rename.renameme.repository.WelcomeMessageRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

@Service
public class WelcomeMessageService {

    private final WelcomeMessageRepository repository;

    public WelcomeMessageService(final WelcomeMessageRepository repository) {
        this.repository = repository;
    }

    public Optional<WelcomeMessage> findWelcomeMessageById(final Long id) {
        return repository.findById(id);
    }

    @Transactional
    public WelcomeMessage createWelcomeMessage(final String content) {
        return repository.save(new WelcomeMessage(content));
    }
}
