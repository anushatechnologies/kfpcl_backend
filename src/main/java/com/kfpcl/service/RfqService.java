package com.kfpcl.service;

import com.kfpcl.dto.CreateRfqRequest;
import com.kfpcl.dto.RfqResponse;

import java.util.List;

public interface RfqService {

    RfqResponse createRfq(CreateRfqRequest request);

    List<RfqResponse> getBuyerRfqs();

    RfqResponse getBuyerRfqById(String rfqId);

    RfqResponse cancelRfq(String rfqId);
}
