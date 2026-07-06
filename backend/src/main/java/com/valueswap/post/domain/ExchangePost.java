package com.valueswap.post.domain;

import com.valueswap.user.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "exchange_posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExchangePost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PostStatus status;

    @Column(nullable = false, length = 100)
    private String region;

    @OneToMany(mappedBy = "exchangePost", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<ProvideItem> provideItems = new ArrayList<>();

    @OneToMany(mappedBy = "exchangePost", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<WantItem> wantItems = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private ExchangePost(User user, String title, String description, String region) {
        this.user = user;
        this.title = title;
        this.description = description;
        this.region = region;
        this.status = PostStatus.ACTIVE;
    }

    public static ExchangePost create(User user, String title, String description, String region) {
        return new ExchangePost(user, title, description, region);
    }

    public void replaceDetails(String title, String description, String region,
                               List<ProvideItem> provides, List<WantItem> wants) {
        this.title = title;
        this.description = description;
        this.region = region;
        this.provideItems.clear();
        this.wantItems.clear();
        provides.forEach(this::addProvideItem);
        wants.forEach(this::addWantItem);
    }

    public void addProvideItem(ProvideItem item) {
        item.attachTo(this);
        provideItems.add(item);
    }

    public void addWantItem(WantItem item) {
        item.attachTo(this);
        wantItems.add(item);
    }

    public void cancel() {
        this.status = PostStatus.CANCELED;
    }

    public void startExchange() { this.status = PostStatus.IN_EXCHANGE; }
}
