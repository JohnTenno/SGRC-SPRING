package com.app.modules.tutoring;

import com.app.modules.tutoring.entity.TutoringSubject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TutoringSubjectRepository extends JpaRepository<TutoringSubject, Integer> {
}
