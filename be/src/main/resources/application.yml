server:
  port: 8080

spring:
  datasource:
    url: jdbc:h2:mem:basedb
    driver-class-name: org.h2.Driver
    username: sa
    password: password

  h2:
    console:
      enabled: true
      path: /h2-console

  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    defer-datasource-initialization: true
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        format_sql: true

  sql:
    init:
      mode: always

app:
  jwt:
    secret: 404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
    expiration: 86400000
    refresh-expiration: 604800000

logging:
  level:
    com.base: DEBUG
    org.springframework.security: INFO