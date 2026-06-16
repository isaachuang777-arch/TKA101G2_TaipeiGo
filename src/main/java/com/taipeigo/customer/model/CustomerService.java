package com.taipeigo.customer.model;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository repository;

    // 新增會員
    public void addCustomer(CustomerVO customerVO) {
        repository.save(customerVO);
    }

    // 修改會員
    public void updateCustomer(CustomerVO customerVO) {
        repository.save(customerVO);
    }

    // 刪除會員
    public void deleteCustomer(Integer custId) {
        if (repository.existsById(custId)) {
            repository.deleteById(custId);
        }
    }

    // 查單筆
    public CustomerVO getOneCustomer(Integer custId) {
        Optional<CustomerVO> optional = repository.findById(custId);
        return optional.orElse(null);
    }

    // 查全部
    public List<CustomerVO> getAllCustomers() {
        return repository.findAll(Sort.by(Sort.Direction.DESC, "custId"));
    }

    // 登入用（帳號查詢）
    public CustomerVO findByAccount(String account) {
        return repository.findByCustAccount(account);
    }

    // 模糊查詢（姓名）
    public List<CustomerVO> findByNameLike(String name) {
        return repository.findByNameLike(name);
    }

    // 帳號是否存在
    public boolean isAccountExist(String account) {
        return repository.findByCustAccount(account) != null;
    }
    
    // 分頁
    public Page<CustomerVO> getCustomersByPage(int page) {
        Pageable pageable = PageRequest.of(
            page,
            10,
            Sort.by(Sort.Direction.DESC, "custId")
        );

        return repository.findAll(pageable);
    }

    /**
     * 台灣身分證字號 Checksum 驗證
     * 演算法：
     *   1. 英文字母對應兩位數字（A=10, B=11, ... Z=35，但跳過 I=34, O=35 等依官方對照表）
     *   2. 第一碼數字十位數 * 1 + 個位數 * 9，加上後續各碼乘以遞減權重
     *   3. 總和 mod 10 == 0 則合法
     */
    public boolean isValidTaiwanId(String id) {
        if (id == null || !id.matches("^[A-HJ-NPR-Z][12][0-9]{8}$")) {
            return false;
        }

        int[] letterValues = {
            10, 11, 12, 13, 14, 15, 16, 17, 34, 18, 19, 20, 21,
            22, 35, 23, 24, 25, 26, 27, 28, 29, 32, 30, 31, 33
        };
        // A=0, B=1, ... Z=25

        char firstChar = id.charAt(0);
        int letterIndex = firstChar - 'A';
        int letterValue = letterValues[letterIndex];

        int sum = (letterValue / 10) + (letterValue % 10) * 9;

        int[] weights = {8, 7, 6, 5, 4, 3, 2, 1};
        for (int i = 0; i < 8; i++) {
            sum += Character.getNumericValue(id.charAt(i + 1)) * weights[i];
        }

        sum += Character.getNumericValue(id.charAt(9));

        return sum % 10 == 0;
    }
}
