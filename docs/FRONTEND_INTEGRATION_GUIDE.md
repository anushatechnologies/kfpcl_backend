# KFPCL Frontend Integration Guide & API Flow

This document provides frontend engineers (React, Next.js, Vue, Angular, or Mobile) with complete specifications, workflow diagrams, TypeScript interfaces, and endpoint details to integrate with the **KFPCL Catalog, Product, and Inventory Backend**.

---

## 🏗️ 1. Architecture & Workflow Diagrams

### Admin Flow: Image Upload ➡️ Category ➡️ Subcategory ➡️ Product & Inventory

```mermaid
sequenceDiagram
    autonumber
    actor Admin as Admin Frontend
    participant API as KFPCL Backend (Spring Boot)
    participant DB as MySQL Database
    participant FS as File System / CDN

    Note over Admin,FS: Step 1: Image Upload (Optional)
    Admin->>API: POST /api/v1/admin/catalog/images (Multipart file)
    API->>FS: Save image to /uploads/
    API-->>Admin: 201 Created { fileUrl: "/uploads/image.jpg" }

    Note over Admin,FS: Step 2: Category Creation
    Admin->>API: POST /api/v1/admin/catalog/categories { name, imageUrl, ... }
    API->>DB: INSERT into categories
    API-->>Admin: 201 Created { id: "cat_dairy", ... }

    Note over Admin,FS: Step 3: Subcategory Creation
    Admin->>API: POST /api/v1/admin/catalog/subcategories { categoryId, name, ... }
    API->>DB: INSERT into subcategories
    API-->>Admin: 201 Created { id: "sub_milk", ... }

    Note over Admin,FS: Step 4: Product Creation (Auto-creates Inventory)
    Admin->>API: POST /api/v1/admin/catalog/products { productName, categoryId, subcategoryId, price, mrp, stockQuantity, sku, ... }
    API->>DB: INSERT into products
    API->>DB: INSERT into inventories (stockQuantity = 50, status = 'IN_STOCK')
    API->>DB: INSERT into inventory_logs (adjustmentType = 'INITIAL')
    API-->>Admin: 201 Created { id: "prod_amul", discount: 6.25, ... }

    Note over Admin,FS: Step 5: Inventory Adjustments
    Admin->>API: POST /api/v1/admin/inventory/{inventoryId}/adjustment { type: "ADD", quantity: 20 }
    API->>DB: UPDATE inventories (stock = 70) & INSERT log
    API-->>Admin: 200 OK { stockQuantity: 70, status: "IN_STOCK" }
```

---

### Buyer Flow: Storefront Discovery & Search

```mermaid
sequenceDiagram
    autonumber
    actor Buyer as Buyer Frontend (Storefront)
    participant API as KFPCL Backend

    Note over Buyer,API: 1. Fetch Categories for Navigation
    Buyer->>API: GET /api/v1/admin/catalog/categories?status=ACTIVE
    API-->>Buyer: 200 OK [ { id: "cat_dairy", name: "Dairy", imageUrl: "..." } ]

    Note over Buyer,API: 2. Browse Category / Search Products
    Buyer->>API: GET /api/v1/catalog/products?categoryId=cat_dairy&search=Milk&inStock=true&page=0&size=20
    API-->>Buyer: 200 OK { content: [ ProductSummary... ], totalElements: 12, totalPages: 1 }

    Note over Buyer,API: 3. View Product Detail Page (PDP)
    Buyer->>API: GET /api/v1/catalog/products/{productId}
    API-->>Buyer: 200 OK { id, productName, price, mrp, discount, stockQuantity, status }
```

---

## 💻 2. TypeScript Data Interfaces

Frontend developers can copy and paste these TypeScript interfaces into their project:

```typescript
// Standard API Envelope
export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp?: string;
  error?: string;
}

// Paginated Response
export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

// Category
export interface Category {
  id: string;
  name: string;
  imageUrl?: string;
  description?: string;
  displayOrder: number;
  discount?: number;
  status: 'ACTIVE' | 'INACTIVE' | 'ARCHIVED';
  createdAt?: string;
  updatedAt?: string;
}

// Subcategory
export interface Subcategory {
  id: string;
  categoryId: string;
  categoryName?: string;
  name: string;
  imageUrl?: string;
  description?: string;
  displayOrder: number;
  discount?: number;
  status: 'ACTIVE' | 'INACTIVE' | 'ARCHIVED';
  createdAt?: string;
  updatedAt?: string;
}

// Product
export interface Product {
  id: string;
  productName: string;
  categoryId: string;
  categoryName?: string;
  subcategoryId: string;
  subcategoryName?: string;
  brand?: string;
  description?: string;
  imageUrl?: string;
  price: number;
  mrp: number;
  discount: number; // Server-calculated: ((mrp - price) / mrp) * 100
  quantity: number;
  unit: string;
  stockQuantity: number;
  status: 'ACTIVE' | 'INACTIVE' | 'OUT_OF_STOCK' | 'ARCHIVED';
  sku: string;
  createdAt?: string;
  updatedAt?: string;
}

// Inventory
export interface Inventory {
  id: string;
  productId: string;
  productName: string;
  sku: string;
  stockQuantity: number;
  reservedQuantity: number;
  availableQuantity: number;
  reorderLevel: number;
  status: 'IN_STOCK' | 'LOW_STOCK' | 'OUT_OF_STOCK';
  createdAt?: string;
  updatedAt?: string;
  recentLogs?: InventoryLog[];
}

// Inventory Audit Log
export interface InventoryLog {
  id: string;
  inventoryId: string;
  productId: string;
  adjustmentType: 'INITIAL' | 'ADD' | 'SUBTRACT' | 'SET' | 'DAMAGE' | 'RETURN' | 'SALE' | 'CORRECTION';
  quantity: number;
  previousQuantity: number;
  newQuantity: number;
  reason?: string;
  adjustedBy?: string;
  createdAt: string;
}
```

