# Pedidos360 🚀

Sistema de gestión de pedidos con arquitectura BFF (Backend-For-Frontend) integrado con **Microsoft Entra External ID (CIAM)**.

## 📁 Estructura del Proyecto

```
Pedidos360/
├── frontend/   # Aplicación SPA Angular 18 (MSAL Angular)
└── backend/    # Spring Boot 3.3.5 (OAuth2 Resource Server)
```

## 🛠️ Tecnologías Utilizadas

- **Frontend:** Angular 18, MSAL Angular / Browser, RxJS, Tailwind/CSS.
- **Backend:** Spring Boot 3.3.5, Java 21, Spring Security OAuth2 Resource Server, Spring Data JPA, H2 Database.
- **Identidad y Autenticación:** Microsoft Entra External ID (CIAM).

## 🚀 Instrucciones de Ejecución

### Backend (Spring Boot)
```bash
cd backend
./mvnw spring-boot:run
```
- **Puerto:** `8081`
- **Consola H2:** `http://localhost:8081/h2-console`

### Frontend (Angular)
```bash
cd frontend
npm install
npm start
```
- **Puerto:** `4200` (`http://localhost:4200`)
