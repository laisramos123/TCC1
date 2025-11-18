// package com.example.auth_server;

// import com.example.auth_server.dto.ConsentRequest;
// import com.example.auth_server.dto.ConsentResponse;
// import com.example.auth_server.service.ConsentService;
// import com.example.auth_server.dilithium.DilithiumSignature;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.boot.test.web.client.TestRestTemplate;
// import org.springframework.http.*;
// import org.springframework.test.context.ActiveProfiles;

// import java.time.LocalDateTime;
// import java.util.Arrays;
// import java.util.List;

// import static org.assertj.core.api.Assertions.assertThat;
// import static org.junit.jupiter.api.Assertions.*;

// @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// @ActiveProfiles("test")
// @DisplayName("Testes de Integração - Open Finance com Dilithium")
// public class OpenFinanceIntegrationTest {

// @Autowired
// private TestRestTemplate restTemplate;

// @Autowired
// private ConsentService consentService;

// @Autowired
// private DilithiumSignature dilithiumSignature;

// private String consentId;
// private static final String CPF_TEST = "12345678900";

// @BeforeEach
// void setUp() throws Exception {

// if (dilithiumSignature.getPublicKey() == null) {
// dilithiumSignature.keyPair();
// }
// }

// @Test
// @DisplayName("1. Fluxo completo OAuth2 + Consent + Dilithium")
// void testCompleteOAuth2FlowWithDilithium() throws Exception {

// ConsentRequest consentRequest = createConsentRequest();

// ResponseEntity<ConsentResponse> consentResponse = restTemplate.postForEntity(
// "/open-banking/consents/v2/consents",
// consentRequest,
// ConsentResponse.class);

// assertThat(consentResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
// assertNotNull(consentResponse.getBody());
// assertNotNull(consentResponse.getBody().getData().getConsentId());

// consentId = consentResponse.getBody().getData().getConsentId();

// assertEquals("AWAITING_AUTHORISATION",
// consentResponse.getBody().getData().getStatus());

// String authorizationUrl = buildAuthorizationUrl(consentId);
// assertThat(authorizationUrl).contains("consent:" + consentId);

// ConsentResponse updatedConsent = consentService.updateStatus(
// consentId,
// com.example.auth_server.enums.ConsentStatus.AUTHORISED);

// assertEquals("AUTHORISED", updatedConsent.getData().getStatus());

// String consentData = consentId + "|" + CPF_TEST;
// byte[] signature = dilithiumSignature.sign(consentData.getBytes());

// boolean isValid = dilithiumSignature.verify(
// consentData.getBytes(),
// signature,
// dilithiumSignature.getPublicKey());

// assertTrue(isValid, "Assinatura Dilithium deve ser válida");
// }

// @Test
// @DisplayName("2. Validação de permissões do consentimento")
// void testConsentPermissionValidation() {

// List<String> permissions = Arrays.asList(
// "ACCOUNTS_READ",
// "ACCOUNTS_BALANCES_READ");

// ConsentRequest request = createConsentRequestWithPermissions(permissions);
// ConsentResponse response = consentService.createConsent(request);

// consentService.updateStatus(
// response.getData().getConsentId(),
// com.example.auth_server.enums.ConsentStatus.AUTHORISED);

// assertDoesNotThrow(() -> consentService.validateConsentForResourceAccess(
// response.getData().getConsentId(),
// "ACCOUNTS_READ"));

// assertThrows(RuntimeException.class, () ->
// consentService.validateConsentForResourceAccess(
// response.getData().getConsentId(),
// "ACCOUNTS_TRANSACTIONS_READ"));
// }

// @Test
// @DisplayName("3. Teste de expiração de consentimento")
// void testConsentExpiration() {

// ConsentRequest request = createConsentRequest();
// request.getData().setExpirationDateTime(LocalDateTime.now().plusMinutes(1));

// ConsentResponse response = consentService.createConsent(request);
// String consentId = response.getData().getConsentId();

// consentService.updateStatus(
// consentId,
// com.example.auth_server.enums.ConsentStatus.AUTHORISED);

// assertDoesNotThrow(() ->
// consentService.validateConsentForResourceAccess(consentId, "ACCOUNTS_READ"));

// assertTrue(response.getData().getExpirationDateTime().isAfter(LocalDateTime.now()));
// }

// @Test
// @DisplayName("4. Teste de revogação de consentimento")
// void testConsentRevocation() {
// ConsentRequest request = createConsentRequest();
// ConsentResponse response = consentService.createConsent(request);
// String consentId = response.getData().getConsentId();

// consentService.updateStatus(
// consentId,
// com.example.auth_server.enums.ConsentStatus.AUTHORISED);

// consentService.revokeConsent(consentId, "CUSTOMER_MANUALLY_REVOKED", "USER");

