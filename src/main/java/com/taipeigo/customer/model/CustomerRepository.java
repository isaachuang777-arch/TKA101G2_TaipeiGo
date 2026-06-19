package com.taipeigo.customer.model;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface CustomerRepository 
        extends JpaRepository<CustomerVO, Integer> {

    @Transactional
    @Modifying
    @Query(value = "delete from customer where CUST_ID = ?1", nativeQuery = true)
    void deleteByCustId(int custId);

    // 用帳號查
    CustomerVO findByCustAccount(String custAccount);
    
    // 用 Email 查
    CustomerVO findByCustEmail(String custEmail);

    // 用身分證查
    CustomerVO findByCustIdCard(String custIdCard);
    
    // 模糊查姓名（JPQL）
    @Query("from CustomerVO c where c.custName like %?1%")
    List<CustomerVO> findByNameLike(String name);
}