---

## 🛡️ 3. Frontend Form Validation Rules

| Field | Rule | Error Condition |
| :--- | :--- | :--- |
| **Product Price vs MRP** | `price <= mrp` | `422 Unprocessable Entity` (Selling price cannot exceed MRP) |
| **Price & MRP** | Positive numbers ($> 0$) | `400 Bad Request` |
| **Product SKU** | Unique alphanumeric | `409 Conflict` (Duplicate SKU detected) |
| **Category Name** | Unique string | `409 Conflict` (Category with name already exists) |
| **Stock Deduction** | `quantity <= currentStock` | `422 Unprocessable Entity` (Cannot reduce stock below 0) |

---

## 🚀 4. Ready-to-Use API Client Service (Axios / Fetch)

```typescript
import axios from 'axios';

const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api/v1',
  headers: {
    'Content-Type': 'application/json',
  },
});

export const CatalogApi = {
  // 1. Images
  uploadImage: async (file: File) => {
    const formData = new FormData();
    formData.append('file', file);
    const { data } = await api.post('/admin/catalog/images', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return data.data; // { fileUrl: string, fileName: string }
  },

  // 2. Categories
  createCategory: async (payload: { name: string; description?: string; imageUrl?: string; displayOrder?: number; discount?: number; isActive?: boolean }) => {
    const { data } = await api.post('/admin/catalog/categories', payload);
    return data.data;
  },
  getCategories: async (page = 0, size = 50) => {
    const { data } = await api.get(`/admin/catalog/categories?page=${page}&size=${size}&sortBy=displayOrder&sortDir=ASC`);
    return data.data;
  },

  // 3. Subcategories
  createSubcategory: async (payload: { categoryId: string; name: string; description?: string; imageUrl?: string; displayOrder?: number; isActive?: boolean }) => {
    const { data } = await api.post('/admin/catalog/subcategories', payload);
    return data.data;
  },
  getSubcategoriesByCategory: async (categoryId: string) => {
    const { data } = await api.get(`/admin/catalog/categories/${categoryId}/subcategories`);
    return data.data;
  },

  // 4. Products
  createProduct: async (payload: Omit<Product, 'id' | 'discount' | 'createdAt' | 'updatedAt'>) => {
    const { data } = await api.post('/admin/catalog/products', payload);
    return data.data;
  },
  getAdminProducts: async (page = 0, size = 10, search?: string) => {
    const params = new URLSearchParams({ page: String(page), size: String(size) });
    if (search) params.append('search', search);
    const { data } = await api.get(`/admin/catalog/products?${params.toString()}`);
    return data.data;
  },

  // 5. Inventory
  getInventoryList: async (page = 0, size = 10, search?: string) => {
    const params = new URLSearchParams({ page: String(page), size: String(size) });
    if (search) params.append('search', search);
    const { data } = await api.get(`/admin/inventory?${params.toString()}`);
    return data.data;
  },
  getInventoryById: async (inventoryId: string) => {
    const { data } = await api.get(`/admin/inventory/${inventoryId}`);
    return data.data;
  },
  adjustStock: async (inventoryId: string, payload: { adjustmentType: string; quantity: number; reason: string; adjustedBy: string }) => {
    const { data } = await api.post(`/admin/inventory/${inventoryId}/adjustment`, payload);
    return data.data;
  },

  // 6. Buyer Storefront
  getStoreProducts: async (filters: { categoryId?: string; search?: string; inStock?: boolean; minPrice?: number; maxPrice?: number; page?: number; size?: number }) => {
    const { data } = await api.get('/catalog/products', { params: filters });
    return data.data;
  },
  getStoreProductById: async (productId: string) => {
    const { data } = await api.get(`/catalog/products/${productId}`);
    return data.data;
  },
};
```
