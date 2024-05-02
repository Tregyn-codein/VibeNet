package com.example.vibenet.service;

import com.example.vibenet.entity.Reaction;
import com.example.vibenet.repository.ReactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReactionService {

    private final ReactionRepository reactionRepository;

    @Autowired
    public ReactionService(ReactionRepository reactionRepository) {
        this.reactionRepository = reactionRepository;
    }

    public List<Reaction> findAllReactions() {
        return reactionRepository.findAll();
    }

    public Optional<Reaction> findReactionById(Long id) {
        return reactionRepository.findById(id);
    }

    public Reaction saveReaction(Reaction reaction) {
        return reactionRepository.save(reaction);
    }

    public void deleteReaction(Long id) {
        reactionRepository.deleteById(id);
    }

    // Дополнительные методы, связанные с бизнес-логикой реакций
}
