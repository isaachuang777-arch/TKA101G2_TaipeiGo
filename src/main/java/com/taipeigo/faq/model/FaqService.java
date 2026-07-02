package com.taipeigo.faq.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

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
    
    //---for frontend json
    public List<FeFaqDto> getAllwithouttime(){
 	   //voList已經拿好只有 只有可顯示的Faq List 
 	   List<FaqVO> voList= faqRepository.findByStatus((byte) 1);
 	   //一個沒有時間 沒有狀態的DTO list
 	   List<FeFaqDto> dtoList = new ArrayList();
 	   
 	   for(FaqVO faqList : voList) {
 		   FeFaqDto dto = new FeFaqDto();
 		   
 		   dto.setFaqId(faqList.getFaqId());
 		   dto.setCategory(faqList.getCategory());
 		   dto.setTitle(faqList.getTitle());
 		   dto.setContent(faqList.getContent());
 		   
 		   dtoList.add(dto);
 		   
 	   }
 	   return dtoList;
 	   
    }
    
    // 後台複合查詢 + 分頁 + 分類
    public Page<FaqVO> searchFaqs(int page, String keyword, Byte status, Integer category) {

        Pageable pageable = PageRequest.of(
                page,
                10,
                Sort.by(Sort.Direction.DESC, "createTime")
        );

        Specification<FaqVO> spec = (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.trim().isEmpty()) {
                String kw = "%" + keyword.trim() + "%";

                predicates.add(cb.or(
                        cb.like(root.get("title"), kw),
                        cb.like(root.get("content"), kw)
                ));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            
            if (category != null) {
                predicates.add(cb.equal(root.get("category"), category));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return faqRepository.findAll(spec, pageable);
    }
}