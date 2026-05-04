package com.fresh_market.ecom.service;

import com.fresh_market.ecom.model.Product;

import java.util.List;
import java.util.UUID;

public interface IProductService {
    List<Product> getAllProducts();
    Product getProductById(UUID id);
    Product createProduct(Product product);
    Product updateProduct(UUID id, Product product);
    void deleteProduct(UUID id);
}