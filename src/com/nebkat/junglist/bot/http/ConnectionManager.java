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

package com.nebkat.junglist.bot.http;

import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

public class ConnectionManager {
    private static HttpClient sHttpClient;

    public static final String REDIRECTED = "redirected";

    public static HttpClient getHttpClient() {
        if (sHttpClient == null) {
            SchemeRegistry schemeRegistry = new SchemeRegistry();
            schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
            schemeRegistry.register(new Scheme("https", 443, new EasySSLSocketFactory()));

            HttpParams params = new BasicHttpParams();
            params.setParameter(HttpProtocolParams.USE_EXPECT_CONTINUE, false);
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);

            PoolingClientConnectionManager connectionManager = new PoolingClientConnectionManager(schemeRegistry);
            connectionManager.setDefaultMaxPerRoute(30);
            connectionManager.setMaxTotal(30);

            sHttpClient = new DefaultHttpClient(connectionManager, params);

            ((DefaultHttpClient)sHttpClient).addResponseInterceptor((response, context) -> {
                if (response.containsHeader("Location")) {
                    context.setAttribute(REDIRECTED, true);
                }
            });
        }
        return sHttpClient;
    }
}
