package com.example.weatherforcastapp.model.api;

public final class LocationDto {

    private String name;
    private String country;
    private double lat;
    private double lon;


    public LocationDto(
            String name,
            String country,
            double lat,
            double lon
    ){
        this.name = name;
        this.country = country;
        this.lat = lat;
        this.lon = lon;
    }


    public String getName(){
        return name;
    }


    public String getCountry(){
        return country;
    }


    public double getLat(){
        return lat;
    }


    public double getLon(){
        return lon;
    }


    public String getRegion(){
        return "";
    }
}