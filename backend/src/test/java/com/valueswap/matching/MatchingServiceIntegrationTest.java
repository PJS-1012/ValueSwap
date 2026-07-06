package com.valueswap.matching;

import com.valueswap.notification.NotificationRepository;
import com.valueswap.common.exception.ApiException;
import com.valueswap.post.ExchangePostRepository;
import com.valueswap.post.domain.Category;
import com.valueswap.post.domain.ExchangePost;
import com.valueswap.post.domain.ProvideItem;
import com.valueswap.post.domain.WantItem;
import com.valueswap.post.domain.PostStatus;
import com.valueswap.matching.domain.AcceptStatus;
import com.valueswap.matching.domain.MatchStatus;
import com.valueswap.user.User;
import com.valueswap.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Comparator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MatchingServiceIntegrationTest {
    @Autowired
    private MatchingService matchingService;

    @Autowired
    private MatchCandidateRepository candidateRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExchangePostRepository postRepository;

    @Test
    void runIsIdempotentAndNotifiesEveryParticipantOnce() {
        createThreePartyScenario();

        var first = matchingService.runMatching();
        var second = matchingService.runMatching();

        assertThat(first.scannedPosts()).isEqualTo(3);
        assertThat(first.discovered()).isEqualTo(1);
        assertThat(first.created()).isEqualTo(1);
        assertThat(second.created()).isZero();
        assertThat(candidateRepository.count()).isEqualTo(1);
        assertThat(notificationRepository.count()).isEqualTo(3);

        var candidate = candidateRepository.findAll().get(0);
        assertThat(candidate.getParticipants()).hasSize(3);
        assertThat(candidate.getEdges()).hasSize(3);
        assertThat(candidate.getCycleKey()).startsWith("THREE_PARTY:");
    }

    @Test
    void myMatchesAreReturnedByScoreThenCreatedAtDescending() {
        Scenario scenario = createThreePartyScenario();
        User cleaner = saveUser("cleaner@match.test", "생활용품사용자");
        savePost(scenario.restaurant(), "식당의 두 번째 교환 글",
                provide(Category.ELECTRONICS, "스마트폰", "스마트폰", 300_000, "스마트폰"),
                want(Category.DAILY_GOODS, "생활용품", "세제", 20_000, 40_000, "세제"));
        savePost(cleaner, "생활용품 교환 글",
                provide(Category.DAILY_GOODS, "생활용품", "세제", 30_000, "세제"),
                want(Category.ELECTRONICS, "스마트폰", "스마트폰", 250_000, 350_000, "스마트폰"));
        matchingService.runMatching();

        var matches = matchingService.findMine(scenario.restaurant().getId());

        assertThat(matches).hasSize(2);
        assertThat(matches).isSortedAccordingTo(
                Comparator.comparingInt(com.valueswap.matching.dto.MatchSummaryResponse::score).reversed());
        assertThat(matches).allSatisfy(match -> {
            assertThat(match.participants()).hasSizeBetween(2, 3);
            assertThat(match.edges()).hasSize(match.participants().size());
        });
    }

    @Test
    void allParticipantsAcceptingStartsExchangeForEveryPost() {
        Scenario scenario = createThreePartyScenario();
        matchingService.runMatching();
        Long matchId = candidateRepository.findAll().get(0).getId();

        matchingService.accept(matchId, scenario.restaurant().getId());
        matchingService.accept(matchId, scenario.farmer().getId());
        var result = matchingService.accept(matchId, scenario.designer().getId());

        assertThat(result.match().status()).isEqualTo(MatchStatus.ACCEPTED);
        assertThat(candidateRepository.findById(matchId).orElseThrow().getParticipants())
                .allSatisfy(participant -> assertThat(participant.getAcceptStatus()).isEqualTo(AcceptStatus.ACCEPTED));
        assertThat(postRepository.findAll()).allSatisfy(post -> assertThat(post.getStatus()).isEqualTo(PostStatus.IN_EXCHANGE));
    }

    @Test
    void oneRejectionClosesCandidate() {
        Scenario scenario = createThreePartyScenario();
        matchingService.runMatching();
        Long matchId = candidateRepository.findAll().get(0).getId();

        var result = matchingService.reject(matchId, scenario.farmer().getId());

        assertThat(result.match().status()).isEqualTo(MatchStatus.REJECTED);
    }

    @Test
    void aPostAlreadyInExchangeCannotAcceptAnotherCandidate() {
        Scenario scenario = createThreePartyScenario();
        User cleaner = saveUser("cleaner-conflict@match.test", "생활용품사용자");
        User anotherCleaner = saveUser("cleaner-conflict-2@match.test", "두번째생활용품사용자");
        savePost(scenario.restaurant(), "식당의 두 번째 교환 글",
                provide(Category.ELECTRONICS, "스마트폰", "스마트폰", 300_000, "스마트폰"),
                want(Category.DAILY_GOODS, "생활용품", "세제", 20_000, 40_000, "세제"));
        savePost(cleaner, "생활용품 교환 글",
                provide(Category.DAILY_GOODS, "생활용품", "세제", 30_000, "세제"),
                want(Category.ELECTRONICS, "스마트폰", "스마트폰", 250_000, 350_000, "스마트폰"));
        savePost(anotherCleaner, "두 번째 생활용품 교환 글",
                provide(Category.DAILY_GOODS, "생활용품", "세제", 30_000, "세제"),
                want(Category.ELECTRONICS, "스마트폰", "스마트폰", 250_000, 350_000, "스마트폰"));
        matchingService.runMatching();

        var candidates = candidateRepository.findAll();
        var twoPartyCandidates = candidates.stream().filter(candidate -> candidate.getParticipants().size() == 2).toList();
        var first = twoPartyCandidates.get(0);
        var second = twoPartyCandidates.get(1);
        first.getParticipants().forEach(participant -> matchingService.accept(first.getId(), participant.getUser().getId()));

        assertThatThrownBy(() -> matchingService.accept(second.getId(), scenario.restaurant().getId()))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("다른 거래가 진행 중");
    }

    private Scenario createThreePartyScenario() {
        User restaurant = saveUser("restaurant@match.test", "식당");
        User farmer = saveUser("farmer@match.test", "농가");
        User designer = saveUser("designer@match.test", "디자이너");

        savePost(restaurant, "식당 교환 글",
                provide(Category.FOOD, "식사권", "돈까스 정식 식사권", 50_000, "식사권", "한식"),
                want(Category.FOOD_MATERIAL, "쌀", "쌀", 40_000, 60_000, "쌀", "식재료"));
        savePost(farmer, "농가 교환 글",
                provide(Category.FOOD_MATERIAL, "쌀", "쌀 10kg", 50_000, "쌀", "식재료"),
                want(Category.DESIGN, "메뉴판디자인", "메뉴판 디자인", 40_000, 80_000, "메뉴판", "디자인"));
        savePost(designer, "디자이너 교환 글",
                provide(Category.DESIGN, "메뉴판디자인", "식당 메뉴판 디자인", 70_000, "메뉴판", "디자인"),
                want(Category.FOOD, "식사권", "식사권", 30_000, 80_000, "식사권", "한식"));
        return new Scenario(restaurant, farmer, designer);
    }

    private User saveUser(String email, String nickname) {
        return userRepository.save(User.create(email, "encoded", nickname, nickname));
    }

    private ExchangePost savePost(User user, String title, ProvideItem provide, WantItem want) {
        ExchangePost post = ExchangePost.create(user, title, "설명", "광주");
        post.addProvideItem(provide);
        post.addWantItem(want);
        return postRepository.save(post);
    }

    private ProvideItem provide(Category category, String subCategory, String name, long value, String... tags) {
        return ProvideItem.create(category, subCategory, name, null, 1, value, List.of(tags));
    }

    private WantItem want(Category category, String subCategory, String name,
                          long minValue, long maxValue, String... tags) {
        return WantItem.create(category, subCategory, name, null, 1, minValue, maxValue, List.of(tags));
    }

    private record Scenario(User restaurant, User farmer, User designer) {
    }
}
