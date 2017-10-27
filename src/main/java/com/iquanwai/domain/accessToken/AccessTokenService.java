package com.iquanwai.domain.accesstoken;


public interface AccessTokenService {
    String getAccessToken();

    String refreshAccessToken(boolean force);
}
