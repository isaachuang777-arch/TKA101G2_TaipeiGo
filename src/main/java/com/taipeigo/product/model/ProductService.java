package com.taipeigo.product.model;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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

    // 取所有商品 (支援分頁，一頁10筆)
    public Page<ProductVO> getProductsByKeyword(String keyword, int page) {
        // page - 1 因為 Spring Data JPA 的頁碼是從 0 開始
        Pageable pageable = PageRequest.of(page - 1, 10);

        if(keyword == null || keyword.trim().isEmpty()){
            return productRepository.findAll(pageable);
        }

        return productRepository.findByKeyword(keyword, pageable);
    }

    // 用ID取得商品
    public ProductVO getProductById(Integer id){
        return productRepository.findById(id).orElse(null);
    }

    // 新增商品
    public ProductVO addProduct(ProductVO product){

        if(product.getStatus() == null){
            product.setStatus(1);
        }

        return productRepository.save(product);
    }

    // 更新商品
    public ProductVO updateProduct(ProductVO product){

        ProductVO existingProduct = productRepository.findById(product.getProductId()).orElse(null);
        
        if(existingProduct != null){

            existingProduct.setProductName(product.getProductName());
            existingProduct.setActivityId(product.getActivityId());
            existingProduct.setTicketId(product.getTicketId());
            existingProduct.setStatus(product.getStatus());
            return productRepository.save(existingProduct);
        }

        return null;
    }

    // 上下架切換
    public ProductVO toggleProductStatus(Integer productId){
        ProductVO product = productRepository.findById(productId).orElse(null);
        if(product != null) {
            product.setStatus(product.getStatus() == 1 ? 0 : 1);
            return productRepository.save(product);
        }
        return null;
    }

}
