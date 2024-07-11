package saaspe.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UserRolesUtils implements UserDetails {
	private static final long serialVersionUID = 1L;
	private Collection<? extends GrantedAuthority> authorities;

	public static UserRolesUtils build(saaspe.entity.UserDetails user) {
		List<GrantedAuthority> authorities = new ArrayList<>();
		SimpleGrantedAuthority authority = new SimpleGrantedAuthority(user.getUserRole());
		authorities.add(authority);
		return new UserRolesUtils(authorities);
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	public UserRolesUtils(Collection<? extends GrantedAuthority> authorities) {
		super();
		this.authorities = authorities;
	}

	@Override
	public String getPassword() {

		return null;
	}

	@Override
	public String getUsername() {

		return null;
	}

	@Override
	public boolean isAccountNonExpired() {

		return false;
	}

	@Override
	public boolean isAccountNonLocked() {

		return false;
	}

	@Override
	public boolean isCredentialsNonExpired() {

		return false;
	}

	@Override
	public boolean isEnabled() {

		return false;
	}

}
