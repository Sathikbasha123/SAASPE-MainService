package saaspe.model;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class MultiCloudCountResponse extends Response implements Serializable {

	private static final long serialVersionUID = 1L;

	private String vendorName;
	private String logo;
	private List<MultiCloudOverviewResponse> service;
}