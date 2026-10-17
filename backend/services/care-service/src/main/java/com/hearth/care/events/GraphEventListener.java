package com.hearth.care.events;

import com.hearth.care.graph.GraphState;
import com.hearth.common.events.DomainEvent;
import com.hearth.common.events.EventReader;
import com.hearth.common.events.Topics;
import java.util.Set;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Keeps the family map honest: whenever a task, member, dependant, family or appointment changes anywhere,
 * this family's graph is marked stale, and it is rebuilt the next time someone opens it.
 *
 * It has its own consumer group, so it receives every event independently of
 * CareEventListener. Handling an event twice is harmless: the graph is only
 * marked stale again.
 */
@Component
public class GraphEventListener {

    private static final Set<String> PREFIXES_THAT_CHANGE_THE_MAP = Set.of("task.", "member.", "dependant.", "family.", "appointment.");

    /** Raised by a timer or a status line, not by a change to the people, tasks or visits on the map. */
    private static final Set<String> NOISE = Set.of("task.reminder", "appointment.reminder", "member.status_changed");

    private final EventReader reader;
    private final GraphState state;

    public GraphEventListener(EventReader reader, GraphState state) {
        this.reader = reader;
        this.state = state;
    }

    @KafkaListener(topics = {Topics.FAMILY, Topics.TASK, Topics.CARE}, groupId = "care-service-graph")
    public void onEvent(String message) {
        DomainEvent event = reader.read(message);
        if (event == null) {
            return;
        }
        String type = event.type();
        if (!NOISE.contains(type) && PREFIXES_THAT_CHANGE_THE_MAP.stream().anyMatch(type::startsWith)) {
            state.changed(event.familyId());
        }
    }
}
