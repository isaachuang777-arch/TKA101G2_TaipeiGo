package com.taipeigo.product.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.taipeigo.activity.model.ActivityVO;
import com.taipeigo.ticket.model.TicketVO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository){

        this.productRepository = productRepository;
    }

    // 用ID取得商品
    public ProductVO getProductById(Integer id){
        return productRepository.findById(id).orElse(null);
    }

    // 自動連動(ticket或activity只要有上架就會自動新增)

    public void syncActivityToProduct(ActivityVO activity){

        if(activity == null || activity.getActivityId() == null)
            return;

        // 找是否已有該活動商品，沒有的話就 new 一個
        ProductVO product = productRepository.findByActivityId(activity.getActivityId()).orElse(new ProductVO());

        product.setActivityId(activity.getActivityId());
        product.setProductName(activity.getActivityName());

        //連動下架，活動如果下架商品也會自動下架

        product.setStatus(activity.getActivityStatus());

        productRepository.save(product);

    }

    public void syncTicketToProduct(TicketVO ticket){

        if (ticket == null || ticket.getTicketId() == null) {

            return;
            
        }
    

    ProductVO product = productRepository.findByTicketId(ticket.getTicketId()).orElse(new ProductVO());

    product.setTicketId(ticket.getTicketId());
    product.setProductName(ticket.getTicketName());
    product.setStatus(ticket.getTicketStatus());

    productRepository.save(product);

    }
    

}