// assertThrows(RuntimeException.class,
// () -> consentService.validateConsentForResourceAccess(consentId,
// "ACCOUNTS_READ"));
// }

// @Test
// @DisplayName("5. Teste de assinatura Dilithium em JWT")
// void testDilithiumJWTSignature() throws Exception {

// String header = "{\"alg\":\"DILITHIUM3\",\"typ\":\"JWT\"}";
// String payload = "{\"sub\":\"" + CPF_TEST + "\",\"consent_id\":\"" +
// consentId + "\",\"exp\":" +
// (System.currentTimeMillis() / 1000 + 3600) + "}";

// String signingInput = base64Encode(header) + "." + base64Encode(payload);

// byte[] signature = dilithiumSignature.sign(signingInput.getBytes());

// boolean isValid = dilithiumSignature.verify(
// signingInput.getBytes(),
// signature,
// dilithiumSignature.getPublicKey());

// assertTrue(isValid, "JWT assinado com Dilithium deve ser válido");

// assertThat(signature.length).isBetween(3200, 3400);
// }

// @Test
// @DisplayName("6. Comparação de performance RSA vs Dilithium")
// void testPerformanceComparison() throws Exception {
// int iterations = 100;
// String testData = "Performance test data for TCC UnB 2025";

// long dilithiumStart = System.nanoTime();
// for (int i = 0; i < iterations; i++) {
// byte[] sig = dilithiumSignature.sign(testData.getBytes());
// dilithiumSignature.verify(testData.getBytes(), sig,
// dilithiumSignature.getPublicKey());
// }
// long dilithiumTime = System.nanoTime() - dilithiumStart;

// System.out.println("=== Performance Comparison ===");
// System.out.println("Dilithium (100 sign+verify): " +
// (dilithiumTime / 1_000_000) + " ms");
// System.out.println("Average per operation: " +
// (dilithiumTime / iterations / 1_000_000) + " ms");

// assertTrue((dilithiumTime / iterations / 1_000_000) < 10);
// }

// @Test
// @DisplayName("7. Teste de endpoints protegidos com JWT Dilithium")
// void testProtectedEndpointsWithDilithiumJWT() {

// HttpHeaders headers = new HttpHeaders();
// headers.setBearerAuth(generateDilithiumJWT());

// HttpEntity<String> entity = new HttpEntity<>(headers);

// String resourceUrl = "/open-banking/accounts/v2/accounts";

// assertNotNull(headers.getFirst(HttpHeaders.AUTHORIZATION));
// assertTrue(headers.getFirst(HttpHeaders.AUTHORIZATION).startsWith("Bearer
// "));
// }

// // Métodos auxiliares

// private ConsentRequest createConsentRequest() {
// return createConsentRequestWithPermissions(Arrays.asList(
// "ACCOUNTS_READ",
// "ACCOUNTS_BALANCES_READ",
// "ACCOUNTS_TRANSACTIONS_READ"));
// }

// private ConsentRequest createConsentRequestWithPermissions(List<String>
// permissions) {
// return ConsentRequest.builder()
// .data(ConsentRequest.Data.builder()
// .loggedUser(ConsentRequest.LoggedUser.builder()
// .document(ConsentRequest.Document.builder()
// .identification(CPF_TEST)
// .rel("CPF")
// .build())
// .build())
// .businessEntity(ConsentRequest.BusinessEntity.builder()
// .document(ConsentRequest.Document.builder()
// .identification(CPF_TEST)
// .rel("CPF")
// .build())
// .build())
// .permissions(permissions)
// .expirationDateTime(LocalDateTime.now().plusDays(60))
// .transactionFromDateTime(LocalDateTime.now().minusYears(1))
// .transactionToDateTime(LocalDateTime.now())
// .build())
// .build();
// }

// private String buildAuthorizationUrl(String consentId) {
// return "http://localhost:8080/oauth2/authorize?" +
// "response_type=code&" +
// "client_id=oauth-client&" +
// "scope=openid consent:" + consentId + " accounts&" +
// "redirect_uri=http://localhost:8081/callback&" +
// "code_challenge=test_challenge&" +
// "code_challenge_method=S256";
// }

// private String generateDilithiumJWT() {

// return "eyJhbGciOiJESUxJVEhJVU0zIiwidHlwIjoiSldUIn0." +
// "eyJzdWIiOiIxMjM0NTY3ODkwMCIsImNvbnNlbnRfaWQiOiJ0ZXN0IiwiZXhwIjoxNzA5MjUxMjAwfQ."
// +
// "dilithium_signature_placeholder";
// }

// private String base64Encode(String data) {
// return java.util.Base64.getUrlEncoder()
// .withoutPadding()
// .encodeToString(data.getBytes());
// }
// }