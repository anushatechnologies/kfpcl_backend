package com.kfpcl.service;

import com.kfpcl.entity.Rfq;

public interface SupplierNotificationService {

    void notifyEligibleSuppliers(Rfq rfq);
}
