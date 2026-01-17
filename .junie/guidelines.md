
# Project Guidelines: Library & Reservation System (Project-7)

## 1. Technical Stack & Environment
- **Java:** 21
- **Framework:** Spring Boot 4.x.x
- **Build Tool:** Maven
- **Database (Prod/Dev):** PostgreSQL (via Docker Compose)
- **Database (Tests):** H2 (In-memory)
- **Lombok:** Enabled (Use `@Data`, `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`)
- **Documentation:** Springdoc OpenAPI / Swagger UI at `/swagger-ui.html`
## 2. Architecture & Design Patterns
- **Layered Architecture:** Controller -> Service -> Repository/DAO.
- **Persistence:** * Use **Spring Data JPA** for standard CRUD and complex relationships.
    - Use **JdbcTemplate** for specific performance-heavy queries or custom DAO logic.
- **Data Mapping:** * **Automatic Model Mapping:** For model mapping use ModelMapper from org.modelmapper. If this is impossible, then create dedicated mapper classes or static methods to convert Entity ↔ DTO.
    - **Validation:** Use `jakarta.validation` (`@NotNull`, `@NotBlank`, etc.) in DTOs.
- **Error Handling:** Global handler using `@RestControllerAdvice` and `@ExceptionHandler`. Custom exceptions like `ResourceNotFoundException` are required.
## 3. Database Schema & Models
- **Entities:** Use `@Entity`, `@Id`, `@GeneratedValue`, `@Column`.
- **Relationships:** * `@OneToMany` / `@ManyToOne` with `@JoinColumn`.
    - `@ManyToMany` with `@JoinTable`.
- **Initialization:** Use `.sql` files for schema/data seeding.
## 4. Business Logic: Reservations & Books
- **Reservation States:** Must implement: `OCZEKUJĄCA`, `POTWIERDZONA`, `WYPOŻYCZONA`, `ZWRÓCONA`.
- **Loan Flow:** User reserves -> Reservation confirmation (state change) -> Book loan.
- **Constraints:** * Block reservation if no copies are available.
    - Users can only rate a book once per week.
    - Calculate average rating dynamically.
- **Search & Analytics:** * Filter books by Title, Author, and Genre (Keywords).
    - "Popular" section based on highest average ratings.
    - Reports: Most popular books, most read authors, most active users using JPQL or Native SQL.
## 5. Security (Spring Security)
- **Implementation:** `SecurityFilterChain` as a `@Bean`.
- **Access Control:** Use `requestMatchers` to secure endpoints.
- **Password Storage:** `BCryptPasswordEncoder`.
- **Authentication:** `UserDetailsService` with `loadUserByUsername`.
## 6. Frontend (Thymeleaf)
- **Engine:** Thymeleaf with fragments (`th:fragment`, `th:replace`).
- **Styling:** Bootstrap 5.
- **Features:** File upload (images/covers), Download (Resources), and Export to CSV/PDF.
## 7. Testing & Quality
- **Coverage:** Minimum **70%** (JaCoCo).
- **Unit Tests:** JUnit 5 + Mockito (`@Mock`, `@InjectMocks`).
- **Persistence Tests:** `@DataJpaTest` for repositories (min. 10 CRUD tests).
- **Integration Tests:** `MockMvc` for REST controllers and `@WithMockUser` for Security.
- **Architecture Tests:** Use **ArchUnit** to enforce package rules (e.g., Controllers must not depend directly on Entities).