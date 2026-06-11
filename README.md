## PAȘII DE COMPILARE, INSTALARE ȘI LANSARE AI APLICAȚIEI

### Adresele repository-urilor de pe github.com

1. Frontend: https://github.com/helenajullia/RescueMe-frontend.git
2. Backend: https://github.com/helenajullia/RescueMe-backend.git

Pentru clonarea proiectului în mediul de dezvoltare local, se vor folosi comenzile: "git clone https://github.com/helenajullia/RescueMe-frontend.git" și "git clone https://github.com/helenajullia/RescueMe-backend.git"

#### Configurare Backend

Proiectul trebuie deschis într-un IDE (mediu de dezvoltare integrat) compatibil cu Java (ex. IntelliJ IDEA).

Trebuie instalat Java 21 și Apache Maven.

Trebuie să se seteze variabilele de mediu pentru conectarea la baza de date:

* SPRING_DATASOURCE_URL
* SPRING_DATASOURCE_USERNAME
* SPRING_DATASOURCE_PASSWORD

După configurare, compilarea se realizează în terminal prin comanda: `mvn clean install` în directorul `backend`.

Aplicația se pornește prin rularea clasei `RescuemeApplication`, fiind disponibilă la portul `8080`.

#### Configurare Frontend

Proiectul se deschide în Visual Studio Code sau orice alt editor care suportă JavaScript și Vue.js.

Pe sistem trebuie să fie instalat Node.js versiunea 20.12.0 (sau compatibil) și npm.

În directorul proiectului `frontend` se rulează comanda: `npm install`.

Pornirea aplicației se face cu: `npm run dev`.

Interfața va fi disponibilă la adresa `http://localhost:5173`.

#### Configurare bazei de date

Pentru ca aplicația să funcționeze complet, trebuie configurată o bază de date PostgreSQL. Este indicat să fie instalate atât PostgreSQL, cât și interfața grafică pgAdmin.
