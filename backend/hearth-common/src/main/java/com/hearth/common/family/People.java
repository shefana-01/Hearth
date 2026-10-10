package com.hearth.common.family;

import java.util.List;
import java.util.UUID;

/**
 * Utility methods for looking up people (members) from a {@link FamilyDirectory}.
 *
 * <p>"People" in care-service terms means anyone in the family roster, whether a
 * full member or someone the family looks after.
 */
public final class People {

    private People() {
    }

    /** Returns {@code true} if the given id matches any member in the directory. */
    public static boolean exists(FamilyDirectory directory, UUID id) {
        if (id == null) {
            return false;
        }
        return directory.members().stream().anyMatch(m -> m.id().equals(id));
    }

    /** Returns {@code true} if the given id is a full (non-guest) member of the family. */
    public static boolean isMember(FamilyDirectory directory, UUID id) {
        if (id == null) {
            return false;
        }
        return directory.members().stream().anyMatch(m -> m.id().equals(id));
    }

    /**
     * Returns the full display name of the person with {@code id}, or
     * {@value Names#UNASSIGNED} when not found.
     */
    public static String name(FamilyDirectory directory, UUID id) {
        return Names.of(directory.members(), id);
    }

    /**
     * Returns just the first name of the person with {@code id}, or
     * {@value Names#UNASSIGNED} when not found.
     */
    public static String firstName(FamilyDirectory directory, UUID id) {
        return Names.firstOf(directory.members(), id);
    }

    /** Finds the {@link MemberRef} for {@code id}, or {@code null} when absent. */
    public static MemberRef find(FamilyDirectory directory, UUID id) {
        if (id == null) {
            return null;
        }
        return directory.members().stream().filter(m -> m.id().equals(id)).findFirst().orElse(null);
    }

    /** All members whose id is in {@code ids}. */
    public static List<MemberRef> forIds(FamilyDirectory directory, Iterable<UUID> ids) {
        if (ids == null) {
            return List.of();
        }
        return directory.members().stream()
                .filter(m -> {
                    for (UUID id : ids) {
                        if (m.id().equals(id)) return true;
                    }
                    return false;
                })
                .toList();
    }
}
