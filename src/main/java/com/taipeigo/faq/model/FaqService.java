package com.taipeigo.faq.model;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Service
public class FaqService {

    @Autowired
    private FaqRepository faqRepository;

    public List<FaqVO> getAll() {
        return faqRepository.findAll();
    }

    public FaqVO getOneFaq(Integer faqId) {
        Optional<FaqVO> optional = faqRepository.findById(faqId);
        return optional.orElse(null);
    }

    public void addFaq(FaqVO faqVO) {
        faqRepository.save(faqVO);
    }

    public void updateFaq(FaqVO faqVO) {
        faqRepository.save(faqVO);
    }

    public void deleteFaq(Integer faqId) {
        faqRepository.deleteById(faqId);
    }
    
    // 查分頁用
    public Page<FaqVO> getFaqByPage(int page) {
        Pageable pageable = PageRequest.of(page, 10);
        return faqRepository.findAll(pageable);
    }
}