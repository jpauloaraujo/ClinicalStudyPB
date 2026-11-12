package UEPB.ESWII.ClinicalStudyPB.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import UEPB.ESWII.ClinicalStudyPB.domain.model.ScheduledVisit;

public interface ScheduledVisitRepository extends JpaRepository<ScheduledVisit, UUID> {
}
