package com.sky.web.utils;

import org.joda.time.DateTime;

import java.util.Date;

public interface TimeProvider {

    long getTimeMillis();

    Date getDate();

    DateTime getDateTime();
}
