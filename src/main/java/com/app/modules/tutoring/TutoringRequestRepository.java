package com.app.modules.tutoring;

import com.app.modules.tutoring.entity.TutoringRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TutoringRequestRepository extends JpaRepository<TutoringRequest, Integer> {
    List<TutoringRequest> findByStudentEnrollment(String enrollment);
}
