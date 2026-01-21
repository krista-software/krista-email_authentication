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

import java.io.File;
import java.io.FileReader;
import com.opencsv.CSVReader;

public class CSVUtils {

    private final static String CSV_DIRECTORY_PATH = "/opt/krista/tokens/";

    public CSVUtils() {
    }

    private static String[] getArray(TokenInfo tokenInfo) {
        return new String[]{tokenInfo.getAccountId(),
                tokenInfo.getInvokerId(),
                tokenInfo.getRefreshToken(),
                tokenInfo.getAccessToken(),
                "" + tokenInfo.getRefreshTokenExpiryTime(),
                "" + tokenInfo.getAccessTokenExpiryTime()};
    }

    private static TokenInfo createToken(String[] tokenInfo) {
        if (tokenInfo == null || tokenInfo.length < 6) {
            throw new IllegalArgumentException("Invalid token information.");
        }
        return new TokenInfo(tokenInfo[0],
                tokenInfo[1],
                tokenInfo[2],
                Long.parseLong(tokenInfo[3]),
                Long.parseLong(tokenInfo[4]),
                tokenInfo[5]);
    }

    public TokenInfo readFromCSV(String fileName) {
        try {
            FileReader fileReader = new FileReader(CSV_DIRECTORY_PATH + fileName);
            CSVReader csvReader = new CSVReader(fileReader);
            String[] tokenInfo = csvReader.readNext();
            return createToken(tokenInfo);
        } catch (Exception cause) {
            throw new IllegalStateException("Failed to read from csv.", cause);
        }

    }

    public void deleteFromCSV(String fileName) {
        File file = new File(CSV_DIRECTORY_PATH + fileName);
        file.delete();
    }

}
