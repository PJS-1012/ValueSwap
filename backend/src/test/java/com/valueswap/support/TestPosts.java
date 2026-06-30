package com.valueswap.support;

public final class TestPosts {
    private TestPosts() {
    }

    public static String restaurantJson() {
        return """
                {
                  "title":"식사권과 쌀을 교환하고 싶습니다",
                  "description":"돈까스 정식 5인분 제공 가능합니다.",
                  "region":"광주",
                  "provideItems":[{
                    "category":"FOOD","subCategory":"식사권","name":"돈까스 정식 식사권",
                    "description":"1인분 식사권 5장","quantity":5,"estimatedValue":50000,
                    "tags":["돈까스","식사권","한식","식사"]
                  }],
                  "wantItems":[{
                    "category":"FOOD_MATERIAL","subCategory":"쌀","name":"쌀",
                    "description":"10kg 이상","quantity":1,"minValue":40000,"maxValue":60000,
                    "tags":["쌀","식재료","10kg"]
                  }]
                }
                """;
    }

    public static String updatedJson() {
        return restaurantJson().replace("식사권과 쌀을 교환하고 싶습니다", "수정된 교환 글")
                .replace("돈까스 정식 5인분 제공 가능합니다.", "수정된 설명입니다.");
    }

    public static String invalidValueRangeJson() {
        return restaurantJson().replace("\"minValue\":40000,\"maxValue\":60000",
                "\"minValue\":70000,\"maxValue\":60000");
    }
}
