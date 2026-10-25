package com.hearth.care.infrastructure.repository;

import com.hearth.care.domain.GroceryItem;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * HEAR-462: Repository for family shopping list items.
 */
@Repository
public interface GroceryItemRepository extends JpaRepository<GroceryItem, UUID> {
    List<GroceryItem> findByFamilyIdOrderByCreatedAtDesc(UUID familyId);
    List<GroceryItem> findByFamilyIdOrderByCreatedAtAsc(UUID familyId);
    void deleteByFamilyIdAndCheckedTrue(UUID familyId);
    void deleteByFamilyIdAndAddedBy(UUID familyId, UUID addedBy);
}
