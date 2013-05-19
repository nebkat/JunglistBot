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

package com.nebkat.junglist.bot.command;

import java.util.Arrays;
import java.util.Comparator;

public enum UserLevel {
    NORMAL(0),
    MODERATOR(1),
    ADMIN(2),
    OWNER(3);

    private final int level;

    private UserLevel(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public static UserLevel forLevel(int level) {
        return Arrays.stream(UserLevel.values())
                .filter((userLevel) -> userLevel.getLevel() == level)
                .findAny().orElse(UserLevel.NORMAL);
    }

    public static Comparator<UserLevel> COMPARATOR = (a, b) -> a.getLevel() - b.getLevel();
}
