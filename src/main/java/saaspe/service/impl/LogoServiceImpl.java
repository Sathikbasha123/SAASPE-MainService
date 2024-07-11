package saaspe.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import saaspe.entity.ApplicationLogoEntity;
import saaspe.model.ApplicatoinsLogoResponse;
import saaspe.model.CommonResponse;
import saaspe.model.Response;
import saaspe.repository.ApplicationLogoRepository;
import saaspe.service.LogoService;

@Service
public class LogoServiceImpl implements LogoService {

    @Autowired
    private ApplicationLogoRepository applicationLogoRepository;

    @Override
    public CommonResponse getApplicatoinLogoDetails() {
        CommonResponse commonResponse = new CommonResponse();
        Response response = new Response();
        List<ApplicatoinsLogoResponse> list = new ArrayList<>();
        List<ApplicationLogoEntity> applicationLogoEntity = applicationLogoRepository.findAll();
        for (ApplicationLogoEntity logoEntity : applicationLogoEntity) {
            ApplicatoinsLogoResponse logoResponse = new ApplicatoinsLogoResponse();
            logoResponse.setProviderID(logoEntity.getProviderId().getProviderId());
            logoResponse.setApplicationDescription(logoEntity.getDescription());
            logoResponse.setApplicationName(logoEntity.getApplicationName());
            logoResponse.setApplicationPageURL(logoEntity.getApplicationPageUrl());
            logoResponse.setLogoURL(logoEntity.getLogoUrl());
            list.add(logoResponse);
            response.setData(list);
        }
        response.setAction("logoMasterResponse");
        commonResponse.setStatus(HttpStatus.OK);
        commonResponse.setMessage("Details retrieved successfully");
        commonResponse.setResponse(response);
        return commonResponse;
    }

}
