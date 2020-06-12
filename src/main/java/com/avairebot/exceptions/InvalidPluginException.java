/*
 * Copyright (c) 2018.
 *
 * This file is part of av.
 *
 * av is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * av is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with av.  If not, see <https://www.gnu.org/licenses/>.
 *
 *
 */

package com.avbot.exceptions;

public class InvalidPluginException extends Exception {

    public InvalidPluginException(String message) {
        super(message);
    }

    public InvalidPluginException(String message, Object... args) {
        super(String.format(message, args));
    }

    public InvalidPluginException(String message, Throwable cause) {
        super(message, cause);
    }
}
