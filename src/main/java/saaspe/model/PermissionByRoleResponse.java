package saaspe.model;

import lombok.Data;

@Data
public class PermissionByRoleResponse {
	private String name;

	private Boolean edit;

	private Boolean add;

	private Boolean view;

	private Boolean delete;

	private Boolean review;

	private Boolean approve;
}
