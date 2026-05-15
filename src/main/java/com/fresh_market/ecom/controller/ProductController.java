package com.fresh_market.ecom.controller;

import com.fresh_market.ecom.model.Product;
import com.fresh_market.ecom.service.IProductService;
import com.fresh_market.ecom.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@Tag(name = "Products", description = "Manages products in the store")
@RestController
@RequestMapping("/api/public/products")
public class ProductController {
    private final IProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @Operation(summary = "Get all products")
    @GetMapping
    public List<Product> getProductService() {
        return productService.getAllProducts();
    }

    @Operation(summary = "Get a product by ID")
    @GetMapping("/{id}")
    public Product getProductById(@PathVariable UUID id) {
        return productService.getProductById(id);
    }

    @Operation(summary = "Create a new product")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Product createProduct(@RequestBody Product product) {
        return productService.createProduct(product);
    }

    @Operation(summary = "Update a product")
    @PutMapping("/{id}")
    public Product updateProduct(@PathVariable UUID id, @RequestBody Product product) {
        return productService.updateProduct(id, product);
    }

    @Operation(summary = "Delete a product")
    @DeleteMapping("/{id}")
    public void deleteProductById(@PathVariable UUID id) {
        productService.deleteProduct(id);
    }
}
