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

package com.avbot.scheduler.jobs;

import com.avbot.av;
import com.avbot.Constants;
import com.avbot.contracts.scheduler.Job;
import com.avbot.database.connections.SQLite;
import com.avbot.language.I18n;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

/**
 * This job will delete any player experience record from the database
 * that doesn't have a valid guild attached/linked to it.
 */
public class DeleteUnusedPlayerDataJob extends Job {

    private static final Logger log = LoggerFactory.getLogger(DeleteUnusedPlayerDataJob.class);

    public DeleteUnusedPlayerDataJob(av av) {
        super(av, 5, 120, TimeUnit.MINUTES);
    }

    @Override
    public void run() {
        try {
            if (av.getDatabase().getConnection() instanceof SQLite) {
                return;
            }
        } catch (SQLException ignored) {
        }

        try {
            av.getDatabase().queryUpdate(I18n.format(
                "DELETE `{0}` FROM `{0}` LEFT JOIN `{1}` ON `{0}`.`guild_id` = `{1}`.`id` WHERE `{1}`.`id` IS NULL;",
                Constants.PLAYER_EXPERIENCE_TABLE_NAME, Constants.GUILD_TABLE_NAME
            ));
        } catch (SQLException e) {
            log.error("Failed to delete unused player data records, error: {}", e.getMessage());
        }
    }
}
