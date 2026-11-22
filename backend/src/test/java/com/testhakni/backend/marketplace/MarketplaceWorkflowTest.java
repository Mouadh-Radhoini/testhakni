package com.testhakni.backend.marketplace;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.testhakni.backend.booking.domain.model.Booking;
import com.testhakni.backend.catalog.domain.model.ServiceCategory;
import com.testhakni.backend.catalog.domain.model.ServiceOffering;
import com.testhakni.backend.identity.domain.model.Role;
import com.testhakni.backend.identity.domain.model.User;
import com.testhakni.backend.notification.domain.model.Notification;
import com.testhakni.backend.offer.application.OfferService;
import com.testhakni.backend.offer.domain.model.Offer;
import com.testhakni.backend.offer.domain.model.OfferStatus;
import com.testhakni.backend.provider.domain.model.ProviderProfile;
import com.testhakni.backend.request.domain.model.RequestStatus;
import com.testhakni.backend.request.domain.model.ServiceRequest;
import com.testhakni.backend.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.http.MediaType;

class MarketplaceWorkflowTest extends IntegrationTest {
    private JsonNode call(String token, MockHttpServletRequestBuilder req, int expected) throws Exception {
        String body = mockMvc.perform(withToken(req, token)).andExpect(status().is(expected))
                .andReturn().getResponse().getContentAsString();
        return body.isBlank() ? null : readJson(body);
    }

    private MockHttpServletRequestBuilder body(MockHttpServletRequestBuilder req, Object payload) throws Exception {
        return req.contentType(MediaType.APPLICATION_JSON).content(json(payload));
    }

    private String request(String customer) throws Exception {
        return call(customer, body(post("/api/requests"), Map.of("title","Fuite cuisine", "description",
                "Réparer la fuite dans la cuisine", "category","PLOMBERIE","city","tUNIS","address","Adresse privée")), 201).get("id").asText();
    }

    private String offer(String provider, String request) throws Exception {
        return call(provider, body(post("/api/requests/" + request + "/offers"), Map.of("price",75,
                "proposedDate",LocalDate.now().plusDays(2).toString(),"description","Réparation complète")),201).get("id").asText();
    }

