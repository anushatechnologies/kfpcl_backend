package com.kfpcl.config;

import com.kfpcl.entity.*;
import com.kfpcl.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import java.math.BigDecimal;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final BuyerRepository buyerRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final RfqRepository rfqRepository;
    private final QuotationRepository quotationRepository;

    @Override
    public void run(String... args) {
        try {
            initCategories();
            initUsersAndProfiles();
            initProducts();
            initSampleRfqAndQuotations();
            log.info("Sample database initialization completed successfully.");
        } catch (Exception ex) {
            log.warn("Database initialization skipped or already present: {}", ex.getMessage());
        }
    }

    private void initSampleRfqAndQuotations() {
        if (rfqRepository.count() == 0) {
            Buyer buyer = buyerRepository.findById("buyer_1").orElse(null);
            Product product = productRepository.findById("prod_1").orElse(null);
            Category category = categoryRepository.findById("cat_dairy").orElse(null);
            Supplier supplier = supplierRepository.findById("supp_1").orElse(null);

            if (buyer != null && product != null && category != null && supplier != null) {
                Rfq rfq = rfqRepository.save(Rfq.builder()
                        .id("rfq_1")
                        .buyer(buyer)
                        .product(product)
                        .productTitle(product.getTitle())
                        .category(category)
                        .quantity(500)
                        .unit("liter")
                        .targetPrice(BigDecimal.valueOf(26.50))
                        .expectedDeliveryDate(java.time.LocalDate.now().plusDays(15))
                        .description("Need regular weekly supply for dairy distribution.")
                        .status(Rfq.Status.QUOTATIONS_RECEIVED)
                        .build());

                quotationRepository.save(Quotation.builder()
                        .id("quot_1")
                        .rfq(rfq)
                        .supplier(supplier)
                        .quotedPrice(BigDecimal.valueOf(26.00))
                        .quantity(500)
                        .leadTimeDays(3)
                        .validUntil(java.time.LocalDate.now().plusDays(10))
                        .notes("Special wholesale pricing with doorstep cold-chain delivery included.")
                        .status(Quotation.Status.PENDING)
                        .build());
            }
        }
    }

    private void initCategories() {
        if (categoryRepository.count() == 0) {
            categoryRepository.save(new Category("cat_dairy", "Dairy & Poultry", Category.Status.ACTIVE));
            categoryRepository.save(new Category("cat_grains", "Grains & Pulses", Category.Status.ACTIVE));
            categoryRepository.save(new Category("cat_spices", "Spices & Condiments", Category.Status.ACTIVE));
            categoryRepository.save(new Category("cat_beverages", "Beverages", Category.Status.INACTIVE));
        }
    }

    private void initUsersAndProfiles() {
        // Buyer User
        userRepository.findByEmail("buyer@kfpcl.com").ifPresentOrElse(user -> {
            user.setPassword("password123");
            userRepository.save(user);
        }, () -> {
            User buyerUser = userRepository.save(User.builder()
                    .id("usr_buyer_1")
                    .email("buyer@kfpcl.com")
                    .password("password123")
                    .name("Rajesh Kumar")
                    .phone("+919876543210")
                    .role(Role.ROLE_BUYER)
                    .build());

            buyerRepository.save(Buyer.builder()
                    .id("buyer_1")
                    .user(buyerUser)
                    .companyName("KFPCL Supermarkets Ltd")
                    .businessType("Retail Chain")
                    .gstNumber("27AAAAA0000A1Z5")
                    .address("123 Market Yard, Pune, Maharashtra 411037")
                    .build());
        });

        // Supplier 1
        userRepository.findByEmail("supplier@amul.com").ifPresentOrElse(user -> {
            user.setPassword("password123");
            userRepository.save(user);
        }, () -> {
            User supplierUser1 = userRepository.save(User.builder()
                    .id("usr_supp_1")
                    .email("supplier@amul.com")
                    .password("password123")
                    .name("Amul Sales Representative")
                    .phone("+919876500001")
                    .role(Role.ROLE_SUPPLIER)
                    .build());

            supplierRepository.save(Supplier.builder()
                    .id("supp_1")
                    .user(supplierUser1)
                    .companyName("Amul Dairy India")
                    .gstNumber("24AAACA2144K1ZT")
                    .gstVerified(true)
                    .isVerified(true)
                    .contactEmail("supplier@amul.com")
                    .contactPhone("+919876500001")
                    .address("Anand, Gujarat 388001")
                    .build());
        });

        // Supplier 2
        userRepository.findByEmail("supplier@tata.com").ifPresentOrElse(user -> {
            user.setPassword("password123");
            userRepository.save(user);
        }, () -> {
            User supplierUser2 = userRepository.save(User.builder()
                    .id("usr_supp_2")
                    .email("supplier@tata.com")
                    .password("password123")
                    .name("Tata Consumer Rep")
                    .phone("+919876500002")
                    .role(Role.ROLE_SUPPLIER)
                    .build());

            supplierRepository.save(Supplier.builder()
                    .id("supp_2")
                    .user(supplierUser2)
                    .companyName("Tata Consumer Products Ltd")
                    .gstNumber("27AAACT2727Q1ZW")
                    .gstVerified(true)
                    .isVerified(true)
                    .contactEmail("supplier@tata.com")
                    .contactPhone("+919876500002")
                    .address("Fort, Mumbai, Maharashtra 400001")
                    .build());
        });
    }

    private void initProducts() {
        if (productRepository.count() == 0) {
            Category dairy = categoryRepository.findById("cat_dairy").orElse(null);
            Category grains = categoryRepository.findById("cat_grains").orElse(null);
            Category spices = categoryRepository.findById("cat_spices").orElse(null);

            Supplier amul = supplierRepository.findById("supp_1").orElse(null);
            Supplier tata = supplierRepository.findById("supp_2").orElse(null);

            if (dairy != null && amul != null) {
                productRepository.save(Product.builder()
                        .id("prod_1")
                        .title("Amul Taaza Toned Milk 1L")
                        .description("Fresh homogenized toned milk pouch with 3.0% fat and 8.5% SNF.")
                        .category(dairy)
                        .supplier(amul)
                        .price(BigDecimal.valueOf(28.00))
                        .unit("liter")
                        .moq(50)
                        .stockQuantity(1000)
                        .featured(true)
                        .status(Product.Status.ACTIVE)
                        .imageUrl("https://images.unsplash.com/photo-1550583724-b2692b85b150?w=600")
                        .gstRate(BigDecimal.valueOf(5.0))
                        .build());

                productRepository.save(Product.builder()
                        .id("prod_2")
                        .title("Amul Pasteurised Butter 500g")
                        .description("Delicious pure dairy butter made from fresh cream.")
                        .category(dairy)
                        .supplier(amul)
                        .price(BigDecimal.valueOf(250.00))
                        .unit("piece")
                        .moq(20)
                        .stockQuantity(500)
                        .featured(true)
                        .status(Product.Status.ACTIVE)
                        .imageUrl("https://images.unsplash.com/photo-1589985270826-4b7bb135bc9d?w=600")
                        .gstRate(BigDecimal.valueOf(12.0))
                        .build());
            }

            if (grains != null && tata != null) {
                productRepository.save(Product.builder()
                        .id("prod_3")
                        .title("Tata Sampann Unpolished Toor Dal 1kg")
                        .description("Rich in natural protein, unpolished toor dal without water or oil polish.")
                        .category(grains)
                        .supplier(tata)
                        .price(BigDecimal.valueOf(165.00))
                        .unit("kg")
                        .moq(100)
                        .stockQuantity(2000)
                        .featured(false)
                        .status(Product.Status.ACTIVE)
                        .imageUrl("https://images.unsplash.com/photo-1586201375761-83865001e31c?w=600")
                        .gstRate(BigDecimal.valueOf(5.0))
                        .build());
            }

            if (spices != null && tata != null) {
                productRepository.save(Product.builder()
                        .id("prod_4")
                        .title("Tata Salt Vacuum Evaporated Iodised Salt 1kg")
                        .description("India's first vacuum evaporated iodised salt with guaranteed purity.")
                        .category(spices)
                        .supplier(tata)
                        .price(BigDecimal.valueOf(25.00))
                        .unit("kg")
                        .moq(200)
                        .stockQuantity(5000)
                        .featured(true)
                        .status(Product.Status.ACTIVE)
                        .imageUrl("https://images.unsplash.com/photo-1518110925495-5fe2fda0442c?w=600")
                        .gstRate(BigDecimal.valueOf(0.0))
                        .build());
            }
        }
    }
}
