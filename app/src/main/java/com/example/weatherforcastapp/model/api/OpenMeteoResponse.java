package com.example.weatherforcastapp.model.api;

import java.util.List;

public final class OpenMeteoResponse {
    private List<Result> results;
    public List<Result> getResults(){ return results; }
    public static final class Result {
        private String name;
        private String country;
        private double latitude;
        private double longitude;
        public String getName(){return name;}
        public String getCountry(){return country;}
        public double getLatitude(){return latitude;}
        public double getLongitude(){return longitude;}
    }
}
