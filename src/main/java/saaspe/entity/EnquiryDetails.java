package saaspe.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ENQUIRY_DETAILS")
public class EnquiryDetails {

	@Id
	@Column(name = "enquiry_id")
	private String enquiryId;

	@Column(name = "NAME")
	private String name;

	@Column(name = "NUMBER")
	private String number;

	@Column(name = "MESSAGE")
	private String message;

	@Column(name = "EMAIL")
	private String email;

	@Column(name = "COMPANY_NAME")
	private String companyName;

	@Column(name = "DESIGNATION")
	private String designation;

	@NonNull
	@Column(name = "CREATED_ON")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date createdOn;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@Column(name = "START_DATE")
	private Date startDate;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@Column(name = "END_DATE")
	private Date endDate;

	@Column(name = "UPDATED_ON")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date updatedOn;

}
