package com.n11bootcamp.payment.service;

import com.iyzipay.Options;
import com.iyzipay.model.*;
import com.iyzipay.request.CreateCheckoutFormInitializeRequest;
import com.iyzipay.request.RetrieveCheckoutFormRequest;
import com.n11bootcamp.common.exception.InvalidOrderStateException;
import com.n11bootcamp.common.exception.IyzicoIntegrationException;
import com.n11bootcamp.common.exception.OrderNotFoundException;
import com.n11bootcamp.common.exception.PaymentProviderUnavailableException;
import com.n11bootcamp.payment.config.IyzicoProperties;
import com.n11bootcamp.payment.dto.PaymentInitializeResponse;
import com.n11bootcamp.payment.integration.MarkPaidBody;
import com.n11bootcamp.payment.integration.OrderDetailDto;
import com.n11bootcamp.payment.integration.OrderDetailDto.OrderLineDto;
import com.n11bootcamp.payment.integration.PaymentIntegrationClient;
import com.n11bootcamp.payment.integration.UserProfileDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final IyzicoProperties iyzicoProperties;
    private final PaymentIntegrationClient integration;

    public PaymentService(IyzicoProperties iyzicoProperties, PaymentIntegrationClient integration) {
        this.iyzicoProperties = iyzicoProperties;
        this.integration = integration;
    }

    public PaymentInitializeResponse initializeCheckout(Long userId, Long orderId, String authorizationHeader) {
        ensureIyzicoReady();
        OrderDetailDto order = fetchOrder(orderId, authorizationHeader);
        if (!"CREATED".equals(order.status())) {
            throw new InvalidOrderStateException("Sipariş ödeme için uygun değil: " + order.status());
        }

        UserProfileDto me = integration.getMe(authorizationHeader);

        Options options = buildOptions();
        String conversationId = "commerce-order-" + order.id();
        integration.patchCheckoutMetadata(orderId, userId, conversationId, null);

        CreateCheckoutFormInitializeRequest request = new CreateCheckoutFormInitializeRequest();
        request.setLocale(Locale.TR.getValue());
        request.setConversationId(conversationId);
        request.setPrice(order.totalAmount());
        request.setPaidPrice(order.totalAmount());
        request.setCurrency(Currency.TRY.name());
        request.setBasketId("BASKET-" + order.id());
        request.setPaymentGroup(PaymentGroup.PRODUCT.name());
        request.setCallbackUrl(iyzicoProperties.callbackUrl());
        request.setBuyer(buildBuyer(me));
        request.setShippingAddress(buildAddress(me.email()));
        request.setBillingAddress(buildAddress(me.email()));
        request.setBasketItems(buildBasket(order.lines()));

        CheckoutFormInitialize init = CheckoutFormInitialize.create(request, options);
        if (!Status.SUCCESS.getValue().equalsIgnoreCase(init.getStatus())) {
            throw new IyzicoIntegrationException(
                    "İyzico initialize başarısız: " + init.getErrorCode() + " " + init.getErrorMessage());
        }
        if (!init.verifySignature(iyzicoProperties.secretKey())) {
            throw new IyzicoIntegrationException("İyzico yanıt imzası doğrulanamadı");
        }

        integration.patchCheckoutMetadata(orderId, userId, conversationId, init.getToken());

        log.info("Iyzico checkout initialized orderId={} conversationId={}", orderId, conversationId);
        return new PaymentInitializeResponse(
                init.getToken(), init.getPaymentPageUrl(), init.getCheckoutFormContent(), conversationId);
    }

    public void handleCheckoutCallback(String token) {
        ensureIyzicoReady();
        Options options = buildOptions();

        RetrieveCheckoutFormRequest retrieveRequest = new RetrieveCheckoutFormRequest();
        retrieveRequest.setLocale(Locale.TR.getValue());
        retrieveRequest.setToken(token);

        CheckoutForm form = CheckoutForm.retrieve(retrieveRequest, options);
        if (!Status.SUCCESS.getValue().equalsIgnoreCase(form.getStatus())) {
            log.warn(
                    "Iyzico retrieve non-success status={} error={}",
                    form.getStatus(),
                    form.getErrorMessage());
            throw new IyzicoIntegrationException(
                    "İyzico callback doğrulama başarısız: " + form.getErrorCode() + " " + form.getErrorMessage());
        }
        if (!form.verifySignature(iyzicoProperties.secretKey())) {
            throw new IyzicoIntegrationException("İyzico callback imzası geçersiz");
        }

        BigDecimal paid = form.getPaidPrice() != null ? form.getPaidPrice() : BigDecimal.ZERO;
        integration.markPaid(new MarkPaidBody(form.getConversationId(), paid, form.getPaymentStatus()));
    }

    private OrderDetailDto fetchOrder(Long orderId, String authorizationHeader) {
        try {
            return integration.getOrder(orderId, authorizationHeader);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                throw new OrderNotFoundException(orderId);
            }
            throw e;
        }
    }

    private void ensureIyzicoReady() {
        if (!iyzicoProperties.enabled()) {
            throw new PaymentProviderUnavailableException(
                    "İyzico devre dışı (app.iyzico.enabled=false). Sandbox için APP_IYZICO_ENABLED=true ve anahtarları ayarlayın.");
        }
        if (!iyzicoProperties.hasCredentials()) {
            throw new PaymentProviderUnavailableException(
                    "İyzico API anahtarları tanımlı değil (APP_IYZICO_API_KEY / APP_IYZICO_SECRET_KEY).");
        }
        if (iyzicoProperties.callbackUrl() == null || iyzicoProperties.callbackUrl().isBlank()) {
            throw new PaymentProviderUnavailableException("app.iyzico.callback-url tanımlı olmalı.");
        }
    }

    private Options buildOptions() {
        Options options = new Options();
        options.setApiKey(iyzicoProperties.apiKey());
        options.setSecretKey(iyzicoProperties.secretKey());
        options.setBaseUrl(iyzicoProperties.baseUrl());
        return options;
    }

    private Buyer buildBuyer(UserProfileDto user) {
        String email = user.email();
        String local = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
        Buyer buyer = new Buyer();
        buyer.setId(String.valueOf(user.id()));
        buyer.setName(local.length() > 1 ? local.substring(0, 1).toUpperCase() + local.substring(1) : local);
        buyer.setSurname("Müşteri");
        buyer.setGsmNumber("+905555555555");
        buyer.setEmail(email);
        buyer.setIdentityNumber(iyzicoProperties.testBuyerIdentityNumber());
        buyer.setRegistrationAddress("Türkiye");
        buyer.setCity("Istanbul");
        buyer.setCountry("Turkey");
        buyer.setIp("85.34.78.112");
        buyer.setRegistrationDate("2020-03-05 12:00:00");
        buyer.setLastLoginDate("2020-03-05 12:00:00");
        buyer.setZipCode("34000");
        return buyer;
    }

    private static Address buildAddress(String contactHint) {
        Address a = new Address();
        a.setContactName("Müşteri");
        a.setCity("Istanbul");
        a.setCountry("Turkey");
        a.setAddress("Teslimat adresi — " + contactHint);
        a.setZipCode("34000");
        return a;
    }

    private static List<BasketItem> buildBasket(List<OrderLineDto> lines) {
        List<BasketItem> items = new ArrayList<>();
        for (OrderDetailDto.OrderLineDto line : lines) {
            BasketItem bi = new BasketItem();
            bi.setId(String.valueOf(line.productId()));
            bi.setName(line.productName());
            bi.setCategory1("Genel");
            bi.setCategory2("-");
            bi.setItemType(BasketItemType.PHYSICAL.name());
            bi.setPrice(line.lineTotal());
            items.add(bi);
        }
        return items;
    }
}

