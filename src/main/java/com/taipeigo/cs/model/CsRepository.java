package com.taipeigo.cs.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CsRepository extends JpaRepository<CsVO, Integer	> {

	List<CsVO> findByCustomerVO_CustId(Integer custId);

	
}
