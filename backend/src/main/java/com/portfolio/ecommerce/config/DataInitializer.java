package com.portfolio.ecommerce.config;

import com.portfolio.ecommerce.entity.*;
import com.portfolio.ecommerce.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (roleRepository.count() == 0) {
            log.info("Seeding E-commerce roles...");
            Role adminRole = roleRepository.save(Role.builder().name(RoleType.ROLE_ADMIN).build());
            Role customerRole = roleRepository.save(Role.builder().name(RoleType.ROLE_CUSTOMER).build());

            log.info("Seeding Admin user (admin@store.com)...");
            User admin = User.builder()
                    .fullName("Store Administrator")
                    .email("admin@store.com")
                    .password(passwordEncoder.encode("AdminPassword@123"))
                    .phone("+1-555-0200")
                    .role(adminRole)
                    .build();
            userRepository.save(admin);

            log.info("Seeding Customer user (customer@store.com)...");
            User customer = User.builder()
                    .fullName("Praveen Kumar")
                    .email("customer@store.com")
                    .password(passwordEncoder.encode("CustomerPassword@123"))
                    .phone("+1-555-0201")
                    .address("452 Tech Avenue, Suite 100, San Francisco, CA")
                    .role(customerRole)
                    .build();
            User savedCustomer = userRepository.save(customer);

            // Create cart for demo customer
            Cart cart = Cart.builder().user(savedCustomer).build();
            cartRepository.save(cart);

            log.info("Seeding Categories...");
            Category electronics = categoryRepository.save(Category.builder().name("Electronics").description("High performance devices and peripherals").build());
            Category audio = categoryRepository.save(Category.builder().name("Audio & Sound").description("Studio monitors, wireless headsets, and audiophile gear").build());
            Category wearables = categoryRepository.save(Category.builder().name("Wearables").description("Smartwatches, fitness bands, and trackers").build());
            Category accessories = categoryRepository.save(Category.builder().name("Accessories").description("Mechanical keyboards, mice, and desk accessories").build());

            log.info("Seeding Products...");
            Product p1 = productRepository.save(Product.builder()
                    .name("Pro Wireless Noise-Cancelling Headphones")
                    .description("Active hybrid noise cancellation with 40-hour battery life and spatial audio driver architecture.")
                    .price(new BigDecimal("299.99"))
                    .stockQuantity(35)
                    .imageUrl("https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500")
                    .category(audio)
                    .active(true)
                    .build());

            Product p2 = productRepository.save(Product.builder()
                    .name("Mechanical RGB Ergonomic Keyboard")
                    .description("Hot-swappable linear mechanical switches, gasket-mounted aluminum chassis, and PBT double-shot keycaps.")
                    .price(new BigDecimal("149.50"))
                    .stockQuantity(50)
                    .imageUrl("https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=500")
                    .category(accessories)
                    .active(true)
                    .build());

            Product p3 = productRepository.save(Product.builder()
                    .name("Ultra-Thin 4K IPS USB-C Monitor 27\"")
                    .description("Color-calibrated 99% DCI-P3 display with 90W power delivery and anti-glare coating.")
                    .price(new BigDecimal("499.00"))
                    .stockQuantity(18)
                    .imageUrl("https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=500")
                    .category(electronics)
                    .active(true)
                    .build());

            Product p4 = productRepository.save(Product.builder()
                    .name("Titanium GPS Smart Fitness Watch")
                    .description("Dual-frequency satellite navigation, optical heart-rate sensor, and sapphire crystal lens.")
                    .price(new BigDecimal("349.99"))
                    .stockQuantity(4) // Low stock indicator test
                    .imageUrl("https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=500")
                    .category(wearables)
                    .active(true)
                    .build());

            log.info("Seeding Sample Order for Customer...");
            Order sampleOrder = Order.builder()
                    .orderNumber("ORD-DEMO-9001")
                    .user(savedCustomer)
                    .shippingAddress(savedCustomer.getAddress())
                    .status(OrderStatus.CONFIRMED)
                    .paymentNotice("Payment integration not configured")
                    .totalAmount(new BigDecimal("449.49"))
                    .createdAt(LocalDateTime.now().minusDays(1))
                    .build();

            OrderItem item1 = OrderItem.builder()
                    .order(sampleOrder)
                    .product(p1)
                    .quantity(1)
                    .unitPrice(p1.getPrice())
                    .subtotal(p1.getPrice())
                    .build();

            OrderItem item2 = OrderItem.builder()
                    .order(sampleOrder)
                    .product(p2)
                    .quantity(1)
                    .unitPrice(p2.getPrice())
                    .subtotal(p2.getPrice())
                    .build();

            sampleOrder.setItems(List.of(item1, item2));
            orderRepository.save(sampleOrder);

            log.info("E-commerce seed initialization completed successfully.");
        }
    }
}
