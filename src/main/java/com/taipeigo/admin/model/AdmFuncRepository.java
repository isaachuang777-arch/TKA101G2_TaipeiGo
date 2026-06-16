package com.taipeigo.admin.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.taipeigo.admin.model.AdmFuncVO;

@Repository
public interface AdmFuncRepository extends JpaRepository<AdmFuncVO, Integer> {

}
