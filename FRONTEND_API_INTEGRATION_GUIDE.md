# 🌱 KFPCL Agriculture Platform — Frontend Developer API & Integration Guide

**Production Domain (SSL / HTTPS):** `https://api.kfpclexports.com`  
**Live Remote AWS Server:** `http://18.61.70.201:8080`  
**Local Development Base URL:** `http://localhost:8080`  
**Interactive Swagger UI:** [http://18.61.70.201:8080/swagger-ui/index.html](http://18.61.70.201:8080/swagger-ui/index.html)  
**OpenAPI 3.0 Spec:** [http://18.61.70.201:8080/v3/api-docs](http://18.61.70.201:8080/v3/api-docs)

---

## 1. 📐 Standard Response Structure
Every response returned by the backend uses this uniform JSON envelope:
```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { ... },
  "error": null,
  "timestamp": "2026-08-26T15:00:00.000"
}
```

---

## 2. 🔐 Super Admin Authentication & Full Permissions
The Super Administrator (`admin@kfpcl.com`) has total overarching control and full access across all platform modules.

### POST `/api/v1/admin/auth/login`
```json
// Request Body
{
  "email": "admin@kfpcl.com",
  "password": "admin123"
}

// Response
{
  "success": true,
  "message": "Admin authenticated successfully",
  "data": {
    "userId": "user_admin_default",
    "name": "KFPCL Super Admin",
    "email": "admin@kfpcl.com",
    "role": "ADMIN",
    "permissions": [
      "ADMIN",
      "SUPER_ADMIN",
      "ADMIN_ALL",
      "ADMIN_USERS_READ",
      "ADMIN_USERS_WRITE",
      "ADMIN_USERS_CREATE",
      "ADMIN_USERS_UPDATE",
      "ADMIN_USERS_DELETE",
      "ADMIN_PRODUCTS_READ",
      "ADMIN_PRODUCTS_WRITE",
      "ADMIN_PRODUCTS_CREATE",
      "ADMIN_PRODUCTS_UPDATE",
      "ADMIN_PRODUCTS_EDIT",
      "ADMIN_PRODUCTS_DELETE",
      "ADMIN_PRODUCTS_APPROVE",
      "ADMIN_PRODUCTS_REJECT",
      "ADMIN_CATALOG_READ",
      "ADMIN_CATALOG_WRITE",
      "ADMIN_CATEGORIES_READ",
      "ADMIN_CATEGORIES_WRITE",
      "ADMIN_CATEGORIES_CREATE",
      "ADMIN_CATEGORIES_UPDATE",
      "ADMIN_CATEGORIES_DELETE",
      "ADMIN_SUBCATEGORIES_READ",
      "ADMIN_SUBCATEGORIES_WRITE",
      "ADMIN_SUBCATEGORIES_CREATE",
      "ADMIN_SUBCATEGORIES_UPDATE",
      "ADMIN_SUBCATEGORIES_DELETE",
      "ADMIN_BRANDS_READ",
      "ADMIN_BRANDS_WRITE",
      "ADMIN_BRANDS_CREATE",
      "ADMIN_BRANDS_UPDATE",
      "ADMIN_BRANDS_DELETE",
      "ADMIN_INVENTORY_READ",
      "ADMIN_INVENTORY_WRITE",
      "ADMIN_INVENTORY_UPDATE",
      "ADMIN_ORDERS_READ",
      "ADMIN_ORDERS_WRITE",
      "ADMIN_ORDERS_UPDATE",
      "ADMIN_ORDERS_DELETE",
      "ADMIN_SELLERS_READ",
      "ADMIN_SELLERS_WRITE",
      "ADMIN_SELLERS_APPROVE",
      "ADMIN_SELLERS_REJECT",
      "ADMIN_BUYERS_READ",
      "ADMIN_BUYERS_WRITE",
      "ADMIN_RFQS_READ",
      "ADMIN_RFQS_WRITE",
      "ADMIN_QUOTATIONS_READ",
      "ADMIN_QUOTATIONS_WRITE",
      "ADMIN_REVIEWS_READ",
      "ADMIN_REVIEWS_WRITE",
      "ADMIN_REVIEWS_APPROVE",
      "ADMIN_REVIEWS_REJECT",
      "ADMIN_SUPPORT_READ",
      "ADMIN_SUPPORT_WRITE",
      "ADMIN_SUPPORT_REPLY",
      "ADMIN_NOTIFICATIONS_READ",
      "ADMIN_NOTIFICATIONS_WRITE",
      "ADMIN_NOTIFICATIONS_SEND",
      "ADMIN_ANALYTICS_READ",
      "ADMIN_AUDIT_READ",
      "ADMIN_SETTINGS_READ",
      "ADMIN_SETTINGS_WRITE"
    ]
  }
}
```

---

## 3. 🛒 Products Workflow (Direct Admin Add vs Seller Approval Queue)

### A. Admin Creates Product (Direct Approval & Publishing)
When an admin creates a product via Admin Panel, it is **directly APPROVED and ACTIVE** with no approval step needed.

* **Method:** `POST`
* **URL:** `/api/v1/admin/catalog/products`
```json
// Request Body
{
  "productName": "Kisan Pride Sharbati Wheat 10kg",
  "categoryId": "cat_grains",
  "subcategoryId": "sub_wheat",
  "brand": "Kisan Pride Organics",
  "description": "Farm harvested Sharbati wheat",
  "imageUrl": "/uploads/catalog/wheat-10kg.jpg",
  "price": 450.0,
  "mrp": 500.0,
  "quantity": 10.0,
  "unit": "kg",
  "stockQuantity": 100,
  "status": "ACTIVE",
  "sku": "KFP-SHARBATI-10KG"
}

// Result: approvalStatus = "APPROVED", status = "ACTIVE"
```

### B. Admin Edits / Updates Product (Supports both PUT & PATCH)
* **Method:** `PUT` or `PATCH`
* **URL:** `/api/v1/admin/catalog/products/{productId}`
```json
{
  "productName": "Kisan Pride Sharbati Wheat 10kg (Premium)",
  "price": 440.0,
  "mrp": 500.0,
  "status": "ACTIVE"
}
```

---

### C. Seller Submits Product for Approval
When a seller adds a product, it is saved as **`PENDING` and `INACTIVE`** for Admin review.

* **Method:** `POST`
* **URL:** `/api/v1/seller/catalog/products`
```json
// Request Body
{
  "sellerId": "user_seller_123",
  "productName": "Organic Mustard Oil 1L",
  "categoryId": "cat_oils",
  "subcategoryId": "sub_mustard_oil",
  "brand": "Gramin Agro",
  "description": "Cold pressed organic mustard oil",
  "imageUrl": "/uploads/catalog/oil-1l.jpg",
  "price": 180.0,
  "mrp": 210.0,
  "quantity": 1.0,
  "unit": "L",
  "stockQuantity": 50,
  "sku": "GRM-OIL-1L"
}

// Result: approvalStatus = "PENDING", status = "INACTIVE"
```

---

### D. Admin Approves / Rejects Seller Products
Admin reviews pending submissions and approves or rejects:

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/v1/admin/catalog/product-approvals?status=PENDING` | List all pending submissions |
| `GET` | `/api/v1/admin/catalog/product-approvals/{id}` | View single submission details |
| `POST` | `/api/v1/admin/catalog/product-approvals/{id}/approve` | **Approve product** (sets status to ACTIVE) |
| `POST` | `/api/v1/admin/catalog/product-approvals/{id}/reject` | **Reject product** with reason |

```json
// Approve Request (POST /api/v1/admin/catalog/product-approvals/{id}/approve)
// Optional body: { "note": "Approved after quality verification" }

// Reject Request (POST /api/v1/admin/catalog/product-approvals/{id}/reject)
{
  "reason": "Missing nutritional label or FSSAI certificate"
}
```

---

## 4. 📂 Master Categories & Subcategories (PUT & PATCH Supported)

### Categories:
* `GET /api/v1/admin/catalog/categories`
* `POST /api/v1/admin/catalog/categories`
* `PUT /api/v1/admin/catalog/categories/{id}`
* `PATCH /api/v1/admin/catalog/categories/{id}`
* `DELETE /api/v1/admin/catalog/categories/{id}`

### Subcategories:
* `GET /api/v1/admin/catalog/subcategories`
* `GET /api/v1/admin/catalog/categories/{catId}/subcategories`
* `POST /api/v1/admin/catalog/subcategories`
* `PUT /api/v1/admin/catalog/subcategories/{id}`
* `PATCH /api/v1/admin/catalog/subcategories/{id}`
* `DELETE /api/v1/admin/catalog/subcategories/{id}`

---

## 5. 🖼️ Catalog Image Upload (Multipart)
* **Method:** `POST`
* **URL:** `/api/v1/admin/catalog/images`
* **Content-Type:** `multipart/form-data`
* **Form Field:** `file`
```typescript
const formData = new FormData();
formData.append('file', imageFile);

const res = await apiClient.post('/api/v1/admin/catalog/images', formData, {
  headers: { 'Content-Type': 'multipart/form-data' }
});
// Returns: { "data": { "fileUrl": "/uploads/catalog/img_xxx.jpg" } }
```

---

## 6. 🌐 Frontend Axios Client Configuration
```typescript
import axios from 'axios';

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'https://api.kfpclexports.com',
  headers: {
    'Content-Type': 'application/json',
  },
});

apiClient.interceptors.response.use(
  (response) => response.data,
  (error) => {
    const errorMsg = error.response?.data?.message || error.message || 'API request failed';
    return Promise.reject(new Error(errorMsg));
  }
);
```
