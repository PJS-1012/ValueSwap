package com.valueswap.post.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "want_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WantItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exchange_post_id", nullable = false)
    private ExchangePost exchangePost;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private Category category;

    @Column(length = 100)
    private String subCategory;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private Integer quantity;

    @Column
    private Long minValue;

    @Column
    private Long maxValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ValuePolicy valuePolicy;

    @ElementCollection
    @CollectionTable(name = "want_item_tags", joinColumns = @JoinColumn(name = "want_item_id"))
    @OrderColumn(name = "tag_order")
    @Column(name = "tag", nullable = false, length = 100)
    private final List<String> tags = new ArrayList<>();

    private WantItem(Category category, String subCategory, String name, String description,
                     Integer quantity, Long minValue, Long maxValue, List<String> tags) {
        this.category = category;
        this.subCategory = subCategory;
        this.name = name;
        this.description = description;
        this.quantity = quantity;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.valuePolicy = ValuePolicy.DIRECT;
        this.tags.addAll(tags);
    }

    public static WantItem create(Category category, String subCategory, String name,
                                  String description, Integer quantity, Long minValue,
                                  Long maxValue, List<String> tags) {
        return new WantItem(category, subCategory, name, description, quantity, minValue, maxValue, tags);
    }

    public static WantItem create(Category category, String subCategory, String name,
                                  String description, Integer quantity, Long minValue, Long maxValue,
                                  ValuePolicy valuePolicy, List<String> tags) {
        WantItem item = new WantItem(category, subCategory, name, description, quantity, minValue, maxValue, tags);
        item.valuePolicy = valuePolicy;
        return item;
    }

    void attachTo(ExchangePost exchangePost) {
        this.exchangePost = exchangePost;
    }
}
