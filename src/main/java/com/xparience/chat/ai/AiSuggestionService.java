package com.xparience.chat.ai;

import com.xparience.chat.ai.dto.AiSuggestionResponse;
import com.xparience.matching.MatchRepository;
import com.xparience.profile.Profile;
import com.xparience.profile.ProfileRepository;
import com.xparience.user.User;
import com.xparience.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AiSuggestionService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final MatchRepository matchRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<AiSuggestionResponse> getPhysicalDateSuggestions(Long matchedUserId) {
        User currentUser = getCurrentUser();
        Profile myProfile = profileRepository.findByUserId(currentUser.getId()).orElse(null);
        Profile theirProfile = profileRepository.findByUserId(matchedUserId).orElse(null);

        // In production: use an AI/LLM to generate personalised suggestions
        // based on both profiles' preferences, diet, and interests.
        // Here we return curated mock suggestions:
        AiSuggestionResponse s1 = new AiSuggestionResponse();
        s1.setCategory("MOVIE_DATE");
        s1.setTitle("Movie Date 🎬");
        s1.setDescription("Watch a movie together online");
        s1.setTags(List.of("Virtual", "Cosy"));

        AiSuggestionResponse s2 = new AiSuggestionResponse();
        s2.setCategory("ART_EXHIBITION");
        s2.setTitle("Art Exhibition");
        s2.setDescription("Explore a virtual or local art exhibition");
        s2.setTags(List.of("Cultural", "Creative"));

        AiSuggestionResponse s3 = new AiSuggestionResponse();
        s3.setCategory("MINI_GOLF");
        s3.setTitle("Mini Golf Date");
        s3.setDescription("A fun and casual mini golf outing");
        s3.setTags(List.of("Active", "Fun"));

        AiSuggestionResponse s4 = new AiSuggestionResponse();
        s4.setCategory("RESTAURANT");
        s4.setTitle("Dinner Date");
        s4.setDescription("Share a meal at a restaurant you both love");
        s4.setTags(List.of("Romantic", "Food"));

        return List.of(s1, s2, s3, s4);
    }

    public List<AiSuggestionResponse> getVirtualDateOptions() {
        AiSuggestionResponse s1 = new AiSuggestionResponse();
        s1.setCategory("MOVIE_DATE");
        s1.setTitle("See a movie together");
        s1.setDescription("Stream a movie simultaneously on your chosen platform");

        AiSuggestionResponse s2 = new AiSuggestionResponse();
        s2.setCategory("RESTAURANT_DATE");
        s2.setTitle("Be at the restaurant together");
        s2.setDescription("Order from the same restaurant and eat together virtually");

        AiSuggestionResponse s3 = new AiSuggestionResponse();
        s3.setCategory("GAME_DATE");
        s3.setTitle("Play a game together");
        s3.setDescription("Compete or cooperate in an online game");

        AiSuggestionResponse s4 = new AiSuggestionResponse();
        s4.setCategory("BLOG_TOGETHER");
        s4.setTitle("Blog together");
        s4.setDescription("Co-write a blog post or creative piece");

        return List.of(s1, s2, s3, s4);
    }
}