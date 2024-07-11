package saaspe.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor
@Entity
@Data
@Table(name = "saaspe_category_master")
public class ApplicationCategoryMaster {

    @Id
    @Column(name = "CATEGORY_ID")
    private String categoryId;

    @NonNull
    @Column(name = "CATEGORY_NAME")
    private String categoryName;

    @Column(name = "CREATED_ON")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date createdOn;

    @Column(name = "UPDATED_ON")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date updatedOn;

    @NonNull
    @Column(name = "CREATED_BY")
    private String createdBy;

    @NonNull
    @Column(name = "UPDATED_BY")
    private String updatedBy;

    @NonNull
    @Column(name = "BUID")
    private String buID;

    @NonNull
    @Column(name = "OPID")
    private String opID = "SAASPE";

    @JsonIgnore
    @OneToMany(mappedBy = "categoryId")
    private List<ApplicationDetails> saaspeApplicationDetails = new ArrayList<>();
}
