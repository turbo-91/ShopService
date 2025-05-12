package org.example;

import java.util.List;

public class ShopService {
    private final OrderListRepo orderRepo;
    private final ProductRepo productRepo;

    public ShopService(OrderListRepo orderRepo, ProductRepo productRepo) {
        this.orderRepo = orderRepo;
        this.productRepo = productRepo;
    }

    public Order placeOrder(String id, List<Product> products) {
        for (Product product : products) {
            // getProduct will throw NoSuchElementException if not found
            productRepo.getProduct(product.id());
        }

        final Order newOrder = new Order(id, products);
        orderRepo.addOrder(newOrder);
        return newOrder;
    }

}
