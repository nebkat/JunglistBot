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
 * Thrown when attempting to load an invalid Plugin file
 */
public class InvalidPluginException extends Exception {
    /**
     * Constructs a new InvalidPluginException based on the given Exception.
     *
     * @param cause Exception that triggered this Exception.
     */
    public InvalidPluginException(final Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new InvalidPluginException.
     */
    public InvalidPluginException() {

    }

    /**
     * Constructs a new InvalidPluginException with the specified detail message and cause.
     *
     * @param message The detail message (which is saved for later retrieval by the getMessage() method).
     * @param cause The cause (which is saved for later retrieval by the getCause() method). (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public InvalidPluginException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new InvalidPluginException with the specified detail message
     *
     * @param message The detail message is saved for later retrieval by the getMessage() method.
     */
    public InvalidPluginException(final String message) {
        super(message);
    }
}
