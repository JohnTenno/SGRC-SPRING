package com.app.modules.tutoring;

import com.app.modules.tutoring.entity.TutoringProfessor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TutoringProfessorRepository extends JpaRepository<TutoringProfessor, String> {
}
