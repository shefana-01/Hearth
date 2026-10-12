package com.hearth.care.service;

import com.hearth.common.events.EventPublisher;
import com.hearth.common.events.Events;
import com.hearth.common.events.Topics;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Raises "starts within the hour" reminders. Once a minute it finds the appointments that begin in the next
 * hour and have not been reminded yet, tells the person each is for and whoever goes along, and marks them.
 *
 * Nobody is signed in when this runs, so it works from the appointment row alone: the family's time zone is
 * stored with it. A reminder is a notification for one member, never an audit line or a chat message, so a
 * private appointment stays private. If the visit is for someone the family looks after, notification-service
 * ignores that recipient (they do not use the app) and only the escort hears about it.
 *
 * Several instances can run: "skip locked" keeps two of them from reminding about the same row.
 */
@Component
@ConditionalOnProperty(name = "hearth.outbox.enabled", havingValue = "true")
public class ReminderJob {

    private static final Duration LEAD = Duration.ofMinutes(60);

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);

    private record Due(UUID id, UUID familyId, String title, String location, Instant start, UUID forId, UUID escortId, String timezone) {
    }

    private final JdbcTemplate jdbc;
    private final EventPublisher events;

    public ReminderJob(JdbcTemplate jdbc, EventPublisher events) {
        this.jdbc = jdbc;
        this.events = events;
    }

    @Scheduled(fixedDelayString = "${hearth.reminders.poll-ms:60000}")
    @Transactional
    public void remind() {
        Instant now = Instant.now();
        List<Due> due = jdbc.query(
                "select id, family_id, title, location, start_at, for_id, escort_id, timezone from appointment"
                        + " where reminder_sent_at is null and start_at > ? and start_at <= ? order by start_at limit 200 for update skip locked",
                (rs, row) -> new Due(
                        rs.getObject("id", UUID.class),
                        rs.getObject("family_id", UUID.class),
                        rs.getString("title"),
                        rs.getString("location"),
                        rs.getTimestamp("start_at").toInstant(),
                        rs.getObject("for_id", UUID.class),
                        rs.getObject("escort_id", UUID.class),
                        rs.getString("timezone")),
                Timestamp.from(now), Timestamp.from(now.plus(LEAD)));
        for (Due appointment : due) {
            Set<UUID> people = new LinkedHashSet<>();
            people.add(appointment.forId());
            if (appointment.escortId() != null) {
                people.add(appointment.escortId());
            }
            String message = message(appointment);
            for (UUID person : people) {
                events.publish(Topics.CARE, Events.of("appointment.reminder", appointment.familyId(), null)
                        .notifyMember(person, "reminder", message, "/appointments/" + appointment.id())
                        .data("appointmentId", appointment.id())
                        .build());
            }
            jdbc.update("update appointment set reminder_sent_at = ? where id = ?", Timestamp.from(now), appointment.id());
        }
    }

    /** "Dr Rahman — appointment at 3:00 PM, City Clinic." in the family's time zone. */
    private static String message(Due appointment) {
        String time = TIME.format(appointment.start().atZone(ZoneId.of(appointment.timezone())));
        String where = appointment.location() == null || appointment.location().isBlank() ? "" : ", " + appointment.location();
        return appointment.title() + " — appointment at " + time + where + ".";
    }
}
