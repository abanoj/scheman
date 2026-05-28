package com.abanoj.scheman.employee;

import com.abanoj.scheman.auth.entity.Role;
import com.abanoj.scheman.auth.entity.User;
import com.abanoj.scheman.config.JpaAuditingConfig;
import com.abanoj.scheman.employee.entity.Employee;
import com.abanoj.scheman.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.abanoj.scheman.store.entity.Store;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaAuditingConfig.class)
class EmployeeRepositoryTest {
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private TestEntityManager entityManager;

    private static final Pageable DEFAULT_PAGEABLE = PageRequest.of(0,10);

    private Employee persistEmployee(String firstname, String lastname, String dni){
        User user = User.builder()
                .firstName(firstname)
                .lastName(lastname)
                .email(firstname.toLowerCase() + "." + lastname.toLowerCase() +"@mail.com")
                .password("password")
                .role(Role.EMPLOYEE)
                .build();
        entityManager.persist(user);
        Employee employee = Employee.builder()
                .dni(dni)
                .user(user)
                .build();
        entityManager.persistAndFlush(employee);
        return employee;
    }

    private Store persistStore(String name){
        Store store = Store.builder()
                .name(name)
                .build();
        entityManager.persistAndFlush(store);
        return store;
    }


    @Nested
    @DisplayName("findAll")
    class FindAll {
        @Test
        void shouldReturnAllEmployees_whenExist(){
            //given
            Employee employeePedro = persistEmployee("Pedro", "Perez", "Y8190012J");
            Employee employeeMaria = persistEmployee("Maria", "Segovia", "X1234567A");
            entityManager.clear();
            //when
            Page<Employee> result = employeeRepository.findAll(DEFAULT_PAGEABLE);
            //then
            assertThat(result).hasSize(2);
            assertThat(result)
                    .extracting(Employee::getId)
                    .containsExactlyInAnyOrder(employeePedro.getId(), employeeMaria.getId());
        }

        @Test
        void shouldReturnEmpty_whenThereAreNoEmployees(){
            //given
            //when
            Page<Employee> result = employeeRepository.findAll(DEFAULT_PAGEABLE);
            //then
            assertThat(result).isEmpty();
        }

        @Test
        void shouldFetchUserAndPreferredStores(){
            //given
            Employee employeePedro = persistEmployee("Pedro", "Perez", "Y8190012J");
            Store store = persistStore("Sucursal Centro");
            employeePedro.addStore(store);
            entityManager.persistAndFlush(employeePedro);
            entityManager.clear();
            //when
            Page<Employee> result = employeeRepository.findAll(DEFAULT_PAGEABLE);
            //then
            Employee fetched = result.getContent().get(0);
            assertThat(fetched.getUser().getFirstName()).isEqualTo("Pedro");
            assertThat(fetched.getPreferredStores()).hasSize(1);
            assertThat(fetched.getPreferredStores().get(0).getName()).isEqualTo("Sucursal Centro");
        }

        @Test
        void shouldRespectPagination(){
            //given
            persistEmployee("Pedro", "Perez", "Y8190012J");
            persistEmployee("Maria", "Segovia", "X1234567A");
            persistEmployee("Ana", "Lopez", "Z9876543B");
            entityManager.clear();
            //when
            Page<Employee> firstPage = employeeRepository.findAll(PageRequest.of(0, 2));
            Page<Employee> secondPage = employeeRepository.findAll(PageRequest.of(1, 2));
            //then
            assertThat(firstPage.getContent()).hasSize(2);
            assertThat(firstPage.getTotalElements()).isEqualTo(3);
            assertThat(firstPage.getTotalPages()).isEqualTo(2);
            assertThat(secondPage.getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("findByIdWithUser")
    class FindByIdWithUser {
        @Test
        void shouldReturnEmployee_whenIdExists(){
            //given
            Employee employeePedro = persistEmployee("Pedro", "Perez", "Y8190012J");
            entityManager.clear();
            //when
            Optional<Employee> result = employeeRepository.findByIdWithUser(employeePedro.getId());
            //then
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(employeePedro.getId());
            assertThat(result.get().getUser().getFirstName()).isEqualTo("Pedro");
        }

        @Test
        void shouldReturnEmpty_whenEmployeeDoesNotExist(){
            //given
            persistEmployee("Pedro", "Perez", "Y8190012J");
            entityManager.clear();
            //when
            Optional<Employee> result = employeeRepository.findByIdWithUser(UUID.randomUUID());
            //then
            assertThat(result).isEmpty();
        }

        @Test
        void shouldNotReturnAnotherEmployee(){
            //given
            Employee employeePedro = persistEmployee("Pedro", "Perez", "Y8190012J");
            persistEmployee("Maria", "Segovia", "X1234567A");
            entityManager.clear();
            //when
            Optional<Employee> result = employeeRepository.findByIdWithUser(employeePedro.getId());
            //then
            assertThat(result).isPresent();
            assertThat(result.get().getUser().getFirstName()).isEqualTo("Pedro");
            assertThat(result.get().getUser().getLastName()).isEqualTo("Perez");
        }

        @Test
        void shouldFetchPreferredStores(){
            //given
            Employee employeePedro = persistEmployee("Pedro", "Perez", "Y8190012J");
            Store store = persistStore("Sucursal Centro");
            employeePedro.addStore(store);
            entityManager.persistAndFlush(employeePedro);
            entityManager.clear();
            //when
            Optional<Employee> result = employeeRepository.findByIdWithUser(employeePedro.getId());
            //then
            assertThat(result).isPresent();
            assertThat(result.get().getPreferredStores()).hasSize(1);
            assertThat(result.get().getPreferredStores().get(0).getName()).isEqualTo("Sucursal Centro");
        }
    }

}
