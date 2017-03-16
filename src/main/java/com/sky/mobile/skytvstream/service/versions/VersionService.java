package com.sky.mobile.skytvstream.service.versions;

public interface VersionService {
    boolean isAllowedVersion(String version);

    boolean isSlatedVersion(String version);
}
