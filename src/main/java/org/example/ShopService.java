package org.example;

import java.math.BigDecimal;
import java.util.List;

public class ShopService {
    private final OrderRepo orderRepo;
    private final ProductRepo productRepo;

    public ShopService(OrderRepo orderRepo, ProductRepo productRepo) {
        this.orderRepo = orderRepo;
        this.productRepo = productRepo;
    }

    public Order placeOrder(String id, List<Product> products, BigDecimal totalPrice) {
        for (Product product : products) {
            // getProduct will throw NoSuchElementException if not found
            productRepo.getProduct(product.id());
        }

        final Order newOrder = new Order(id, products, totalPrice);
        orderRepo.addOrder(newOrder);
        return newOrder;
    }

}
