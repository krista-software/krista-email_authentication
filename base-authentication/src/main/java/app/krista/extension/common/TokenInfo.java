/*
 * Email Authentication Extension for Krista
 * Copyright (C) 2025 Krista Software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>. 
 */

package app.krista.extension.common;

public class TokenInfo {

    private final String accountId;
    private final String refreshToken;
    private final String accessToken;
    private final long refreshTokenExpiryTime;
    private final long accessTokenExpiryTime;
    private final String invokerId;

    public TokenInfo(String accountId, String accessToken, String refreshToken,
            long accessTokenExpiryTime,
            long refreshTokenExpiryTime, String invokerId) {
        this.accountId = accountId;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.accessTokenExpiryTime = accessTokenExpiryTime;
        this.refreshTokenExpiryTime = refreshTokenExpiryTime;
        this.invokerId = invokerId;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getInvokerId() {
        return invokerId;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public long getRefreshTokenExpiryTime() {
        return refreshTokenExpiryTime;
    }

    public long getAccessTokenExpiryTime() {
        return accessTokenExpiryTime;
    }

    @Override
    public String toString() {
        return "{" +
                "accountId='" + accountId + '\'' +
                ", accessToken='" + accessToken + '\'' +
                ", refreshToken='" + refreshToken + '\'' +
                ", accessTokenExpiryTime=" + accessTokenExpiryTime +
                ", refreshTokenExpiryTime=" + refreshTokenExpiryTime +
                ", invokerId='" + invokerId + '\'' +
                '}';
    }

}
