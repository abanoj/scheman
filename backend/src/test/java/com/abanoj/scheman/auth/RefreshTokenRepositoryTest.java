package com.abanoj.scheman.auth;

import com.abanoj.scheman.auth.entity.RefreshToken;
import com.abanoj.scheman.auth.entity.Role;
import com.abanoj.scheman.auth.entity.User;
import com.abanoj.scheman.auth.repository.RefreshTokenRepository;
import com.abanoj.scheman.config.JpaAuditingConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaAuditingConfig.class)
public class RefreshTokenRepositoryTest {
    @Autowired
    RefreshTokenRepository refreshTokenRepository;
    @Autowired
    TestEntityManager testEntityManager;

    private User persistUser(String firstname, String lastname){
        String email = firstname.toLowerCase() + "." + lastname.toLowerCase() + "@mail.com";
        String password = firstname.repeat(2);
        User user = User.builder()
                .firstName(firstname)
                .lastName(lastname)
                .email(email)
                .password(password)
                .role(Role.EMPLOYEE)
                .build();
        testEntityManager.persistAndFlush(user);
        return user;
    }

    private RefreshToken persistToken(String token, User user, Instant expiryDate, boolean revoked){
        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .user(user)
                .expiryDate(expiryDate)
                .revoked(revoked)
                .build();
        testEntityManager.persistAndFlush(refreshToken);
        return refreshToken;
    }


    @Nested
    @DisplayName("revokeAllByUser")
    class RevokeAllByUser{
        @Test
        void shouldRevokeAllTokensByUser(){
            //given
            User user = persistUser("Jesus", "Abano");
            RefreshToken token1 = persistToken("test-123", user, Instant.now().minusSeconds(3600), false);
            RefreshToken token2 = persistToken("test-456", user, Instant.now().minusSeconds(1800), false);
            testEntityManager.clear();
            //when
            refreshTokenRepository.revokeAllByUser(user);
            //then
            RefreshToken find1 = testEntityManager.find(RefreshToken.class, token1.getId());
            RefreshToken find2 = testEntityManager.find(RefreshToken.class, token2.getId());
            assertThat(find1.isRevoked()).isTrue();
            assertThat(find2.isRevoked()).isTrue();
        }

        @Test
        void shouldOnlyRevokeTokens_whenTokensBelongToUser(){
            //given
            User user = persistUser("Jesus", "Abano");
            User pedro = persistUser("Pedro", "Perez");
            RefreshToken token1 = persistToken("test-123", user, Instant.now().minusSeconds(3600), false);
            RefreshToken token2 = persistToken("test-456", pedro, Instant.now().minusSeconds(1800), false);
            testEntityManager.clear();
            //when
            refreshTokenRepository.revokeAllByUser(user);
            //then
            RefreshToken find1 = testEntityManager.find(RefreshToken.class, token1.getId());
            RefreshToken find2 = testEntityManager.find(RefreshToken.class, token2.getId());
            assertThat(find1.isRevoked()).isTrue();
            assertThat(find2.isRevoked()).isFalse();
        }

        @Test
        void shouldNotRevokeTokens_whenUserHasNoTokens(){
            //given
            User user = persistUser("Jesus", "Abano");
            User pedro = persistUser("Pedro", "Perez");
            RefreshToken token1 = persistToken("test-123", pedro, Instant.now().minusSeconds(3600), false);
            RefreshToken token2 = persistToken("test-456", pedro, Instant.now().minusSeconds(1800), false);
            testEntityManager.clear();
            //when
            refreshTokenRepository.revokeAllByUser(user);
            //then
            RefreshToken find1 = testEntityManager.find(RefreshToken.class, token1.getId());
            RefreshToken find2 = testEntityManager.find(RefreshToken.class, token2.getId());
            assertThat(find1.isRevoked()).isFalse();
            assertThat(find2.isRevoked()).isFalse();
        }

