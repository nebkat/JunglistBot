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

package com.nebkat.junglist.bot;

import java.util.List;

@SuppressWarnings("unused")
public final class Configuration {
    public static class BotConfiguration {
        private String levels;
        private String plugins;
        private String prefix;

        public String getLevels() {
            return levels;
        }

        public String getPlugins() {
            return plugins;
        }

        public String getPrefix() {
            return prefix;
        }
    }

    public static class IrcConfiguration {
        public static class ServerConfiguration {
            private String name;
            private int port = -1;
            private String pass;

            public String getName() {
                return name;
            }

            public int getPort() {
                return port;
            }

            public String getPass() {
                return pass;
            }
        }
        public static class AuthConfiguration {
            public enum AuthService {
                NickServ
            }
            
            private AuthService service;
            private String user;
            private String pass;
            private boolean delayJoin;

            public AuthService getService() {
                return service;
            }

            public String getUser() {
                return user;
            }

            public String getPass() {
                return pass;
            }

            public boolean getDelayJoin() {
                return delayJoin;
            }
        }

        private String user;
        private List<String> nicks;
        private List<String> joins;
        private ServerConfiguration server;
        private AuthConfiguration auth;


        public String getUser() {
            return user;
        }

        public List<String> getNicks() {
            return nicks;
        }

        public List<String> getJoins() {
            return joins;
        }

        public ServerConfiguration getServerConfiguration() {
            return server;
        }

        public AuthConfiguration getAuthConfiguration() {
            return auth;
        }
    }

    private BotConfiguration bot;
    private IrcConfiguration irc;

    /**
     * @return Bot configuration.
     */
    public BotConfiguration getBotConfiguration() {
        return bot;
    }

    /**
     * @return Irc settings.
     */
    public IrcConfiguration getIrcConfiguration() {
        return irc;
    }
}
