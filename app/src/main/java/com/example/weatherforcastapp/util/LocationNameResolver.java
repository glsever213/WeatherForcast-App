package com.example.weatherforcastapp.util;

import android.content.Context;
import android.location.Geocoder;
import android.location.Address;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

//Chuyển đổi tên gửi từ api thành tiếng việt
public class LocationNameResolver {
    private LocationNameResolver() {}

    public static String resolve(Context context, double lat, double lon, String fallback) {
        if (!Geocoder.isPresent()) {
            return fallback;
        }

        try {

            Geocoder geocoder = new Geocoder(context, new Locale("vi", "VN"));

            List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);

            if (addresses == null || addresses.isEmpty()) {
                return fallback;
            }

            Address address = addresses.get(0);

            if (address.getAdminArea() != null
                    && !address.getAdminArea().isEmpty()) {
                return address.getAdminArea();
            }

            if (address.getSubAdminArea() != null
                    && !address.getSubAdminArea().isEmpty()) {
                return address.getSubAdminArea();
            }

            if (address.getLocality() != null
                    && !address.getLocality().isEmpty()) {
                return address.getLocality();
            }

        } catch (IOException ignored) {
        }

        return fallback;
    }
}
