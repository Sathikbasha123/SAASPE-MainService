package saaspe.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import saaspe.entity.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {

	RefreshToken findByRefreshToken(String refreshToken);

}
