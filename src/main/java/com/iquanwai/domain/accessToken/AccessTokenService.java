package com.iquanwai.domain.accessToken;


public interface AccessTokenService {
    String getAccessToken();

    String refreshAccessToken(boolean force);
}
