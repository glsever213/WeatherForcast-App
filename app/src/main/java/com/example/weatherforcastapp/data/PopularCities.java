package com.example.weatherforcastapp.data;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class PopularCities {

    private PopularCities() {
    }

    public static List<PopularCity> all() {
        return Collections.unmodifiableList(Arrays.asList(
                new PopularCity("Định vị", 0, 0, true),
                new PopularCity("Hà Nội", 21.0285, 105.8542, true),
                new PopularCity("Buôn Ma Thuột", 12.6667, 108.0500, false),
                new PopularCity("Cẩm Phả", 21.0103, 107.2425, false),
                new PopularCity("Đà Lạt", 11.9404, 108.4583, false),
                new PopularCity("Đà Nẵng", 16.0544, 108.2022, false),
                new PopularCity("Hải Phòng", 20.8449, 106.6881, false),
                new PopularCity("Huế", 16.4637, 107.5909, false),
                new PopularCity("TP.HCM", 10.8231, 106.6297, false),
                new PopularCity("Nha Trang", 12.2388, 109.1967, false),
                new PopularCity("Cần Thơ", 10.0452, 105.7469, false)
        ));
    }
}
