package com.fresh_market.ecom.service;

import com.fresh_market.ecom.model.ProductImage;

import java.util.List;
import java.util.UUID;

public interface IProductImage {
    List<ProductImage> getProductImages(UUID productId);
    ProductImage getImageById(UUID id);
    ProductImage addProductImage(UUID productId, String ImageUrl);
    void deleteProductImage(UUID id);
    void deleteImagesByProductId(UUID productId);
}
