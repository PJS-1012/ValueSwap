package com.valueswap.config;

import com.valueswap.post.ExchangePostRepository;
import com.valueswap.post.domain.Category;
import com.valueswap.post.domain.ExchangePost;
import com.valueswap.post.domain.ProvideItem;
import com.valueswap.post.domain.WantItem;
import com.valueswap.user.User;
import com.valueswap.user.UserRepository;
import com.valueswap.user.UserRole;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Profile("local")
@Transactional
public class SeedDataInitializer implements CommandLineRunner {
    public static final String SEED_PASSWORD = "Password1!";

    private final UserRepository userRepository;
    private final ExchangePostRepository postRepository;
    private final PasswordEncoder passwordEncoder;

    public SeedDataInitializer(UserRepository userRepository, ExchangePostRepository postRepository,
                               PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seed();
    }

    public void seed() {
        ensureUser("admin@valueswap.local", "관리자", "관리자", UserRole.ADMIN);
        User restaurant = ensureUser("restaurant@valueswap.local", "식당 사장", "식당", UserRole.BUSINESS);
        User farmer = ensureUser("farmer@valueswap.local", "농가 사용자", "농가", UserRole.USER);
        User designer = ensureUser("designer@valueswap.local", "디자이너", "디자이너", UserRole.USER);
        User burger = ensureUser("burger@valueswap.local", "햄버거 사장", "햄버거가게", UserRole.BUSINESS);
        User general = ensureUser("user@valueswap.local", "일반 사용자", "일반사용자", UserRole.USER);

        ensurePost(restaurant, "식사권과 쌀을 교환하고 싶습니다",
                provide(Category.FOOD, "식사권", "돈까스 정식 식사권", 50_000,
                        "돈까스", "식사권", "한식", "식사"),
                want(Category.FOOD_MATERIAL, "쌀", "쌀", 40_000, 60_000,
                        "쌀", "식재료", "10kg"));
        ensurePost(farmer, "쌀과 메뉴판 디자인을 교환합니다",
                provide(Category.FOOD_MATERIAL, "쌀", "쌀 10kg", 50_000,
                        "쌀", "식재료", "10kg"),
                want(Category.DESIGN, "메뉴판디자인", "메뉴판 디자인", 40_000, 80_000,
                        "메뉴판", "디자인", "홍보물"));
        ensurePost(designer, "메뉴판 디자인과 식사권을 교환합니다",
                provide(Category.DESIGN, "메뉴판디자인", "식당 메뉴판 디자인", 70_000,
                        "메뉴판", "디자인", "홍보물"),
                want(Category.FOOD, "식사권", "식사권", 30_000, 80_000,
                        "식사권", "음식", "한식"));
        ensurePost(burger, "햄버거 세트와 생활용품을 교환합니다",
                provide(Category.FOOD, "햄버거", "불고기 햄버거 세트", 30_000,
                        "햄버거", "버거", "불고기버거", "세트", "식사권"),
                want(Category.DAILY_GOODS, "생활용품", "세제", 20_000, 40_000,
                        "세제", "생활용품"));
        ensurePost(general, "세제 세트와 햄버거를 교환합니다",
                provide(Category.DAILY_GOODS, "생활용품", "세제 세트", 25_000,
                        "세제", "생활용품", "세탁"),
                want(Category.FOOD, "햄버거", "햄버거", 20_000, 40_000,
                        "햄버거", "버거"));
    }

    private User ensureUser(String email, String name, String nickname, UserRole role) {
        return userRepository.findByEmail(email).orElseGet(() -> userRepository.save(
                User.createWithRole(email, passwordEncoder.encode(SEED_PASSWORD), name, nickname, role)));
    }

    private void ensurePost(User user, String title, ProvideItem provide, WantItem want) {
        if (postRepository.existsByUserIdAndTitle(user.getId(), title)) {
            return;
        }
        ExchangePost post = ExchangePost.create(user, title, "ValueSwap 테스트용 교환 글입니다.", "광주");
        post.addProvideItem(provide);
        post.addWantItem(want);
        postRepository.save(post);
    }

    private ProvideItem provide(Category category, String subCategory, String name,
                                long value, String... tags) {
        return ProvideItem.create(category, subCategory, name, null, 1, value, List.of(tags));
    }

    private WantItem want(Category category, String subCategory, String name,
                          long minValue, long maxValue, String... tags) {
        return WantItem.create(category, subCategory, name, null, 1, minValue, maxValue, List.of(tags));
    }
}
