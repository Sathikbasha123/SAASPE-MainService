package saaspe.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import saaspe.entity.ApplicationCategoryMaster;
import saaspe.model.CommonResponse;
import saaspe.model.Response;
import saaspe.model.categoryMasterResponse;
import saaspe.repository.ApplicationCategoryMasterRepository;
import saaspe.service.CategoryService;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private ApplicationCategoryMasterRepository applicationCategoryMasterRepository;

    @Override
    public CommonResponse getApplicationCategoryDetails() {
        CommonResponse commonResponse = new CommonResponse();
        Response response = new Response();
        List<categoryMasterResponse> list = new ArrayList<>();
        List<ApplicationCategoryMaster> categorys = applicationCategoryMasterRepository.findAll();
        for (ApplicationCategoryMaster categoryMaster : categorys) {
            categoryMasterResponse categoryResponse = new categoryMasterResponse();
            categoryResponse.setCategoryId(categoryMaster.getCategoryId());
            categoryResponse.setCategoryName(categoryMaster.getCategoryName());
            list.add(categoryResponse);
            response.setData(list);
        }
        response.setAction("categoryMasterResponse");
        commonResponse.setStatus(HttpStatus.OK);
        commonResponse.setMessage("Details retrieved successfully");
        commonResponse.setResponse(response);
        return commonResponse;
    }

}
