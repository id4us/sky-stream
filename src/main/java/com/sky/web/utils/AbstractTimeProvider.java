package com.sky.web.utils;

import org.joda.time.DateTime;

import java.util.Date;

public abstract class AbstractTimeProvider implements TimeProvider {

    @Override
    public Date getDate() {
        long timestamp = getTimeMillis();
        return new Date(timestamp);
    }

    @Override
    public DateTime getDateTime() {
        return new DateTime(getTimeMillis());
    }

}
