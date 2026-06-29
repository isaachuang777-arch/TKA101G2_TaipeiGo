package com.taipeigo.faq.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FaqRepository extends JpaRepository<FaqVO, Integer> {

	List<FaqVO> findByStatus(Byte status);

	List<FaqVO> findByTitleContainingOrContentContaining(String keyword , String keyword2);
}
