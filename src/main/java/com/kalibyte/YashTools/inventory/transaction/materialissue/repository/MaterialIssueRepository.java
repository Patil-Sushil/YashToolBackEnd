package com.kalibyte.YashTools.inventory.transaction.materialissue.repository;

import com.kalibyte.YashTools.inventory.transaction.materialissue.entity.MaterialIssue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MaterialIssueRepository extends JpaRepository<MaterialIssue, UUID> {
    Optional<MaterialIssue> findByIssueNumber(String issueNumber);
}
