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

import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;

public class TrackResponse {

    private final GuildMusicManager musicManager;
    private final AudioItem audioItem;
    private final TrackRequestContext trackContext;

    public TrackResponse(GuildMusicManager musicManager, AudioItem audioItem, TrackRequestContext trackContext) {
        this.musicManager = musicManager;
        this.audioItem = audioItem;
        this.trackContext = trackContext;
    }

    public GuildMusicManager getMusicManager() {
        return musicManager;
    }

    public AudioItem getAudioItem() {
        return audioItem;
    }

    public TrackRequestContext getTrackContext() {
        return trackContext;
    }

    public boolean isPlaylist() {
        return getAudioItem() instanceof AudioPlaylist;
    }
}
