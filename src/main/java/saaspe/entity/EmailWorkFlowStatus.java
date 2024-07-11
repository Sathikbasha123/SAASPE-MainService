package saaspe.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Data
@Table(name = "email_workflow_status")
public class EmailWorkFlowStatus {

    @Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "NUMBER")
	private Long workFlowNumber;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "NAME")
    private String name;

	@Column(name = "RANGE")
	private String range;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "APPLCATION_NAME")
    private String applicationName;

    @Column(name = "APPLCATION_ID")
    private String applicationId;

    @Column(name = "OPID")
	private String opID;

	@Column(name = "BUID")
	private String buID;

	@Column(name = "CREATED_ON")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date createdOn;

	@Column(name = "UPDATED_ON")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date updatedOn;

	@Column(name = "CREATED_BY")
	private String createdBy;

	@Column(name = "UPDATED_BY")
	private String updatedBy;
    
}
