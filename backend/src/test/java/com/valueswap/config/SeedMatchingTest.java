package com.valueswap.config;

import com.valueswap.matching.MatchCandidateRepository;
import com.valueswap.matching.MatchingService;
import com.valueswap.matching.domain.MatchType;
import com.valueswap.post.ExchangePostRepository;
import com.valueswap.user.UserRepository;
import com.valueswap.user.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:seed-matching;MODE=MySQL;DATABASE_TO_LOWER=TRUE")
@Transactional
class SeedMatchingTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExchangePostRepository postRepository;

    @Autowired
    private MatchCandidateRepository candidateRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MatchingService matchingService;

    @Test
    void seedIsIdempotentAndProducesRequiredMatches() {
        SeedDataInitializer initializer = new SeedDataInitializer(userRepository, postRepository, passwordEncoder);

        initializer.seed();
        initializer.seed();
        matchingService.runMatching();

        assertThat(userRepository.count()).isEqualTo(6);
        assertThat(postRepository.count()).isEqualTo(5);
        assertThat(userRepository.findByEmail("admin@valueswap.local").orElseThrow().getRole())
                .isEqualTo(UserRole.ADMIN);
        assertThat(userRepository.findByEmail("restaurant@valueswap.local").orElseThrow().getRole())
                .isEqualTo(UserRole.BUSINESS);
        assertThat(candidateRepository.findAll()).extracting(candidate -> candidate.getMatchType())
                .contains(MatchType.THREE_PARTY, MatchType.ONE_TO_ONE);
        assertThat(candidateRepository.findAll()).flatExtracting(candidate -> candidate.getEdges())
                .anySatisfy(edge -> assertThat(edge.getProvideItem().getName()).isEqualTo("불고기 햄버거 세트"));
    }
}
