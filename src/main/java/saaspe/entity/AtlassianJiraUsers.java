package saaspe.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Entity
@Table(name = "saaspe_atlassian_users")

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class AtlassianJiraUsers {
	@Id
	@Column(name ="account_id")
	private String accountId;

	@Column(name ="user_email")
	private String userEmail;
}
