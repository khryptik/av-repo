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

package com.avbot.audio;

import com.avbot.av;
import com.avbot.scheduler.ScheduleHandler;
import com.avbot.shared.DiscordConstants;
import lavalink.client.io.LavalinkSocket;
import lavalink.client.io.Link;
import lavalink.client.io.jda.JdaLavalink;
import lavalink.client.io.jda.JdaLink;
import lavalink.client.io.metrics.LavalinkCollector;
import lavalink.client.player.IPlayer;
import lavalink.client.player.LavaplayerPlayerWrapper;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.VoiceChannel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class LavalinkManager {

    private JdaLavalink lavalink = null;
    private boolean enabled;

    /**
     * Start the Lavalink Manager, checking if Lavalink is enabled is enabled
     * in the config, as well as connecting to all the Lavalink nodes.
     *
     * @param av The av application instance.
     */
    public void start(av av) {
        enabled = av.getConfig().getBoolean("lavalink.enabled", false);

        if (!isEnabled()) {
            return;
        }

        List<Map<?, ?>> nodes = av.getConfig().getMapList("lavalink.nodes");
        if (nodes.isEmpty()) {
            enabled = false;
            return;
        }

        lavalink = new JdaLavalink(av.getConfig().getString("discord.clientId", "" + DiscordConstants.av_BOT_ID),
            av.getSettings().getShardCount() < 1 ? 1 : av.getSettings().getShardCount(),
            shardId -> av.getShardManager().getShardById(shardId)
        );
        Runtime.getRuntime().addShutdownHook(new Thread(lavalink::shutdown, "lavalink-shutdown-hook"));

        for (Map<?, ?> node : nodes) {
            if (!node.containsKey("name") || !node.containsKey("host") || !node.containsKey("pass")) {
                continue;
            }

            try {
                URI host = new URI((String) node.get("host"));

                lavalink.addNode(
                    (String) node.get("name"), host, (String) node.get("pass")
                );
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }

        new LavalinkCollector(lavalink).register();
    }

    /**
     * Checks if Lavalink is enabled.
     *
     * @return <code>True</code> if Lavalink is enabled, <code>False</code> otherwise.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Creates the music player for the given guild ID, if Lavalink is enabled a player
     * will be created on a remote node that can be used to play music remotely, if
     * Lavalink is disabled a internal Lavaplayer player will be created instead.
     *
     * @param guildId The ID of the guild to create the audio player for.
     * @return The player interface that can be used to communicate with the player.
     */
    IPlayer createPlayer(String guildId) {
        return isEnabled()
            ? lavalink.getLink(guildId).getPlayer()
            : new LavaplayerPlayerWrapper(AudioHandler.getDefaultAudioHandler().getPlayerManager().createPlayer());
    }

    /**
     * Opens a connection to the given voice channel, in the event Lavalink is enabled
     * and the link for the current guild is being destroyed, a 500 millisecond
     * delay will be added every time the link has not finished shutting down
     * completely until the connection can be successfully be opened.
     *
     * @param channel The voice channel that the bot should be connecting to.
     */
    @SuppressWarnings("WeakerAccess")
    public void openConnection(VoiceChannel channel) {
        openConnection(channel, false);
    }

    /**
     * Opens a connection to the given voice channel, in the event Lavalink is enabled
     * and the link for the current guild is being destroyed, a 500 millisecond
     * delay will be added every time the link has not finished shutting down
     * completely until the connection can be successfully be opened.
     *
     * @param channel   The voice channel that the bot should be connecting to.
     * @param forceOpen Determine if the connection should be forcefully opened, this
     *                  is false by default, but in the event that Lavalink is used,
     *                  and the link is being destroyed, the method will call
     *                  itself with force true to make sure we establish a
     *                  voice connection.
     */
    @SuppressWarnings("WeakerAccess")
    public void openConnection(VoiceChannel channel, boolean forceOpen) {
        if (!isEnabled() || forceOpen) {
            channel.getGuild().getAudioManager().openAudioConnection(channel);
        }

        if (isEnabled()) {
            JdaLink link = lavalink.getLink(channel.getGuild());

            if (isLinkBeingDestroyed(link) && !forceOpen) {
                ScheduleHandler.getScheduler().schedule(
                    () -> openConnection(channel, true), 500, TimeUnit.MILLISECONDS
                );
                return;
            }

            link.connect(channel);
        }
    }

    /**
     * Closes a connection connection for the currently connected server.
     *
     * @param guild The guild that the bot should close the connection for.
     */
    public void closeConnection(Guild guild) {
        if (isEnabled()) {
            JdaLink link = lavalink.getExistingLink(guild);

            if (link != null && !isLinkBeingDestroyed(link)) {
                link.disconnect();
            }
        } else {
            guild.getAudioManager().closeAudioConnection();
        }
    }

    /**
     * Gets the connected voice channel for the given guild.
     *
     * @param guild The guild that the connected voice channel should be fetched for.
     * @return Possibly-null, The VoiceChannel that the bot is connected to.
     */
    @Nullable
    public VoiceChannel getConnectedChannel(@Nonnull Guild guild) {
        return guild.getSelfMember().getVoiceState().getChannel();
    }

    /**
     * Checks if the given JDA Link instance is currently being destroyed, this is done
     * by checking the links state for {@link Link.State#DESTROYING destorying}
     * and {@link Link.State#DESTROYED destroyed} states.
     *
     * @param link The link that should be checked.
     * @return <code>True</code> if the link is being destroyed, <code>False</code> otherwise.
     */
    public boolean isLinkBeingDestroyed(JdaLink link) {
        return isLinkInState(link, Link.State.DESTROYING, Link.State.DESTROYED);
    }

    /**
     * Checks if the links state is any of the given states, if one of the
     * given states matches the links state, the method will return true.
     *
     * @param link   The link that should be checked.
     * @param states The list of states that should be compared to the links state.
     * @return <code>True</code> if the links state matches any of the given
     *         states, <code>False</code> otherwise.
     */
    @SuppressWarnings("WeakerAccess")
    public boolean isLinkInState(JdaLink link, Link.State... states) {
        for (Link.State state : states) {
            if (state != null && state.equals(link.getState())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if Lavalink has at least one connected
     * node which can be used to stream music.
     *
     * @return {@code True} if at least one node is available
     *         for streaming music, {@code False} otherwise.
     */
    public boolean hasConnectedNodes() {
        if (lavalink.getNodes().isEmpty()) {
            return false;
        }

        for (LavalinkSocket socket : lavalink.getNodes()) {
            if (socket.isOpen()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the JDA Lavalink instance.
     *
     * @return The JDA Lavalink instance.
     */
    public JdaLavalink getLavalink() {
        return lavalink;
    }

    public static class LavalinkManagerHolder {
        public static final LavalinkManager lavalink = new LavalinkManager();
    }
}
