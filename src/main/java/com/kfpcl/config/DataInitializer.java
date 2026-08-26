package com.kfpcl.config;

import com.kfpcl.entity.*;
import com.kfpcl.entity.enums.Role;
import com.kfpcl.entity.enums.VerificationStatus;
import com.kfpcl.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final SellerRepository sellerRepository;
    private final BuyerRepository buyerRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        initCategories();
        initSellers();
        initBuyers();
    }

    private void initCategories() {
        if (categoryRepository.count() == 0) {
            Category grains = Category.builder()
                    .name("Grains & Pulses")
                    .slug("grains-pulses")
                    .description("Wholesale grains, cereals, pulses, and lentils directly from FPOs")
                    .displayOrder(1)
                    .isActive(true)
                    .build();
            categoryRepository.save(grains);

            Category wheat = Category.builder()
                    .name("Wheat & Millets")
                    .slug("wheat-millets")
                    .description("Milling wheat, durum, bajra, and organic millets")
                    .parent(grains)
                    .displayOrder(1)
                    .isActive(true)
                    .build();
            categoryRepository.save(wheat);

            Category spices = Category.builder()
                    .name("Spices & Condiments")
                    .slug("spices-condiments")
                    .description("Whole and powdered spices directly sourced from origin farmers")
                    .displayOrder(2)
                    .isActive(true)
                    .build();
            categoryRepository.save(spices);

            log.info("Initialized default categories.");
        }
    }

    private void initSellers() {
        // Seller 1
        if (userRepository.findByEmail("seller@kfpcl.com").isEmpty()) {
            User user1 = User.builder()
                    .email("seller@kfpcl.com")
                    .password(passwordEncoder.encode("password123"))
                    .fullName("Kisan Producer FPO")
                    .phoneNumber("+919876543210")
                    .role(Role.ROLE_SELLER)
                    .isActive(true)
                    .build();
            userRepository.save(user1);

            Seller seller1 = Seller.builder()
                    .user(user1)
                    .companyName("Kisan Agri Producer Co. Ltd.")
                    .taxId("27AAACK1234M1Z5")
                    .businessRegistrationNumber("U01111MH2020PTC123456")
                    .address("Plot 45, APMC Market Yard")
                    .city("Nashik")
                    .state("Maharashtra")
                    .country("India")
                    .postalCode("422003")
                    .description("Certified Producer Company supplying high quality certified grains and pulses.")
                    .isVerified(true)
                    .verificationStatus(VerificationStatus.VERIFIED)
                    .rating(4.8)
                    .totalReviews(124)
                    .build();
            sellerRepository.save(seller1);

            log.info("Initialized default verified seller: seller@kfpcl.com (ID: {})", seller1.getId());
        }

        // Seller 2
        if (userRepository.findByEmail("seller2@kfpcl.com").isEmpty()) {
            User user2 = User.builder()
                    .email("seller2@kfpcl.com")
                    .password(passwordEncoder.encode("password123"))
                    .fullName("Sahyadri FPO Admin")
                    .phoneNumber("+919876543211")
                    .role(Role.ROLE_SELLER)
                    .isActive(true)
                    .build();
            userRepository.save(user2);

            Seller seller2 = Seller.builder()
                    .user(user2)
                    .companyName("Sahyadri Farmers Producer Co.")
                    .taxId("27AABCS5678N1Z2")
                    .businessRegistrationNumber("U01112MH2019PTC654321")
                    .address("Gat No 102, Mohadi, Dindori")
                    .city("Nashik")
                    .state("Maharashtra")
                    .country("India")
                    .postalCode("422207")
                    .description("Large scale agricultural cooperative specializing in bulk grain aggregation.")
                    .isVerified(true)
                    .verificationStatus(VerificationStatus.VERIFIED)
                    .rating(4.9)
                    .totalReviews(89)
                    .build();
            sellerRepository.save(seller2);

            log.info("Initialized default verified seller 2: seller2@kfpcl.com (ID: {})", seller2.getId());
        }
    }

    private void initBuyers() {
        if (userRepository.findByEmail("buyer@kfpcl.com").isEmpty()) {
            User buyerUser = User.builder()
                    .email("buyer@kfpcl.com")
                    .password(passwordEncoder.encode("password123"))
                    .fullName("Rajesh Verma")
                    .phoneNumber("+919812345678")
                    .role(Role.ROLE_BUYER)
                    .isActive(true)
                    .build();
            userRepository.save(buyerUser);

            Buyer buyer = Buyer.builder()
                    .user(buyerUser)
                    .companyName("Metro Foods Pvt Ltd")
                    .contactPerson("Rajesh Verma")
                    .businessType("Food Processor / Miller")
                    .shippingAddress("Flour Mills Compound, Pune, Maharashtra - 411028")
                    .city("Pune")
                    .state("Maharashtra")
                    .country("India")
                    .postalCode("411028")
                    .build();
            buyerRepository.save(buyer);

            log.info("Initialized default buyer: buyer@kfpcl.com (ID: {})", buyer.getId());
        }
    }
}
