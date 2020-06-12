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

package com.avbot.commands.music.playlist;

import com.avbot.av;
import com.avbot.Constants;
import com.avbot.commands.CommandMessage;
import com.avbot.commands.music.PlaylistCommand;
import com.avbot.contracts.commands.playlist.PlaylistSubCommand;
import com.avbot.database.controllers.PlaylistController;
import com.avbot.database.transformers.GuildTransformer;
import com.avbot.database.transformers.PlaylistTransformer;

import java.sql.SQLException;

public class RemoveSongFromPlaylist extends PlaylistSubCommand {

    public RemoveSongFromPlaylist(av av, PlaylistCommand command) {
        super(av, command);
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args, GuildTransformer guild, PlaylistTransformer playlist) {
        if (!isValidRequest(context, args, playlist)) {
            return false;
        }

        try {
            int id = Integer.parseInt(args[2], 10) - 1;
            if (id < 0 || id >= playlist.getSongs().size()) {
                context.makeWarning(context.i18n("invalidIdGiven"))
                    .set("command", command.generateCommandTrigger(context.getMessage()) + " " + playlist.getName() + " removesong <song id>")
                    .set("type", id < 0 ? "low" : "high")
                    .queue();

                return false;
            }

            PlaylistTransformer.PlaylistSong removed = playlist.getSongs().remove(id);

            av.getDatabase().newQueryBuilder(Constants.MUSIC_PLAYLIST_TABLE_NAME)
                .where("id", playlist.getId()).andWhere("guild_id", context.getGuild().getId())
                .update(statement -> {
                    statement.set("songs", av.gson.toJson(playlist.getSongs()), true);
                    statement.set("amount", playlist.getSongs().size());
                });

            PlaylistController.forgetCache(context.getGuild().getIdLong());

            context.makeSuccess(context.i18n("songHasBeenRemovedFromPlaylist"))
                .set("song", String.format("[%s](%s)", removed.getTitle(), removed.getLink()))
                .set("playlist", playlist.getName())
                .queue();

            return true;
        } catch (NumberFormatException e) {
            context.makeWarning(context.i18n("invalidIdNumberGiven"))
                .set("command", command.generateCommandTrigger(context.getMessage()) + " " + playlist.getName() + " removesong <song id>")
                .queue();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    private boolean isValidRequest(CommandMessage context, String[] args, PlaylistTransformer playlist) {
        if (args.length < 3) {
            context.makeWarning(context.i18n("invalidFormat"))
                .set("command", command.generateCommandTrigger(context.getMessage()) + " " + playlist.getName() + " removesong <song id>")
                .set("type", "song id")
                .queue();

            return false;
        }

        if (playlist.getSongs().isEmpty()) {
            context.makeWarning(context.i18n("playlistIsAlreadyEmpty"))
                .set("playlist", playlist.getName())
                .queue();

            return false;
        }

        return true;
    }
}
