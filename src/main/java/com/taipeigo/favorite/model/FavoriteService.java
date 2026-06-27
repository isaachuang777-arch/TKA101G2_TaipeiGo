package com.taipeigo.favorite.model;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.taipeigo.product.model.ProductVO;

@Service
public class FavoriteService {

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private FavoriteProductRepository favoriteProductRepository;

    // 查詢某位會員的所有我的最愛
    public List<FavoriteVO> getFavoritesByCustId(Integer custId) {
        return favoriteRepository.findByCustId(custId);
    }

    // 判斷是否已加入我的最愛
    public boolean isFavorite(Integer custId, Integer productId) {
        return favoriteRepository.existsByCustIdAndProductId(custId, productId);
    }

    // 依 type + id 找出 PRODUCTS 對應商品
    private ProductVO findProductByTypeAndId(String type, Integer id) {

        if (type == null || id == null) {
            return null;
        }

        Optional<ProductVO> productOpt;

        if ("ACTIVITY".equalsIgnoreCase(type)) {
            productOpt = favoriteProductRepository.findByActivityId(id);
        } else if ("TICKET".equalsIgnoreCase(type)) {
            productOpt = favoriteProductRepository.findByTicketId(id);
        } else {
            return null;
        }

        return productOpt.orElse(null);
    }
    
 	// 切換我的最愛狀態（已加入則取消，未加入則新增)
    @Transactional
    public boolean toggleFavorite(Integer custId, String type, Integer id) {

        ProductVO product = findProductByTypeAndId(type, id);

        if (product == null) {
            return false;
        }

        if (product.getStatus() == null || product.getStatus() != 1) {
            return false;
        }

        Integer productId = product.getProductId();

        if (favoriteRepository.existsByCustIdAndProductId(custId, productId)) {
            favoriteRepository.deleteByCustIdAndProductId(custId, productId);
            return false; // 代表取消
        }

        FavoriteVO favorite = new FavoriteVO();
        favorite.setCustId(custId);
        favorite.setProductId(productId);
        favoriteRepository.save(favorite);

        return true; // 代表加入
    }
}