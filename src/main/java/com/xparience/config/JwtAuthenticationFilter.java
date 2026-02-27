package com.xparience.config;

import com.xparience.common.ApiResponse;
import com.xparience.profile.Profile;
import com.xparience.profile.ProfileRepository;
import com.xparience.user.User;
import com.xparience.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final List<String> ONBOARDING_ALLOWED_PATH_PREFIXES = List.of(
            "/api/v1/profile",
            "/api/v1/verification",
            "/api/v1/settings",
            "/api/v1/auth",
            "/api/v1/subscription/stripe/webhook",
            "/ws",
            "/api-docs",
            "/swagger-ui",
            "/swagger-ui.html"
    );

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    public JwtAuthenticationFilter(JwtService jwtService,
                                    UserDetailsService userDetailsService,
                                    UserRepository userRepository,
                                    ProfileRepository profileRepository) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7);
        String userEmail = jwtService.extractUsername(jwt);

        if (userEmail != null
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
            if (jwtService.isTokenValid(jwt, userDetails)) {
                var authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);

                if (shouldBlockForIncompleteOnboarding(request, userEmail)) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json");
                    ApiResponse<Void> body = ApiResponse.error("Welcome back, continue setting up your profile");
                    response.getWriter().write("{\"success\":false,\"message\":\""
                            + body.getMessage().replace("\"", "\\\"")
                            + "\",\"data\":null}");
                    return;
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    private boolean shouldBlockForIncompleteOnboarding(HttpServletRequest request, String userEmail) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return false;
        }

        String requestPath = request.getRequestURI();
        boolean allowedPath = ONBOARDING_ALLOWED_PATH_PREFIXES.stream()
                .anyMatch(requestPath::startsWith);
        if (allowedPath) {
            return false;
        }

        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (user == null) {
            return false;
        }

        Profile profile = profileRepository.findByUserId(user.getId()).orElse(null);
        return !isProfileComplete(profile);
    }

    private boolean isProfileComplete(Profile profile) {
        return profile != null
                && profile.isBasicInfoComplete()
                && profile.isImagesComplete()
                && profile.isAboutYouComplete()
                && profile.isPreferencesComplete()
                && profile.isNonNegotiablesComplete()
                && profile.isNutritionVibeComplete()
                && profile.isPersonalityQuizComplete()
                && profile.isReviewSubmitted();
    }
}