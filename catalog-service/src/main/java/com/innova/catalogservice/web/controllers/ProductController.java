package com.innova.catalogservice.web.controllers;

import com.innova.catalogservice.domain.PagedResult;
import com.innova.catalogservice.domain.Product;
import com.innova.catalogservice.domain.ProductNotFoundException;
import com.innova.catalogservice.domain.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/products")
class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    PagedResult<Product> getProducts(@RequestParam(name = "page", defaultValue = "1") int pageNo) {
        return productService.getProducts(pageNo);
    }

    @GetMapping("/{code}")
    ResponseEntity<Product> getProductBycode(@PathVariable String code) {
        return productService
                .getProductBycode(code)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> ProductNotFoundException.forCode(code));
    }
}
