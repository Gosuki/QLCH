package com.example.projectqlch.Controlller;

import com.example.projectqlch.Service.ProductService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/product")
@AllArgsConstructor
public class ProductController {
    private final ProductService productService;

    @PostMapping("/create")
    public ResponseEntity<?> createProduct(@RequestParam String name,
                                           @RequestParam String description,
                                           @RequestParam double price,
                                           @RequestParam int quantity,
                                           @RequestParam MultipartFile image) throws IOException {
        return ResponseEntity.ok(productService.createProduct(name, description, price, quantity, image));
    }

    @GetMapping("/{id}/{fileName}")
    public ResponseEntity<?> downloadImage(@PathVariable String fileName,@PathVariable Long id) throws IOException {
        byte[] imageData=productService.downloadProductImage(fileName,id);
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.valueOf("image/png"))
                .body(imageData);

    }

}
