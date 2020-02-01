# WebAuthn4J

![WebAuthn4J](./docs/image/logo.png)

[![Build Status](https://travis-ci.org/webauthn4j/webauthn4j.svg?branch=master)](https://travis-ci.org/webauthn4j/webauthn4j)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=webauthn4j&metric=coverage)](https://sonarcloud.io/dashboard?id=webauthn4j)
[![Build Status](https://img.shields.io/maven-central/v/com.webauthn4j/webauthn4j-core.svg)](https://search.maven.org/#search%7Cga%7C1%7Cwebauthn4j)
[![license](https://img.shields.io/github/license/webauthn4j/webauthn4j.svg)](https://github.com/webauthn4j/webauthn4j/blob/master/LICENSE.txt)

A portable Java library for WebAuthn server side verification

### Conformance

All mandatory test cases and optional Android Key attestation test cases of [FIDO2 Test Tools provided by FIDO Alliance](https://fidoalliance.org/certification/functional-certification/conformance/)
are passed.

### Supported Attestation statement format

All attestation statement formats are supported.

* Packed attestation
* FIDO U2F attestation
* Android Key attestation
* Android SafetyNet attestation
* TPM attestation
* None attestation

### Projects using WebAuthn4J

* [Keycloak](https://www.keycloak.org/)

## Documentation

You can find out more details from the [reference](https://webauthn4j.github.io/webauthn4j/en/).

## Getting from Maven Central

If you are using Maven, just add the webauthn4j as a dependency:

```xml
<properties>
  ...
  <!-- Use the latest version whenever possible. -->
  <webauthn4j.version>0.3.3.RELEASE</webauthn4j.version>
  ...
</properties>

<dependencies>
  ...
  <dependency>
    <groupId>com.webauthn4j</groupId>
    <artifactId>webauthn4j-core</artifactId>
    <version>${webauthn4j.version}</version>
  </dependency>
  ...
</dependencies>
```


## Build from source

WebAuthn4J uses a Gradle based build system.
In the instructions below, `gradlew` is invoked from the root of the source tree and serves as a cross-platform,
self-contained bootstrap mechanism for the build.

### Prerequisites

- Java8 or later

### Checkout sources

```
git clone https://github.com/webauthn4j/webauthn4j
```

### Build all jars

```
./gradlew build
```

## How to use

Parse and Validation on Registration
```java 
// Client properties
byte[] attestationObject = null /* set attestationObject */;
byte[] clientDataJSON = null /* set clientDataJSON */;
String clientExtensionJSON = null;  /* set clientExtensionJSON */;
Set<String> transports = null /* set transports */;

// Server properties
Origin origin = null /* set origin */;
String rpId = null /* set rpId */;
Challenge challenge = null /* set challenge */;
byte[] tokenBindingId = null /* set tokenBindingId */;
ServerProperty serverProperty = new ServerProperty(origin, rpId, challenge, tokenBindingId);

// expectations
boolean userVerificationRequired = false;
boolean userPresenceRequired = true;
List<String> expectedExtensionIds = Collections.emptyList();

RegistrationRequest registrationRequest = new RegistrationRequest(attestationObject, clientDataJSON, clientExtensionJSON, transports);
RegistrationParameters registrationParameters = new RegistrationParameters(serverProperty, userVerificationRequired, userPresenceRequired, expectedExtensionIds);
RegistrationData registrationData;
try{
    registrationData = webAuthnManager.parse(registrationRequest);
}
catch (DataConversionException e){
    // If you would like to handle WebAuthn data structure parse error, please catch DataConversionException
    throw e;
}
try{
    webAuthnManager.validate(registrationData, registrationParameters);
}
catch (ValidationException e){
    // If you would like to handle WebAuthn data validation error, please catch ValidationException
    throw e;
}

// please persist Authenticator object, which will be used in the authentication process.
Authenticator authenticator =
        new AuthenticatorImpl( // You may create your own Authenticator implementation to save friendly authenticator name
                registrationData.getAttestationObject().getAuthenticatorData().getAttestedCredentialData(),
                registrationData.getAttestationObject().getAttestationStatement(),
                registrationData.getAttestationObject().getAuthenticatorData().getSignCount()
        );
save(authenticator); // please persist authenticator in your manner
```

Parse and Validation on authentication
```java 
// Client properties
byte[] credentialId = null /* set credentialId */;
byte[] userHandle = null /* set userHandle */;
byte[] authenticatorData = null /* set authenticatorData */;
byte[] clientDataJSON = null /* set clientDataJSON */;
String clientExtensionJSON = null /* set clientExtensionJSON */;
byte[] signature = null /* set signature */;

// Server properties
Origin origin = null /* set origin */;
String rpId = null /* set rpId */;
Challenge challenge = null /* set challenge */;
byte[] tokenBindingId = null /* set tokenBindingId */;
ServerProperty serverProperty = new ServerProperty(origin, rpId, challenge, tokenBindingId);

// expectations
boolean userVerificationRequired = true;
boolean userPresenceRequired = true;
List<String> expectedExtensionIds = Collections.emptyList();

Authenticator authenticator = load(credentialId); // please load authenticator object persisted in the registration process in your manner

AuthenticationRequest authenticationRequest =
        new AuthenticationRequest(
                credentialId,
                userHandle,
                authenticatorData,
                clientDataJSON,
                clientExtensionJSON,
                signature
        );
AuthenticationParameters authenticationParameters =
        new AuthenticationParameters(
                serverProperty,
                authenticator,
                userVerificationRequired,
                userPresenceRequired,
                expectedExtensionIds
        );

AuthenticationData authenticationData;
try{
    authenticationData = webAuthnManager.parse(authenticationRequest);
}
catch (DataConversionException e){
    // If you would like to handle WebAuthn data structure parse error, please catch DataConversionException
    throw e;
}
try{
    webAuthnManager.validate(authenticationData, authenticationParameters);
}
catch (ValidationException e){
    // If you would like to handle WebAuthn data validation error, please catch ValidationException
    throw e;
}
// please update the counter of the authenticator record
updateCounter(
        authenticationData.getAuthenticatorData().getAttestedCredentialData().getCredentialId(),
        authenticationData.getAuthenticatorData().getSignCount()
);

```

## Sample application

Spring Security WebAuthn is built on the top of WebAuthn4J, and its sample application demonstrates WebAuthn4J feature well.
Please see [Spring Security WebAuthn sample application](https://github.com/sharplab/spring-security-webauthn).

## License

WebAuthn4J is Open Source software released under the
[Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0.html).

## Contributing

Interested in helping out with WebAuthn4J? Great! Your participation in the community is much appreciated!
Please feel free to open issues and send pull-requests.