        @Test
        void shouldRevokeOnlyActiveTokens(){
            //given
            User user = persistUser("Jesus", "Abano");
            RefreshToken activeToken = persistToken("test-123", user, Instant.now().minusSeconds(3600), false);
            RefreshToken revokedToken = persistToken("test-456", user, Instant.now().minusSeconds(1800), true);
            testEntityManager.clear();
            //when
            refreshTokenRepository.revokeAllByUser(user);
            //then
            RefreshToken find1 = testEntityManager.find(RefreshToken.class, activeToken.getId());
            RefreshToken find2 = testEntityManager.find(RefreshToken.class, revokedToken.getId());
            assertThat(find1.isRevoked()).isTrue();
            assertThat(find2.isRevoked()).isTrue();
        }

    }

    @Nested
    @DisplayName("deleteAllByRevokedTrue")
    class DeleteAllByRevokedTrue{
        @Test
        void shouldDeleteAllTokens_whenTokensAreExpired(){
            //given
            User user = persistUser("Jesus", "Abano");
            User anotherUser = persistUser("Juan", "Bimba");
            RefreshToken token1 = persistToken("test-123", user, Instant.now().minusSeconds(3600), false);
            RefreshToken token2 = persistToken("test-456", anotherUser, Instant.now().minusSeconds(3600), false);
            testEntityManager.clear();
            //when
            int result = refreshTokenRepository.deleteAllByRevokedTrue(Instant.now());
            //then
            assertThat(result).isEqualTo(2);
            assertThat(testEntityManager.find(RefreshToken.class, token1.getId())).isNull();
            assertThat(testEntityManager.find(RefreshToken.class, token2.getId())).isNull();
        }

        @Test
        void shouldDeleteAllTokens_whenTokensAreRevoked(){
            //given
            User user = persistUser("Jesus", "Abano");
            RefreshToken token1 = persistToken("test-123", user, Instant.now().plusSeconds(3600), true);
            RefreshToken token2 = persistToken("test-456", user, Instant.now().plusSeconds(3600), true);
            testEntityManager.clear();
            //when
            int result = refreshTokenRepository.deleteAllByRevokedTrue(Instant.now());
            //then
            assertThat(result).isEqualTo(2);
            assertThat(testEntityManager.find(RefreshToken.class, token1.getId())).isNull();
            assertThat(testEntityManager.find(RefreshToken.class, token2.getId())).isNull();
        }

        @Test
        void shouldNotDeleteValidTokens(){
            //given
            User user = persistUser("Jesus", "Abano");
            RefreshToken token1 = persistToken("test-123", user, Instant.now().plusSeconds(3600), false);
            RefreshToken token2 = persistToken("test-456", user, Instant.now().plusSeconds(3600), false);
            testEntityManager.clear();
            //when
            int result = refreshTokenRepository.deleteAllByRevokedTrue(Instant.now());
            //then
            assertThat(result).isZero();
            assertThat(testEntityManager.find(RefreshToken.class, token1.getId())).isNotNull();
            assertThat(testEntityManager.find(RefreshToken.class, token2.getId())).isNotNull();
        }

        @Test
        void shouldDeleteOnlyInvalidTokens_whenDatabaseHasMixedTokens(){
            //given
            User user = persistUser("Jesus", "Abano");
            RefreshToken expiredToken = persistToken("test-123", user, Instant.now().minusSeconds(3600), false);
            RefreshToken revokedToken = persistToken("test-456", user, Instant.now().plusSeconds(3600), true);
            RefreshToken validToken = persistToken("test-789", user, Instant.now().plusSeconds(3600), false);
            testEntityManager.clear();
            //when
            int result = refreshTokenRepository.deleteAllByRevokedTrue(Instant.now());
            //then
            assertThat(result).isEqualTo(2);
            assertThat(testEntityManager.find(RefreshToken.class, expiredToken.getId())).isNull();
            assertThat(testEntityManager.find(RefreshToken.class, revokedToken.getId())).isNull();
            assertThat(testEntityManager.find(RefreshToken.class, validToken.getId())).isNotNull();
        }

    }

}
