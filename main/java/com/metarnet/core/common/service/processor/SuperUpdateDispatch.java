package com.metarnet.core.common.service.processor;


import com.metarnet.core.common.dao.IBaseDAO;
import com.metarnet.core.common.exception.ServiceException;
import com.metarnet.core.common.model.DisCommonModel;
import com.metarnet.core.common.model.FdbkCommonModel;
import com.metarnet.core.common.model.GeneralInfoModel;
import com.metarnet.core.common.service.IWorkflowBaseService;
import com.metarnet.core.common.service.IWorkflowProcessor;
import com.metarnet.core.common.utils.Constants;
import com.metarnet.core.common.workflow.Participant;
import com.metarnet.core.common.workflow.TaskInstance;
import com.ucloud.paas.proxy.aaaa.entity.UserEntity;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service("superUpdateDispatch")
public class SuperUpdateDispatch implements IWorkflowProcessor {
    @Resource
    private IWorkflowBaseService workflowBaseService;

    @Resource
    private IBaseDAO baseDAO;

    @Override
    public void execute(TaskInstance taskInstance, List<Participant> participants, Object entity, UserEntity userEntity,
                        String params) throws ServiceException {
        if(entity instanceof GeneralInfoModel){
            try {
                DisCommonModel dispatchEntity = (DisCommonModel) baseDAO.get(Class.forName(Constants.DIS_MODEL), ((GeneralInfoModel) entity).getDispatchId());
                List<GeneralInfoModel> generalInfoModels = baseDAO.find("from GeneralInfoModel where dispatchId="+((GeneralInfoModel) entity).getDispatchId()+" and createdBy="+userEntity.getUserId()+" ORDER BY objectId DESC");
                if(dispatchEntity.getShouldNum()!=null&&generalInfoModels.size()>0&&generalInfoModels.get(0).getProcessingStatus().equals("N")){
                        dispatchEntity.setAllWhether("");
                        baseDAO.update(dispatchEntity);

                }

            } catch (Exception e) {
                throw new ServiceException(e);
            }
        }

    }

}