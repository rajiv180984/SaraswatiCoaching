@echo off
echo Generating self-signed SSL certificate for Saraswati Coaching...
keytool -genkeypair ^
  -alias saraswati ^
  -keyalg RSA ^
  -keysize 2048 ^
  -storetype PKCS12 ^
  -keystore src\main\resources\keystore.p12 ^
  -validity 365 ^
  -storepass saraswati@123 ^
  -dname "CN=Saraswati Coaching, OU=Education, O=Saraswati, L=Delhi, ST=Delhi, C=IN"

echo.
echo SSL keystore created at: src\main\resources\keystore.p12
echo Server will start on: https://localhost:8443
echo Swagger UI: https://localhost:8443/swagger-ui/index.html
