package com.example.bustleplayer

import java.lang.String.format
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class Utils @Inject constructor() {
    fun getDurationString(durationMs: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(durationMs)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs)

        return if (hours > 0) {
            format(
                Locale.getDefault(), "%s%02d:%02d:%02d",
                "",
                hours,
                minutes - TimeUnit.HOURS.toMinutes(hours),
                seconds - TimeUnit.MINUTES.toSeconds(minutes)
            )
        } else format(
            Locale.getDefault(), "%s%02d:%02d",
            "",
            minutes,
            seconds - TimeUnit.MINUTES.toSeconds(minutes)
        )
    }
}