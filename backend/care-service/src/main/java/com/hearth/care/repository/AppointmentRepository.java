package com.hearth.care.repository;

import com.hearth.care.entity.Appointment;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    List<Appointment> findByFamilyIdOrderByStartAtAsc(UUID familyId);

    Optional<Appointment> findByIdAndFamilyId(UUID id, UUID familyId);

    List<Appointment> findByFamilyIdAndEscortId(UUID familyId, UUID escortId);

    List<Appointment> findByFamilyIdAndForId(UUID familyId, UUID forId);

    /** Visits the whole family can see: the only ones that ever enter the family map. */
    List<Appointment> findByFamilyIdAndVisibilityOrderByStartAtAsc(UUID familyId, String visibility);
}
