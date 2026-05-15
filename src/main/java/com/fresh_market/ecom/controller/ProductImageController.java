package com.fresh_market.ecom.controller;

import com.fresh_market.ecom.model.ProductImage;
import com.fresh_market.ecom.service.IProductImage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Product Images", description = "Manages images associated with products")
@RestController
@RequestMapping("/api/products/{productId}/images")
public class ProductImageController {

    private final IProductImage productImageService;

    public ProductImageController(IProductImage productImageService) {
        this.productImageService = productImageService;
    }

    @Operation(summary = "Get all images for a product")
    @GetMapping
    public List<ProductImage> getProductImagesByProductId(@PathVariable UUID productId) {
        return productImageService.getProductImages(productId);
    }

    @Operation(summary = "Add an image to a product")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductImage addImage(@PathVariable UUID productId, @RequestBody CreateImageRequest request) {
        return productImageService.addProductImage(productId, request.getImageUrl());
    }

    @Operation(summary = "Delete a product image")
    @DeleteMapping("/{imageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteImage(@PathVariable UUID productId, @PathVariable UUID imageId) {
        productImageService.deleteProductImage(imageId);
    }

    public static class CreateImageRequest {
        public String imageUrl;

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }
    }
}
