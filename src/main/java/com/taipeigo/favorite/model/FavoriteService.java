package com.taipeigo.favorite.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.taipeigo.favorite.dto.FavoriteDTO;
import com.taipeigo.product.model.ProductVO;

import com.taipeigo.activity.model.ActivityRepository;
import com.taipeigo.activity.model.ActivityVO;
import com.taipeigo.ticket.model.TicketRepository;
import com.taipeigo.ticket.model.TicketVO;

@Service
public class FavoriteService {

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private FavoriteProductRepository favoriteProductRepository;
    
    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private TicketRepository ticketRepository;

    // 查詢某位會員的所有我的最愛
    public List<FavoriteVO> getFavoritesByCustId(Integer custId) {
        return favoriteRepository.findByCustId(custId);
    }

    // 判斷是否已加入我的最愛
    public boolean isFavorite(Integer custId, Integer productId) {
        return favoriteRepository.existsByCustIdAndProductId(custId, productId);
    }
    
    // 依 type + id 判斷是否已加入我的最愛
    public boolean isFavoriteByTypeAndId(Integer custId, String type, Integer id) {

        ProductVO product = findProductByTypeAndId(type, id);

        if (product == null) {
            return false;
        }

        return favoriteRepository.existsByCustIdAndProductId(
                custId,
                product.getProductId()
        );
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
    
    // 依商品編號查詢商品
    private ProductVO getProduct(Integer productId) {

        return favoriteProductRepository
                .findById(productId)
                .orElse(null);
    }
    
    // 將 FavoriteVO 與 ProductVO 組成 FavoriteDTO
    private FavoriteDTO buildFavoriteDTO(FavoriteVO favorite, ProductVO product) {

        FavoriteDTO dto = new FavoriteDTO();

        dto.setFavoriteNo(favorite.getFavoriteNo());
        dto.setProductId(product.getProductId());
        dto.setProductName(product.getProductName());

        if (product.getActivityId() != null) {

            dto.setProductType("ACTIVITY");
            dto.setItemId(product.getActivityId());

            ActivityVO activity = activityRepository
                    .findById(product.getActivityId())
                    .orElse(null);

            if (activity != null
                    && activity.getActivityImage() != null
                    && !activity.getActivityImage().isEmpty()) {

                dto.setImageUrl(
                        activity.getActivityImage().get(0).getActivityImageSrc()
                );
            }

        } else if (product.getTicketId() != null) {

            dto.setProductType("TICKET");
            dto.setItemId(product.getTicketId());

            TicketVO ticket = ticketRepository
                    .findById(product.getTicketId())
                    .orElse(null);

            if (ticket != null
                    && ticket.getTicketImages() != null
                    && !ticket.getTicketImages().isEmpty()) {

                dto.setImageUrl(
                        ticket.getTicketImages().get(0).getTicketImageSrc()
                );
            }
        }

        return dto;
    }
    
    // 查詢某位會員的所有我的最愛，並轉成畫面用的 DTO
    public List<FavoriteDTO> getFavoriteDTOByCustId(Integer custId) {

        List<FavoriteVO> favoriteList = favoriteRepository.findByCustId(custId);

        List<FavoriteDTO> dtoList = new ArrayList<>();

        for (FavoriteVO favorite : favoriteList) {

            ProductVO product = getProduct(favorite.getProductId());

            if (product != null) {
                FavoriteDTO dto = buildFavoriteDTO(favorite, product);
                dtoList.add(dto);
            }
        }

        return dtoList;
    }
}