package com.n11bootcamp.product.web;

import com.n11bootcamp.product.domain.Product;
import com.n11bootcamp.product.domain.ProductRepository;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Component
@Profile("!test")
public class CatalogDataLoader implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(CatalogDataLoader.class);
    private final ProductRepository productRepository;

    public CatalogDataLoader(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (productRepository.count() > 0){
            return;
        }
        List<Product> products = new ArrayList<>();
        for (int i = 0; i <= 30; i++){
            Product p = new Product();
            p.setSku("DEMO-SKU-" + String.format("%03d", i));
            p.setName("Demo ürün " + i);
            p.setDescription("Bitirme projesi için örnek ürün açıklaması #" + i);
            p.setPrice(BigDecimal.valueOf(49.90 + i));
            p.setStockQuantity(100 - i);
            p.setImageUrl(
                    "https://picsum.photos/seed/commerce-demo-" + String.format("%03d", i) + "/640/480");
            products.add(p);
        }
        productRepository.saveAll(products);
        log.info("Seed {} demo products",products.size());
    }
}
