package com.sky.web.utils;

import javax.annotation.Resource;

public class CountryImpl implements Country {

    private String country;

    @Override
    public String getCountry() {
        return country;
    }

    public CountryImpl(String country) {
        this.country = country;
    }

    @Override
    public void setCountry(String country) {
        this.country = country;
    }

}