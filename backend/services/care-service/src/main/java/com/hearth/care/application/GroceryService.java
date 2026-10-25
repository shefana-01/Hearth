package com.hearth.care.application;

import com.hearth.care.domain.GroceryItem;
import com.hearth.care.infrastructure.repository.GroceryItemRepository;
import com.hearth.common.error.ApiException;
import com.hearth.common.family.FamilyContext;
import com.hearth.common.family.FamilyDirectory;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HEAR-462: Manages the family grocery shopping list.
 */
@Service
public class GroceryService {

    public record GroceryItemView(UUID id, UUID familyId, String name, String quantity, String category, boolean checked, UUID addedBy, Instant createdAt) {}
    public record CreateGroceryRequest(String name, String quantity, String category) {}

    private final GroceryItemRepository repository;
    private final FamilyDirectory directory;

    public GroceryService(GroceryItemRepository repository, FamilyDirectory directory) {
        this.repository = repository;
        this.directory = directory;
    }

    @Transactional(readOnly = true)
    public List<GroceryItemView> list() {
        FamilyContext context = directory.context();
        return repository.findByFamilyIdOrderByCreatedAtDesc(context.familyId()).stream()
                .map(this::toView)
                .toList();
    }

    @Transactional
    public GroceryItemView add(CreateGroceryRequest request) {
        FamilyContext context = directory.context();
        Instant now = Instant.now();
        GroceryItem item = new GroceryItem(
                UUID.randomUUID(),
                context.familyId(),
                request.name().trim(),
                request.quantity() != null ? request.quantity().trim() : null,
                request.category() != null ? request.category().trim() : "Other",
                false,
                context.memberId(),
                now,
                now
        );
        return toView(repository.save(item));
    }

    @Transactional
    public GroceryItemView toggleChecked(UUID id) {
        FamilyContext context = directory.context();
        GroceryItem item = repository.findById(id)
                .filter(i -> i.getFamilyId().equals(context.familyId()))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Grocery item not found"));
        item.setChecked(!item.isChecked());
        item.setUpdatedAt(Instant.now());
        return toView(repository.save(item));
    }

    @Transactional
    public void delete(UUID id) {
        FamilyContext context = directory.context();
        GroceryItem item = repository.findById(id)
                .filter(i -> i.getFamilyId().equals(context.familyId()))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Grocery item not found"));
        repository.delete(item);
    }

    @Transactional
    public void clearChecked() {
        FamilyContext context = directory.context();
        repository.deleteByFamilyIdAndCheckedTrue(context.familyId());
    }

    @Transactional
    public void personLeft(UUID familyId, UUID personId) {
        if (familyId != null && personId != null) {
            repository.deleteByFamilyIdAndAddedBy(familyId, personId);
        }
    }

    private GroceryItemView toView(GroceryItem item) {
        return new GroceryItemView(item.getId(), item.getFamilyId(), item.getName(), item.getQuantity(), item.getCategory(), item.isChecked(), item.getAddedBy(), item.getCreatedAt());
    }
}
