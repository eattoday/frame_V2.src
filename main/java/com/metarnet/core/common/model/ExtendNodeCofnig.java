package com.metarnet.core.common.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.metarnet.core.common.adapter.WorkflowAdapter;
import com.metarnet.core.common.client.WFServiceClient;
import com.metarnet.core.common.exception.AdapterException;
import com.metarnet.core.common.exception.ServiceException;
import com.metarnet.core.common.utils.Constants;
import org.apache.commons.digester.Digester;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.StringReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA. User: metarnet Date: 13-3-13 Time: 下午4:00
 * 解析流程引擎扩展信息
 */
public class ExtendNodeCofnig {
    public static final String COMPONENT = "component";
    public static final String LINKS = "links";
    public static final String POSTPROCESSOR = "postProcessor";
    public static final String PREPROCESSOR = "preProcessor";
    public static final String DRAFT = "draft";
    public static final String AREANAME = "areaName";
    public final static String SHOWSUBFLOW = "showSubflow";
    public static final String FEEDBACKABSTRACTROLEID = "feedbackAbstractRoleId";
    public static final String NEEDAPPROVAL = "needApproval";

    private static Map<String, Map<String, ActivityModel>> activityModelMap = new ConcurrentHashMap();
    private static Logger logger = Logger.getLogger("ExtendNodeCofnig");

    public static ActivityModel parseActivity(String cloudAccountId, String processModelName,
                                              String activityDefID, String processModelId) throws ServiceException, AdapterException {
        //代码健壮性
        if (StringUtils.isEmpty(activityDefID)) {
            activityDefID = Constants.START_ACTIVITY;
        }
        // 先读缓存
//        if (activityModelMap.containsKey(processModelName) && activityModelMap.get(processModelName).containsKey(activityDefID)) {
//            return activityModelMap.get(processModelName).get(activityDefID);
//        }

        ActivityModel activityModel = null;
        AreaModel areaModel = null;

        if ("PLATFORM".equals(Constants.TODO_ANALYSIS_TYPE)) {
            //解析单个环节的配置
            activityModel = getActivitySetting(processModelName, activityDefID, processModelId);
            areaModel = new AreaModel();
        } else {
            //解析单个环节的配置
            activityModel = parseActivity(WorkflowAdapter.getActivityExtendAttributes(cloudAccountId, processModelName, activityDefID));
            //解析流程模板全局配置
            areaModel = parseProcess(WorkflowAdapter.getActivityExtendAttributes(cloudAccountId, processModelName, null));
            activityModel.getShowLinkList().addAll(areaModel.getShowLinkList());
            activityModel.getEditLinkList().addAll(areaModel.getEditLinkList());
        }

        if (StringUtils.isEmpty(activityModel.getAreaName())) {
            if (null == areaModel.getAreaName() && !Constants.START_ACTIVITY.equals(activityDefID)) {
                logger.info("当前环节没有配置areaName。processModelName=" + processModelName + " activityDefID=" + activityDefID);
            }
            activityModel.setAreaName(areaModel.getAreaName());
        }
        // 加入缓存
        if (!activityModelMap.containsKey(processModelName)) {
            HashMap<String, ActivityModel> modelHashMap = new HashMap();
            activityModelMap.put(processModelName, modelHashMap);
        }
        if (!activityModelMap.get(processModelName).containsKey(activityDefID)) {
            activityModelMap.get(processModelName).put(activityDefID,
                    activityModel);
        }
        return activityModel;
    }

    public static ActivityModel parseActivity(String cloudAccountId, String processModelName,
                                              String activityDefID) throws ServiceException, AdapterException {
        return parseActivity(cloudAccountId, processModelName, activityDefID, null);
    }

