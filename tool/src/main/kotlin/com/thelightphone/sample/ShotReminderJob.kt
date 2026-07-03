package com.thelightphone.sample

import com.thelightphone.sdk.LightJob
import com.thelightphone.sdk.LightJobHandler
import com.thelightphone.sdk.LightJobResult
import com.thelightphone.sdk.buildDatabase

/**
 * Runs every 15 minutes (WorkManager's periodic minimum), not every
 * [REMINDER_CYCLE] — see ReminderScheduler for why. Each tick re-reads the
 * latest shot from the database and only marks the reminder fired once the
 * real 7-day cycle has actually elapsed since that shot, so it self-corrects
 * regardless of when it happens to run and needs no "reset" step when a new
 * shot is logged.
 */
@LightJob(SHOT_REMINDER_JOB_KEY)
val shotReminderJob: LightJobHandler = { lightContext, _ ->
    val database = AppDatabase.getInstance {
        lightContext.buildDatabase(AppDatabase::class.java, AppDatabase.DATABASE_NAME)
    }
    val latestShot = ShotRepository.getInstance(database).getLatestShot()
    if (latestShot != null) {
        val dueAtMillis = latestShot.timestampMillis + REMINDER_CYCLE.inWholeMilliseconds
        if (System.currentTimeMillis() >= dueAtMillis) {
            markReminderFired(lightContext.dataStore, System.currentTimeMillis())
        }
    }
    LightJobResult.Success()
}
