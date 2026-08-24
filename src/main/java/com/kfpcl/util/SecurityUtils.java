package com.kfpcl.util;

import com.kfpcl.entity.Buyer;
import com.kfpcl.entity.User;
import com.kfpcl.exception.ResourceNotFoundException;
import com.kfpcl.repository.BuyerRepository;
import com.kfpcl.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final BuyerRepository buyerRepository;
    private final UserRepository userRepository;

    public Buyer getCurrentBuyer() {
        return buyerRepository.findById("buyer_1")
                .or(() -> buyerRepository.findAll().stream().findFirst())
                .orElseThrow(() -> new ResourceNotFoundException("No active buyer profile found in database"));
    }

    public String getCurrentBuyerId() {
        return getCurrentBuyer().getId();
    }

    public User getCurrentUser() {
        return getCurrentBuyer().getUser();
    }
}