    /**
     * 解析环节配置信息
     *
     * @param extendAttributes 配置信息
     * @return
     * @throws ServiceException
     */
    private static ActivityModel parseActivity(String extendAttributes)
            throws ServiceException {
        Digester digester = new Digester();
        digester.addObjectCreate("extendNodes", ArrayList.class);
        digester.addObjectCreate("extendNodes/extendNode", ExtendNode.class);
        digester.addBeanPropertySetter("extendNodes/extendNode/key");
        digester.addBeanPropertySetter("extendNodes/extendNode/value");
        digester.addBeanPropertySetter("extendNodes/extendNode/desc");
        digester.addSetNext("extendNodes/extendNode", "add");
        try {
            List<ExtendNode> extendNodes = (List<ExtendNode>) digester
                    .parse(new StringReader(extendAttributes));
            ActivityModel activityModel = new ActivityModel();
            for (ExtendNode extendNode : extendNodes) {
                if (StringUtils.isNotEmpty(extendNode.getKey()) && COMPONENT.equals(extendNode.getKey())) {
                    activityModel.setComponent(new ComponentModel(extendNode.getValue(), ComponentModel.SHOW,
                            DRAFT.equals(extendNode.getDesc())));
                    continue;
                }
                if (StringUtils.isNotEmpty(extendNode.getKey()) && LINKS.equals(extendNode.getKey())) {
                    activityModel.addLink(new Links(extendNode.getValue(), StringUtils.isEmpty(extendNode.getDesc()) ? Links.SHOW :
                            extendNode.getDesc()));
                    continue;
                }
                if (StringUtils.isNotEmpty(extendNode.getKey()) && POSTPROCESSOR.equals(extendNode.getKey())) {
                    for (String name : extendNode.getValue().split(",")) {
                        activityModel.getPostProcessorList().add(new ProcessorModel(name, extendNode.getDesc()));
                    }
                    continue;
                }
                if (StringUtils.isNotEmpty(extendNode.getKey()) && PREPROCESSOR.equals(extendNode.getKey())) {
                    for (String name : extendNode.getValue().split(",")) {
                        activityModel.getPreProcessorList().add(new ProcessorModel(name, extendNode.getDesc()));
                    }
                    continue;
                }
                if (StringUtils.isNotEmpty(extendNode.getKey()) && SHOWSUBFLOW.equals(extendNode.getKey())) {
                    activityModel.setShowSubflow(extendNode.getValue());
                    continue;
                }
                if (StringUtils.isNotEmpty(extendNode.getKey()) && AREANAME.equals(extendNode.getKey())) {
                    activityModel.setAreaName(extendNode.getValue());
                    continue;
                }
                if (StringUtils.isNotEmpty(extendNode.getKey()) && NEEDAPPROVAL.equals(extendNode.getKey())) {

                    activityModel.setNextApproverModel(new NextApproverModel(extendNode.getValue(), extendNode.getDesc()));
                    continue;
                }
                if (StringUtils.isNotEmpty(extendNode.getKey()) && FEEDBACKABSTRACTROLEID.equals(extendNode.getKey())) {
                    activityModel.setFeedbackAbstractRoleId(extendNode.getValue());
                }
            }
            return activityModel;
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    /**
     * 解析流程配置信息
     *
     * @param extendAttributes 配置信息
     * @return
     * @throws ServiceException
     */
    public static AreaModel parseProcess(String extendAttributes) throws ServiceException {
        Digester digester = getDigester();
        try {
            List<ExtendNode> extendNodes = (List<ExtendNode>) digester.parse(new StringReader(extendAttributes));
            AreaModel areaModel = new AreaModel();
            for (ExtendNode extendNode : extendNodes) {
                if (StringUtils.isNotEmpty(extendNode.getKey()) && AREANAME.equals(extendNode.getKey()))
                    areaModel.setAreaName(extendNode.getValue());
                if (StringUtils.isNotEmpty(extendNode.getKey()) && LINKS.equals(extendNode.getKey())) {
                    areaModel.addLink(new Links(extendNode.getValue(), StringUtils.isEmpty(extendNode.getDesc()) ? Links.SHOW :
                            extendNode.getDesc()));
                    continue;
                }
            }
            return areaModel;
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    /**
     * 获得解析规则
     *
     * @return
     */
    private static Digester getDigester() {
        Digester digester = new Digester();
        digester.addObjectCreate("extendNodes", ArrayList.class);
        digester.addObjectCreate("extendNodes/extendNode", ExtendNode.class);
        digester.addBeanPropertySetter("extendNodes/extendNode/key");
        digester.addBeanPropertySetter("extendNodes/extendNode/value");
        digester.addBeanPropertySetter("extendNodes/extendNode/desc");
        digester.addSetNext("extendNodes/extendNode", "add");
        return digester;
    }

    public static void reload() {
        activityModelMap.clear();
    }


    /**
     * 获取流程平台节点配置信息
     * add by wangzwty@inspur.com
     * 2017-02-06
     */
    public static ActivityModel getActivitySetting(String processModelName, String activityDefID, String processModelId) {
        ActivityModel activityModel = new ActivityModel();
        String settingStr = WFServiceClient.getInstance().getNodeSetting(processModelName, activityDefID, processModelId);
        if (!"null".equals(settingStr) && !"".equals(settingStr) && settingStr != null) {

            JSONObject setting = JSON.parseObject(settingStr);
            String areaName = setting.getString("areaName");
            String component = setting.getString("component");
            String editLinks = setting.getString("editLinks") == null ? "" : setting.getString("editLinks");
            String showLinks = setting.getString("showLinks") == null ? "" : setting.getString("showLinks");

            String preProcess = setting.getString("preProcessor") == null ? "" : setting.getString("preProcessor");
            String postProcess = setting.getString("postProcessor") == null ? "" : setting.getString("postProcessor");

            activityModel.setAreaName(areaName);

            ComponentModel componentModel = new ComponentModel();
            componentModel.setComponent(component);
            activityModel.setComponent(componentModel);

            List<String> editLinkList = new ArrayList<String>();
            editLinkList.addAll(Arrays.asList(editLinks.split(",")));
            activityModel.setEditLinkList(editLinkList);

            List<String> showLinkList = new ArrayList<String>();
            showLinkList.addAll(Arrays.asList(showLinks.split(",")));
            activityModel.setShowLinkList(showLinkList);

            List<ProcessorModel> preProcessorList = new ArrayList<ProcessorModel>();
            if (!"".equals(preProcess)) {
                for (String processor : Arrays.asList(preProcess.split(","))) {
                    ProcessorModel processorModel = new ProcessorModel(processor, null);
                    preProcessorList.add(processorModel);
                }
                activityModel.setPreProcessorList(preProcessorList);
            }
            List<ProcessorModel> postProcessorList = new ArrayList<ProcessorModel>();
            if (!"".equals(postProcess)) {
                for (String processor : Arrays.asList(postProcess.split(","))) {
                    ProcessorModel processorModel = new ProcessorModel(processor, null);
                    postProcessorList.add(processorModel);
                }
                activityModel.setPostProcessorList(postProcessorList);
            }
        }
        return activityModel;
    }

}
