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

package com.nebkat.junglist.bot.plugin;

/**
 * Thrown when attempting to load an invalid PluginDescriptionFile
 */
public class InvalidDescriptionException extends Exception {
    /**
     * Constructs a new InvalidDescriptionException based on the given Exception.
     *
     * @param message Brief message explaining the cause of the exception.
     * @param cause Exception that triggered this Exception.
     */
    public InvalidDescriptionException(final Throwable cause, final String message) {
        super(message, cause);
    }

    /**
     * Constructs a new InvalidDescriptionException based on the given Exception.
     *
     * @param cause Exception that triggered this Exception.
     */
    public InvalidDescriptionException(final Throwable cause) {
        super("Invalid plugin.yml", cause);
    }

    /**
     * Constructs a new InvalidDescriptionException with the given message.
     *
     * @param message Brief message explaining the cause of the exception.
     */
    public InvalidDescriptionException(final String message) {
        super(message);
    }

    /**
     * Constructs a new InvalidDescriptionException.
     */
    public InvalidDescriptionException() {
        super("Invalid plugin.yml");
    }
}