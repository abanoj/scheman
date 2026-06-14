package com.abanoj.scheman.demo;

import com.abanoj.scheman.auth.entity.Role;
import com.abanoj.scheman.auth.entity.User;
import com.abanoj.scheman.auth.repository.UserRepository;
import com.abanoj.scheman.employee.entity.Employee;
import com.abanoj.scheman.employee.repository.EmployeeRepository;
import com.abanoj.scheman.shift.entity.Shift;
import com.abanoj.scheman.shift.entity.ShiftType;
import com.abanoj.scheman.shift.repository.ShiftRepository;
import com.abanoj.scheman.shiftassignment.entity.ShiftAssignment;
import com.abanoj.scheman.shiftassignment.repository.ShiftAssignmentRepository;
import com.abanoj.scheman.store.entity.Store;
import com.abanoj.scheman.store.repository.StoreRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class DemoDataService {

    public static final String DEMO_ADMIN_EMAIL    = "admin@demo.com";
    public static final String DEMO_MANAGER_EMAIL  = "manager@demo.com";
    public static final String DEMO_EMPLOYEE_EMAIL = "empleado@demo.com";
    static final String DEMO_PASSWORD = "Demo@2026";

    @PersistenceContext
    private EntityManager entityManager;

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final StoreRepository storeRepository;
    private final ShiftRepository shiftRepository;
    private final ShiftAssignmentRepository shiftAssignmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void reset() {
        log.info("Demo reset: wiping data...");
        wipe();
        log.info("Demo reset: seeding data...");
        seed();
        log.info("Demo reset complete.");
    }

    // ── Wipe ────────────────────────────────────────────────────────────────

    private void wipe() {
        entityManager.createNativeQuery("DELETE FROM shift_assignments").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM shift_available_days").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM shifts").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM employee_store").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM employees").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM stores").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM refresh_tokens").executeUpdate();
        entityManager.createNativeQuery(
                "DELETE FROM users WHERE role IN ('EMPLOYEE','MANAGER')").executeUpdate();
        entityManager.createNativeQuery(
                "DELETE FROM users WHERE email = :email")
                .setParameter("email", DEMO_ADMIN_EMAIL)
                .executeUpdate();
        entityManager.flush();
        entityManager.clear();
    }

    // ── Seed ────────────────────────────────────────────────────────────────

    private void seed() {
        String encoded = passwordEncoder.encode(DEMO_PASSWORD);

        // Accounts
        saveUser("Admin",   "Demo",      DEMO_ADMIN_EMAIL,   encoded, Role.ADMIN);
        saveUser("Manager", "Demo",      DEMO_MANAGER_EMAIL, encoded, Role.MANAGER);

        // Stores
        Store centro = saveStore("Tienda Centro", "Calle Gran Vía 1, Madrid");
        Store norte  = saveStore("Tienda Norte",  "Av. del Prado 15, Madrid");

        // Day sets
        Set<DayOfWeek> weekdays = Set.of(
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);
        Set<DayOfWeek> weekdaysPlusSat = Set.of(
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY);

        LocalDate from = LocalDate.of(2024, 1, 1);

        // Employees
        Employee demo   = saveEmployee("Demo",   "Empleado", DEMO_EMPLOYEE_EMAIL,     "00000000T",
                encoded, ShiftType.MORNING, 40, centro);
        Employee ana    = saveEmployee("Ana",    "García",   "ana.garcia@demo.com",   "12345678A",
                passwordEncoder.encode("12345678A"), ShiftType.AFTERNOON, 32, centro);
        Employee carlos = saveEmployee("Carlos", "López",    "carlos.lopez@demo.com", "87654321B",
                passwordEncoder.encode("87654321B"), ShiftType.MORNING, 24, norte);

        carlos.addStore(centro);
        employeeRepository.save(carlos);

        // Shifts
        Shift mCentro = saveShift(centro, "Turno Mañana",
                LocalTime.of(8, 0), LocalTime.of(15, 0), ShiftType.MORNING,  from, weekdays);
        Shift tCentro = saveShift(centro, "Turno Tarde",
                LocalTime.of(15, 0), LocalTime.of(23, 0), ShiftType.AFTERNOON, from, weekdays);
        Shift mNorte  = saveShift(norte,  "Turno Mañana",
                LocalTime.of(9, 0), LocalTime.of(17, 0), ShiftType.MORNING,  from, weekdaysPlusSat);

        // Assignments — current week
        LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY);

        // Demo empleado: Mon, Tue, Wed → Mañana Centro  (3 × 7 h = 21 h / 40 h)
        saveAssignment(demo, mCentro, monday);
        saveAssignment(demo, mCentro, monday.plusDays(1));
        saveAssignment(demo, mCentro, monday.plusDays(2));

        // Ana: Mon, Wed, Fri → Tarde Centro  (3 × 8 h = 24 h / 32 h)
        saveAssignment(ana, tCentro, monday);
        saveAssignment(ana, tCentro, monday.plusDays(2));
        saveAssignment(ana, tCentro, monday.plusDays(4));

        // Carlos: Tue, Thu → Mañana Norte  (2 × 8 h = 16 h / 24 h)
        saveAssignment(carlos, mNorte, monday.plusDays(1));
        saveAssignment(carlos, mNorte, monday.plusDays(3));
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private void saveUser(String firstName, String lastName, String email,
                          String encoded, Role role) {
        userRepository.save(User.builder()
                .firstName(firstName).lastName(lastName)
                .email(email).password(encoded)
                .role(role).mustChangePassword(false)
                .build());
    }

    private Store saveStore(String name, String address) {
        return storeRepository.save(Store.builder().name(name).address(address).build());
    }

    private Employee saveEmployee(String firstName, String lastName, String email, String dni,
                                  String encoded, ShiftType shift, int hours, Store store) {
        User user = User.builder()
                .firstName(firstName).lastName(lastName)
                .email(email).password(encoded)
                .role(Role.EMPLOYEE).mustChangePassword(false)
                .build();
        userRepository.save(user);

        Employee employee = Employee.builder()
                .user(user).dni(dni)
                .preferredShift(shift).weeklyContractedHours(hours)
                .build();
        employee.addStore(store);
        return employeeRepository.save(employee);
    }

    private Shift saveShift(Store store, String name, LocalTime start, LocalTime end,
                            ShiftType type, LocalDate from, Set<DayOfWeek> days) {
        return shiftRepository.save(Shift.builder()
                .name(name).store(store)
                .startTime(start).endTime(end)
                .shiftType(type).effectiveFrom(from)
                .groupId(UUID.randomUUID())
                .availableDays(new HashSet<>(days))
                .build());
    }

    private void saveAssignment(Employee employee, Shift shift, LocalDate date) {
        shiftAssignmentRepository.save(ShiftAssignment.builder()
                .employee(employee).shift(shift).date(date).build());
    }
}
