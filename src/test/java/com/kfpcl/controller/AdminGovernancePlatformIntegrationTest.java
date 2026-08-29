package com.kfpcl.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kfpcl.dto.*;
import com.kfpcl.entity.*;
import com.kfpcl.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminGovernancePlatformIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SellerApplicationRepository sellerApplicationRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private com.kfpcl.repository.CategoryRepository categoryRepository;

    @Autowired
    private com.kfpcl.repository.SubcategoryRepository subcategoryRepository;

    private User buyerUser;
    private User sellerUser;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        if (!userRepository.existsByEmail("buyer_test@kfpcl.com")) {
            buyerUser = User.builder()
                    .id("user_buyer_test_1")
                    .name("Ramesh Farmer Buyer")
                    .email("buyer_test@kfpcl.com")
                    .phone("+91-9876500001")
                    .password("pass123")
                    .role(User.Role.BUYER)
                    .status(User.Status.ACTIVE)
                    .region("South Zone")
                    .build();
            userRepository.save(buyerUser);
        } else {
            buyerUser = userRepository.findByEmail("buyer_test@kfpcl.com").get();
        }

        if (!userRepository.existsByEmail("seller_test@kfpcl.com")) {
            sellerUser = User.builder()
                    .id("user_seller_test_1")
                    .name("Kisan Producer FPO")
                    .email("seller_test@kfpcl.com")
                    .phone("+91-9876500002")
                    .password("pass123")
                    .role(User.Role.SUPPLIER)
                    .status(User.Status.ACTIVE)
                    .region("South Zone")
                    .build();
            userRepository.save(sellerUser);
        } else {
            sellerUser = userRepository.findByEmail("seller_test@kfpcl.com").get();
        }

        if (brandRepository.existsById("brand_organic_india_test")) {
            brandRepository.deleteById("brand_organic_india_test");
        }
        brandRepository.findByNameIgnoreCase("Organic India Test").ifPresent(brandRepository::delete);

        // CREATE MISSING CATEGORY AND SUBCATEGORY TO SATISFY FOREIGN KEY CONSTRAINTS
        if (!categoryRepository.existsById("cat_dairy_test")) {
            categoryRepository.save(com.kfpcl.entity.Category.builder()
                    .id("cat_dairy_test")
                    .name("Dairy Test")
                    .status(com.kfpcl.entity.Category.Status.ACTIVE)
                    .build());
        }
        if (!subcategoryRepository.existsById("sub_milk_test")) {
            subcategoryRepository.save(com.kfpcl.entity.Subcategory.builder()
                    .id("sub_milk_test")
                    .categoryId("cat_dairy_test")
                    .name("Milk Test")
                    .status(com.kfpcl.entity.Subcategory.Status.ACTIVE)
                    .build());
        }

        testProduct = productRepository.findBySku("KFP-PROD-TEST-GOV").orElse(null);
        if (testProduct == null) {
            testProduct = Product.builder()
                    .id("prod_gov_test_1")
                    .productName("Premium Organic Pulses")
                    .categoryId("cat_dairy_test")
                    .subcategoryId("sub_milk_test")
                    .brand("KFPCL Brand")
                    .price(120.0)
                    .mrp(150.0)
                    .sku("KFP-PROD-TEST-GOV")
                    .status(Product.Status.ACTIVE)
                    .approvalStatus(Product.ApprovalStatus.PENDING)
                    .build();
            testProduct = productRepository.save(testProduct);
        } else {
            testProduct.setApprovalStatus(Product.ApprovalStatus.PENDING);
            testProduct.setRejectionReason(null);
            testProduct = productRepository.save(testProduct);
        }
    }

    @Test
    @DisplayName("Admin Users - List and Status Update")
    void testAdminUsersFlow() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users")
                        .param("role", "BUYER")
)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());

        UserStatusUpdateDto statusDto = UserStatusUpdateDto.builder()
                .status("ACTIVE")
                .build();

        mockMvc.perform(patch("/api/v1/admin/users/" + buyerUser.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusDto))
)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("Admin Sellers - Application Approval Flow")
    void testSellerApplicationWorkflow() throws Exception {
        String appId = "app_test_" + System.currentTimeMillis();
        SellerApplication app = SellerApplication.builder()
                .id(appId)
                .userId(sellerUser.getId())
                .businessName("Agri Co-op Hub")
                .businessType("FPO")
                .gstin("29ABCDE1234F1Z5")
                .status(SellerApplication.Status.PENDING)
                .build();
        sellerApplicationRepository.save(app);

        // 1. Get Application Details
        mockMvc.perform(get("/api/v1/admin/sellers/applications/" + appId)
)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.businessName").value("Agri Co-op Hub"));

        // 2. Approve Application
        mockMvc.perform(post("/api/v1/admin/sellers/applications/" + appId + "/approve")
)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }

    @Test
    @DisplayName("Admin Product Approval Flow")
    void testProductApprovalWorkflow() throws Exception {
        // 1. List Product Approvals (Pending)
        mockMvc.perform(get("/api/v1/admin/catalog/product-approvals")
                        .param("status", "PENDING")
)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 2. Approve Product
        mockMvc.perform(post("/api/v1/admin/catalog/product-approvals/" + testProduct.getId() + "/approve")
)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.approvalStatus").value("APPROVED"));
    }

    @Test
    @DisplayName("Brand Registry - CRUD and Duplicate Validation")
    void testBrandRegistryFlow() throws Exception {
        BrandCreateDto brandDto = BrandCreateDto.builder()
                .id("brand_organic_india_test")
                .name("Organic India Test")
                .website("https://organicindiatest.com")
                .description("Organic certified farm produce")
                .build();

        mockMvc.perform(post("/api/v1/admin/catalog/brands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(brandDto))
)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Organic India Test"));

        // Duplicate brand creation -> 409 Conflict
        mockMvc.perform(post("/api/v1/admin/catalog/brands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(brandDto))
)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));

        // List Brands
        mockMvc.perform(get("/api/v1/admin/catalog/brands")
)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("Admin Orders - Lifecycle, Tracking and Export")
    void testAdminOrdersFlow() throws Exception {
        String orderId = "ord_test_" + System.currentTimeMillis() + "_" + java.util.UUID.randomUUID().toString().substring(0, 8);
        Order order = Order.builder()
                .id(orderId)
                .orderNumber("KFP-ORD-" + System.currentTimeMillis() + "-" + java.util.UUID.randomUUID().toString().substring(0, 8))
                .buyerId(buyerUser.getId())
                .buyerName(buyerUser.getName())
                .sellerId(sellerUser.getId())
                .sellerName(sellerUser.getName())
                .totalAmount(5000.0)
                .discountAmount(200.0)
                .taxAmount(100.0)
                .finalAmount(4900.0)
                .paymentStatus(Order.PaymentStatus.PAID)
                .orderStatus(Order.OrderStatus.PENDING)
                .region("South Zone")
                .build();
        orderRepository.save(order);

        // 1. Get Order Details
        mockMvc.perform(get("/api/v1/admin/orders/" + orderId)
)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.finalAmount").value(4900.0));

        // 2. Update Order Status
        OrderStatusUpdateDto statusDto = OrderStatusUpdateDto.builder()
                .status("SHIPPED")
                .remarks("Dispatched via cold chain freight")
                .build();

        mockMvc.perform(patch("/api/v1/admin/orders/" + orderId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusDto))
)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderStatus").value("SHIPPED"));

        // 3. Add Tracking
        OrderTrackingCreateDto trackingDto = OrderTrackingCreateDto.builder()
                .carrier("KFPCL Logistics Express")
                .status("In Transit - Hub 4")
                .location("Bangalore Logistics Hub")
                .build();

        mockMvc.perform(post("/api/v1/admin/orders/" + orderId + "/tracking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(trackingDto))
)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.carrier").value("KFPCL Logistics Express"));

        // 4. Export CSV
        mockMvc.perform(get("/api/v1/admin/orders/export")
)
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Admin Dashboard and Analytics Endpoints")
    void testDashboardAndAnalytics() throws Exception {
        mockMvc.perform(get("/api/v1/admin/dashboard/summary")
)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalUsers").exists())
                .andExpect(jsonPath("$.data.totalProducts").exists());

        mockMvc.perform(get("/api/v1/admin/dashboard/sales-overview")
)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalSales").exists());

        mockMvc.perform(get("/api/v1/admin/analytics/overview")
                        .param("groupBy", "day")
)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.period").value("day"));
    }

    @Test
    @DisplayName("Support Ticket Creation, Admin Reply & Priority Update")
    void testSupportTicketFlow() throws Exception {
        SupportTicketCreateDto createDto = SupportTicketCreateDto.builder()
                .subject("Delay in Wheat Delivery")
                .description("Order #KFP-ORD-8821 shipment delayed by 2 days")
                .category("Delivery")
                .priority("HIGH")
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/support/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").exists())
                .andReturn();

        String ticketId = objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("id").asText();

        // Admin Reply
        TicketReplyCreateDto replyDto = TicketReplyCreateDto.builder()
                .message("We have escalated this with the regional cold chain logistics team.")
                .build();

        mockMvc.perform(post("/api/v1/admin/support/tickets/" + ticketId + "/reply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(replyDto))
)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").exists());
    }

    @Test
    @DisplayName("Platform Notifications - Create and Dispatch")
    void testNotificationFlow() throws Exception {
        NotificationCreateDto notifDto = NotificationCreateDto.builder()
                .title("Monsoon MSP Scheme Update")
                .message("New government MSP rates announced for Paddy and Pulses.")
                .audience("ALL")
                .channels("IN_APP,SMS")
                .scheduledAt(LocalDateTime.now())
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/admin/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notifDto))
)
                .andExpect(status().isCreated())
                .andReturn();

        String notifId = objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("id").asText();

        mockMvc.perform(post("/api/v1/admin/notifications/" + notifId + "/send")
)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DISPATCHED"));
    }

    @Test
    @DisplayName("Audit Logs - Query Mutations")
    void testAuditLogs() throws Exception {
        mockMvc.perform(get("/api/v1/admin/audit-logs")
)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("Shared Conversations and Messages Flow")
    void testConversationFlow() throws Exception {
        ConversationCreateDto convDto = ConversationCreateDto.builder()
                .recipientId(buyerUser.getId())
                .subject("Inquiry regarding Bulk Grain Contract")
                .message("Hello Ramesh, we have reviewed your RFQ submission.")
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/conversations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(convDto))
)
                .andExpect(status().isCreated())
                .andReturn();

        String convId = objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("id").asText();

        // Send additional message
        MessageCreateDto msgDto = MessageCreateDto.builder()
                .content("We can offer a special 5% discount for 500+ quintals.")
                .build();

        mockMvc.perform(post("/api/v1/conversations/" + convId + "/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(msgDto))
)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.content").exists());

        // Get Messages
        mockMvc.perform(get("/api/v1/conversations/" + convId + "/messages")
)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("Admin Buyers - List, Details, Activity and Status Update")
    void testAdminBuyersFlow() throws Exception {
        mockMvc.perform(get("/api/v1/admin/buyers")
)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());

        mockMvc.perform(get("/api/v1/admin/buyers/" + buyerUser.getId())
)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(buyerUser.getEmail()));

        mockMvc.perform(get("/api/v1/admin/buyers/" + buyerUser.getId() + "/activity")
)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        UserStatusUpdateDto statusDto = UserStatusUpdateDto.builder()
                .status("ACTIVE")
                .build();
        mockMvc.perform(patch("/api/v1/admin/buyers/" + buyerUser.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusDto))
)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("Admin Roles - Assign and Query User Roles")
    void testAdminRolesFlow() throws Exception {
        String dynamicRole = "ROLE_PREMIUM_" + System.currentTimeMillis();
        AssignUserRoleDto assignDto = AssignUserRoleDto.builder()
                .userId(buyerUser.getId())
                .role(dynamicRole)
                .build();

        mockMvc.perform(post("/api/v1/admin/roles/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignDto))
)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        mockMvc.perform(get("/api/v1/admin/roles/users")
                        .param("userId", buyerUser.getId())
)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("Admin Seller Stores & Verification")
    void testSellerStoresAndVerification() throws Exception {
        mockMvc.perform(get("/api/v1/admin/sellers/stores")
)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("Admin RFQ and Quotations Listing")
    void testRfqAndQuotationsListing() throws Exception {
        mockMvc.perform(get("/api/v1/admin/rfqs")
)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());

        mockMvc.perform(get("/api/v1/admin/quotations")
)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("Product Reviews - Create, Moderation & Buyer View")
    void testProductReviewsFlow() throws Exception {
        Product revProduct = Product.builder()
                .id("prod_rev_" + System.currentTimeMillis())
                .productName("Organic Pulses For Review")
                .categoryId("cat_dairy_test")
                .subcategoryId("sub_milk_test")
                .brand("KFPCL Brand")
                .price(100.0)
                .mrp(120.0)
                .sku("KFP-REV-" + System.currentTimeMillis())
                .status(Product.Status.ACTIVE)
                .approvalStatus(Product.ApprovalStatus.APPROVED)
                .build();
        revProduct = productRepository.save(revProduct);

        ReviewCreateDto reviewDto = ReviewCreateDto.builder()
                .rating(5)
                .comment("Extremely fresh produce direct from the farm cluster.")
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/products/" + revProduct.getId() + "/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewDto))
)
                .andExpect(status().isCreated())
                .andReturn();

        String reviewId = objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("id").asText();

        // Admin List Reviews
        mockMvc.perform(get("/api/v1/admin/reviews")
)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());

        // Admin Update Review Status (Approve)
        ReviewStatusUpdateDto updateStatusDto = ReviewStatusUpdateDto.builder()
                .status("APPROVED")
                .build();

        mockMvc.perform(patch("/api/v1/admin/reviews/" + reviewId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateStatusDto))
)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));

        // Buyer View Reviews
        mockMvc.perform(get("/api/v1/products/" + revProduct.getId() + "/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("Admin Profile & Platform Settings")
    void testAdminProfileAndSettings() throws Exception {
        mockMvc.perform(get("/api/v1/admin/profile")
)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").exists());

        AdminProfileUpdateDto profileDto = AdminProfileUpdateDto.builder()
                .name("KFPCL Senior Admin")
                .phone("+91-9876543210")
                .build();

        mockMvc.perform(patch("/api/v1/admin/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(profileDto))
)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("KFPCL Senior Admin"));

        java.util.Map<String, String> settingsMap = new java.util.HashMap<>();
        settingsMap.put("platformFee", "2.5");
        settingsMap.put("supportEmail", "support@kfpcl.org");

        PlatformSettingsUpdateDto settingsDto = PlatformSettingsUpdateDto.builder()
                .settings(settingsMap)
                .build();

        mockMvc.perform(patch("/api/v1/admin/settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(settingsDto))
)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Admin Detailed Analytics & Dashboard Breakdown")
    void testDetailedAnalyticsAndDashboard() throws Exception {
        mockMvc.perform(get("/api/v1/admin/dashboard/order-status-breakdown")
)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/v1/admin/dashboard/top-regions")
)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        mockMvc.perform(get("/api/v1/admin/dashboard/latest-sales")
)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        mockMvc.perform(get("/api/v1/admin/analytics/sales")
)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/v1/admin/analytics/products")
)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/v1/admin/analytics/users")
)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/v1/admin/analytics/regions")
)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
