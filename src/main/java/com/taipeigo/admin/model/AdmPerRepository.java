package com.taipeigo.admin.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.taipeigo.admin.model.AdmPerVO;

@Repository
public interface AdmPerRepository extends JpaRepository<AdmPerVO, Integer> {

}
