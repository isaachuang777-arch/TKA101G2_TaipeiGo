package com.taipeigo.cs.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CsMsgRepository extends JpaRepository<CsMsgVO, Integer> {

}
