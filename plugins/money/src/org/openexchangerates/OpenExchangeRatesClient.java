/*
 * Copyright 2013 Nebojsa Cvetkovic. All rights reserved.
 *
 * This file is part of JunglistIRC.
 *
 * JunglistIRC is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JunglistIRC is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JunglistIRC.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.openexchangerates;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import com.nebkat.junglist.bot.http.ConnectionManager;
import org.openexchangerates.exceptions.UnavailableExchangeRateException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Implements Json for Open Exchange Rates(http://openexchangerates.org)
 *
 * @author Demétrio Menezes Neto
 */
public class OpenExchangeRatesClient {
    private final static String OER_URL = "http://openexchangerates.org/api/latest.json?app_id=%1$s";

    private final Gson mGson;
    private String mAppKey;

    public OpenExchangeRatesClient(String appKey) {
        mAppKey = appKey;

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Currency.class, new Currency.Deserializer());
        mGson = gsonBuilder.create();
    }

    /**
     * Get the latest exchange rates
     *
     * @return Last updated exchange rates
     */
    public Map<Currency, BigDecimal> getLatest() throws UnavailableExchangeRateException {

        String url = String.format(OER_URL, mAppKey);

        HttpGet get = new HttpGet(url);

        // Execute the request
        HttpContext context = new BasicHttpContext();
        HttpResponse response;
        try {
            response = ConnectionManager.getHttpClient().execute(get, context);
        } catch (IOException e) {
            throw new UnavailableExchangeRateException(e);
        }

        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            throw new UnavailableExchangeRateException("Could not fetch data");
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
            OERResult result = mGson.fromJson(reader, OERResult.class);
            return result.rates;
        } catch (Exception e) {
            throw new UnavailableExchangeRateException(e);
        }
    }

    public class OERResult {
        public String timestamp;
        public Currency base;
        public Map<Currency, BigDecimal> rates;
    }
}
