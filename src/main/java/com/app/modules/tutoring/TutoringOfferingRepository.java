package com.app.modules.tutoring;

import com.app.modules.tutoring.entity.TutoringOffering;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TutoringOfferingRepository extends JpaRepository<TutoringOffering, String> {
}
