package com.hospital.hms.service;

import com.hospital.hms.auth.UserPrincipal;
import com.hospital.hms.domain.CanteenMenuItem;
import com.hospital.hms.domain.CanteenOrder;
import com.hospital.hms.domain.CanteenOrderLine;
import com.hospital.hms.domain.User;
import com.hospital.hms.domain.enums.PaymentMode;
import com.hospital.hms.repo.BillRepository;
import com.hospital.hms.repo.CanteenMenuItemRepository;
import com.hospital.hms.repo.CanteenOrderLineRepository;
import com.hospital.hms.repo.CanteenOrderRepository;
import com.hospital.hms.repo.UserRepository;
import com.hospital.hms.support.BillRefs;
import com.hospital.hms.web.error.ApiException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CanteenService {

    private final CanteenMenuItemRepository menuItemRepository;
    private final CanteenOrderRepository orderRepository;
    private final CanteenOrderLineRepository orderLineRepository;
    private final UserRepository userRepository;
    private final BillingService billingService;
    private final BillRepository billRepository;

    public CanteenService(CanteenMenuItemRepository menuItemRepository,
            CanteenOrderRepository orderRepository,
            CanteenOrderLineRepository orderLineRepository,
            UserRepository userRepository,
            BillingService billingService,
            BillRepository billRepository) {
        this.menuItemRepository = menuItemRepository;
        this.orderRepository = orderRepository;
        this.orderLineRepository = orderLineRepository;
        this.userRepository = userRepository;
        this.billingService = billingService;
        this.billRepository = billRepository;
    }

    @Transactional(readOnly = true)
    public List<CanteenMenuItem> menu() {
        return menuItemRepository.findAll().stream().filter(CanteenMenuItem::isAvailable).toList();
    }

    @Transactional
    public CanteenOrder placeOrder(UserPrincipal actor, Map<Long, Integer> menuItemIdToQty) {
        User u = userRepository.findById(actor.userId()).orElseThrow();
        if (menuItemIdToQty == null || menuItemIdToQty.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Empty order");
        }
        BigDecimal total = BigDecimal.ZERO;
        CanteenOrder order = new CanteenOrder();
        order.setPlacedBy(u);
        order.setStatus("PLACED");
        order.setPaymentStatus("UNPAID");
        order.setTotalAmount(BigDecimal.ZERO);
        order.setCreatedAt(Instant.now());
        order.setUpdatedAt(Instant.now());
        orderRepository.save(order);

        for (Map.Entry<Long, Integer> e : menuItemIdToQty.entrySet()) {
            if (e.getValue() == null || e.getValue() <= 0) {
                continue;
            }
            CanteenMenuItem item = menuItemRepository.findById(e.getKey())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Menu item " + e.getKey()));
            if (!item.isAvailable()) {
                throw new ApiException(HttpStatus.CONFLICT, "Item not available: " + item.getName());
            }
            BigDecimal lineTotal = item.getPrice().multiply(BigDecimal.valueOf(e.getValue()));
            total = total.add(lineTotal);
            CanteenOrderLine line = new CanteenOrderLine();
            line.setOrder(order);
            line.setMenuItem(item);
            line.setQuantity(e.getValue());
            line.setUnitPrice(item.getPrice());
            line.setCreatedAt(Instant.now());
            orderLineRepository.save(line);
        }
        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "No valid lines");
        }
        order.setTotalAmount(total);
        order.setUpdatedAt(Instant.now());
        orderRepository.save(order);

        billingService.createBill(u, BillRefs.CANTEEN, order.getId(), total, BigDecimal.ZERO, false);
        return orderRepository.findById(order.getId()).orElseThrow();
    }

    @Transactional
    public void payOrder(UserPrincipal payer, Long orderId, PaymentMode mode, String externalRef, Long receivedBy) {
        CanteenOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found"));
        var bill = billRepository.findByReferenceTypeAndReferenceId(BillRefs.CANTEEN, orderId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Bill not found"));
        billingService.payBill(payer, bill.getId(), mode, bill.getTotalAmount(), externalRef, receivedBy);
        order.setPaymentStatus("PAID");
        order.setStatus("FULFILLED");
        order.setUpdatedAt(Instant.now());
        orderRepository.save(order);
    }
}
