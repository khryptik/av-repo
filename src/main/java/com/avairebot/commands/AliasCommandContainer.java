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

package com.avbot.commands;

public class AliasCommandContainer extends CommandContainer {

    private final String[] aliasArguments;

    public AliasCommandContainer(CommandContainer container, String[] aliasArguments) {
        super(container.getCommand(), container.getCategory(), container.getSourceUri());

        this.aliasArguments = aliasArguments;
    }

    public String[] getAliasArguments() {
        return aliasArguments;
    }
}