    @Test
    void concurrentDecisionsCreateOneBookingAndOneAcceptedOffer() throws Exception {
        String customer = token(registerCustomer("client@test.dev"));
        String req = request(customer);
        String a = offer(token(registerProvider("a@test.dev")), req);
        String b = offer(token(registerProvider("b@test.dev")), req);
        var start = new CountDownLatch(1);
        try (var pool = Executors.newFixedThreadPool(2)) {
            var calls = List.of(a,b).stream().map(id -> pool.submit(() -> {
                start.await();
                return mockMvc.perform(withToken(post("/api/requests/"+req+"/offers/"+id+"/accept"),customer))
                        .andReturn().getResponse().getStatus();
            })).toList();
            start.countDown();
            assertThat(List.of(calls.get(0).get(10,TimeUnit.SECONDS),calls.get(1).get(10,TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder(200,409);
        }
        assertThat(mongoTemplate.findAll(Booking.class)).hasSize(1);
        assertThat(mongoTemplate.findAll(Offer.class)).extracting(Offer::status)
                .containsExactlyInAnyOrder(OfferStatus.ACCEPTED, OfferStatus.REJECTED);
    }

    @org.springframework.beans.factory.annotation.Autowired
    private com.testhakni.backend.offer.application.OfferService offerService;
    @org.springframework.beans.factory.annotation.Autowired
    private org.springframework.transaction.PlatformTransactionManager transactions;

    @Test
    void rollbackRevertsWinnerBookingRequestAndNotificationTogether() throws Exception {
        var customer = registerCustomer("customer@test.dev");
        var provider = registerProvider("provider@test.dev");
        String req = request(token(customer));
        String offerId = offer(token(provider), req);
        var tx = new org.springframework.transaction.support.TransactionTemplate(transactions);
        tx.executeWithoutResult(status -> {
            offerService.accept(userId(customer), req, offerId);
            status.setRollbackOnly();
        });
        assertThat(mongoTemplate.findAll(Booking.class)).isEmpty();
        assertThat(mongoTemplate.findById(req, ServiceRequest.class).getStatus()).isEqualTo(RequestStatus.OPEN);
        assertThat(mongoTemplate.findById(offerId, Offer.class).status()).isEqualTo(OfferStatus.SUBMITTED);
        assertThat(mongoTemplate.findAll(Notification.class)).extracting(Notification::type).containsExactly("OFFER_RECEIVED");
    }

    @Test
    void acceptanceAndCancellationCannotBothCommit() throws Exception {
        String customer = token(registerCustomer("customer@test.dev"));
        String provider = token(registerProvider("provider@test.dev"));
        String req = request(customer);
        String offerId = offer(provider, req);
        String path = "/api/requests/" + req;
        var start = new CountDownLatch(1);
        try (var pool = Executors.newFixedThreadPool(2)) {
            var futures = List.of(post(path+"/offers/"+offerId+"/accept"),patch(path+"/cancel")).stream()
                    .map(command -> pool.submit(() -> {
                        start.await();
                        return mockMvc.perform(withToken(command, customer)).andReturn().getResponse().getStatus();
                    })).toList();
            start.countDown();
            assertThat(List.of(futures.get(0).get(10,TimeUnit.SECONDS), futures.get(1).get(10,TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder(200,409);
        }
        boolean cancelled = mongoTemplate.findById(req, ServiceRequest.class).getStatus() == RequestStatus.CANCELLED;
        assertThat(mongoTemplate.findAll(Booking.class)).hasSize(cancelled ? 0 : 1);
        assertThat(mongoTemplate.findById(offerId, Offer.class).status())
                .isEqualTo(cancelled ? OfferStatus.CANCELLED : OfferStatus.ACCEPTED);
    }

    @Test
    void cancellationClosesOffersAndRejectsLateWrites() throws Exception {
        String customer = token(registerCustomer("customer@test.dev"));
        String pro = token(registerProvider("provider@test.dev"));
        String req = request(customer);
        String id = offer(pro,req);
        String path = "/api/requests/"+req;
        var proposal = Map.of("price",80,"proposedDate",LocalDate.now().plusDays(1).toString(),"description","Intervention");
        call(pro,body(post(path+"/offers"),proposal),409);
        call(customer,patch(path+"/cancel"),200);
        call(pro,body(post(path+"/offers"),proposal),409);
        call(customer,post(path+"/offers/"+id+"/accept"),409);
        assertThat(call(pro,get("/api/providers/me/offers"),200).get(0).get("status").asText()).isEqualTo("CANCELLED");
        assertThat(call(pro,get("/api/providers/me/requests"),200).size()).isZero();
        assertThat(mongoTemplate.findAll(Booking.class)).isEmpty();
    }

    @Test
    void eligibilityUsesTheSameProfileForListingAndSubmission() throws Exception {
        String customer = token(registerCustomer("customer@test.dev"));
        var user = registerProvider("provider@test.dev");
        String pro = token(user);
        String req = request(customer);
        var profile = mongoTemplate.findAll(ProviderProfile.class).getFirst();
        var proposal = Map.of("price",80,"proposedDate",LocalDate.now().plusDays(1).toString(),"description","Intervention");
        for (String mismatch : List.of("ville","catégorie","disponibilité")) {
            profile.setCity(mismatch.equals("ville") ? "Sousse" : "Tunis");
            profile.setCategories(List.of(mismatch.equals("catégorie") ? ServiceCategory.MENAGE : ServiceCategory.PLOMBERIE));
            profile.setAvailable(!mismatch.equals("disponibilité"));
            mongoTemplate.save(profile);
            assertThat(call(pro,get("/api/providers/me/requests"),200).size()).isZero();
            call(pro,body(post("/api/requests/"+req+"/offers"),proposal),409);
        }
        call(customer,body(post("/api/requests/"+req+"/offers"),proposal),403);
    }

    @Test
    void concurrentSubmissionCannotLeavePendingOfferAfterAcceptance() throws Exception {
        String customer = token(registerCustomer("customer@test.dev"));
        String provider = token(registerProvider("provider@test.dev"));
        String competitor = token(registerProvider("competitor@test.dev"));
        String req = request(customer);
        String offerId = offer(provider, req);
        String path = "/api/requests/" + req + "/offers";
        var payload = Map.of("price",80,"proposedDate",LocalDate.now().plusDays(1).toString(),"description","Intervention");
        var start = new CountDownLatch(1);
        try (var pool = Executors.newFixedThreadPool(2)) {
            var submit = pool.submit(() -> {
                start.await();
                return mockMvc.perform(withToken(body(post(path),payload), competitor)).andReturn().getResponse().getStatus();
            });
            var accept = pool.submit(() -> {
                start.await();
                return mockMvc.perform(withToken(post(path+"/"+offerId+"/accept"), customer)).andReturn().getResponse().getStatus();
            });
            start.countDown();
            assertThat(submit.get(10,TimeUnit.SECONDS)).isIn(201,409);
            int result = accept.get(10,TimeUnit.SECONDS);
            assertThat(result).isIn(200,409);
            if (result == 409) call(customer,post(path+"/"+offerId+"/accept"),200);
        }
        assertThat(mongoTemplate.findAll(Offer.class)).extracting(Offer::status).doesNotContain(OfferStatus.SUBMITTED);
        assertThat(mongoTemplate.findAll(Booking.class)).hasSize(1);
    }

    @Test
    void unsupportedUploadCannotCreateAService() throws Exception {
        String pro = token(registerProvider("provider@test.dev"));
        call(pro,post("/api/services").contentType("application/octet-stream").content(new byte[]{1,2,3}),415);
        assertThat(mongoTemplate.findAll(ServiceOffering.class)).isEmpty();
    }

    @Test
    void workflowEnforcesPrivacyOwnershipProgressAndModeration() throws Exception {
        String customer = token(registerCustomer("customer@test.dev"));
        String stranger = token(registerCustomer("stranger@test.dev"));
        var provider = registerProvider("provider@test.dev");
        String pro = token(provider);
        var adminUser = mongoTemplate.findById(userId(registerCustomer("admin@test.dev")), User.class);
        adminUser.setRole(Role.ADMIN);
        mongoTemplate.save(adminUser);
        String admin = token(call(null, body(post("/api/auth/login"), Map.of("email","admin@test.dev","password",PASSWORD)),200));
        String req = request(customer);
        var open = call(pro,get("/api/providers/me/requests"),200);
        assertThat(open.size()).isEqualTo(1);
        assertThat(open.get(0).has("address") || open.get(0).has("customerId")).isFalse();
        String id = offer(pro,req);
        String offers = "/api/requests/"+req+"/offers";
        call(stranger,get(offers),403);
        assertThat(call(customer,get(offers),200).size()).isOne();
        call(stranger,post(offers+"/"+id+"/accept"),403);
        var booking = call(customer,post(offers+"/"+id+"/accept"),200);
        assertThat(call(customer,post(offers+"/"+id+"/accept"),200)).isEqualTo(booking);
        String path = "/api/bookings/"+booking.get("id").asText();
        call(stranger,get(path),403);
        var feedback = Map.of("rating",5,"comment","Excellent service");
        call(customer,body(post(path+"/review"),feedback),409);
        call(pro,body(patch(path+"/status"),Map.of("status","IN_PROGRESS")),200);
        call(pro,body(patch(path+"/status"),Map.of("status","AWAITING_CONFIRMATION")),200);
        call(customer,post(path+"/confirm"),200);
        call(customer,post(offers+"/"+id+"/accept"),409);
        call(customer,body(post(path+"/review"),Map.of("rating",6,"comment","Excellent")),400);
        var review = call(customer,body(post(path+"/review"),feedback),201);
        call(customer,body(post(path+"/review"),feedback),409);
        String publicReviews = "/api/providers/"+userId(provider)+"/reviews";
        assertThat(call(null,get(publicReviews),200).get("average").asDouble()).isEqualTo(5);
        String notification = call(pro,get("/api/notifications"),200).get("content").get(0).get("id").asText();
        call(customer,patch("/api/notifications/"+notification+"/read"),404);
        call(pro,patch("/api/notifications/"+notification+"/read"),204);
        call(pro,get("/api/admin/dashboard"),403);
        call(admin,get("/api/admin/dashboard"),200);
        String reviewId = review.get("id").asText();
        String report = call(customer,body(post("/api/reports"),Map.of("targetType","REVIEW","targetId",reviewId,
                "reason","Vérification demandée")),201).get("id").asText();
        call(admin,body(patch("/api/admin/reviews/"+reviewId),Map.of("enabled",false,"reason","Avis masqué")),200);
        assertThat(call(null,get(publicReviews),200).get("count").asLong()).isZero();
        call(admin,body(patch("/api/admin/reports/"+report),Map.of("status","RESOLVED","reason","Avis modéré")),200);
        call(admin,body(patch("/api/admin/users/"+userId(provider)),Map.of("enabled",false,"reason","Contrôle")),200);
        call(pro,get("/api/bookings"),401);
    }
}
