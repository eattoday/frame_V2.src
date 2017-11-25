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
import java.util.List;

@Service("updateDispatch")
public class UpdateDispatch implements IWorkflowProcessor {
    @Resource
    private IWorkflowBaseService workflowBaseService;

    @Resource
    private IBaseDAO baseDAO;


    @Override
    public void execute(TaskInstance taskInstance, List<Participant> participants, Object entity, UserEntity userEntity,
                        String params) throws ServiceException {
        if (entity instanceof GeneralInfoModel) {
            try {
                int  isUpdare = 0;
                DisCommonModel dispatchEntity = (DisCommonModel) baseDAO.get(Class.forName(Constants.DIS_MODEL), ((GeneralInfoModel) entity).getDispatchId());
                List<FdbkCommonModel> fdbkList = baseDAO.find("from "+Constants.FDBK_MODEL+" where dispatchId="+dispatchEntity.getObjectId());
                List<GeneralInfoModel> generalInfoModels = baseDAO.find("from GeneralInfoModel where dispatchId="+((GeneralInfoModel) entity).getDispatchId()+" and processingStatus='Y' "+Constants.LAST_GEN_SUN_SQL);
                if(dispatchEntity.getShouldNum()!=null&&fdbkList.size()>0&&generalInfoModels.size()>= dispatchEntity.getShouldNum()){
                    for(FdbkCommonModel fdbc:fdbkList){
                        List<GeneralInfoModel> genModels = baseDAO.find("from GeneralInfoModel where dispatchId="+((GeneralInfoModel) entity).getDispatchId()+" and processingObjectID="+fdbc.getObjectId()+" "+Constants.LAST_GEN_SUN_SQL+" ORDER BY objectId DESC");
                        if(genModels.size()>0&&genModels.get(0).getProcessingStatus().equals("Y")){
                            isUpdare++;
                        }
                    }
                }
                if (isUpdare>=dispatchEntity.getShouldNum()) {
                    dispatchEntity.setAllWhether("是");
                    baseDAO.update(dispatchEntity);
                }

            } catch (Exception e) {
                throw new ServiceException(e);
            }
        }
    }
}