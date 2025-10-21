// package com.example.auth_server.service;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.security.oauth2.jwt.Jwt;
// import org.springframework.security.oauth2.jwt.JwtEncoder;
// import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
// import org.springframework.security.oauth2.jwt.JwtEncodingException;
// import org.springframework.stereotype.Component;

// @Component
// public class JWTDilithumSigner implements JwtEncoder {

// @Autowired
// private DilithiumService dilithiumService;

// @Override
// public Jwt encode(JwtEncoderParameters parameters) throws
// JwtEncodingException {
// String payload = createJwtPayload(parameters);
// String signature = dilithiumService.signData(payload, null); // TODO: passar
// a chave privada correta
// return buildJwt(payload, signature);
// }

// private Jwt buildJwt(String payload, String signature) {
// // TODO Auto-generated method stub
// throw new UnsupportedOperationException("Unimplemented method 'buildJwt'");
// }

// private String createJwtPayload(JwtEncoderParameters parameters) {
// // TODO Auto-generated method stub
// throw new UnsupportedOperationException("Unimplemented method
// 'createJwtPayload'");
// }

// }
