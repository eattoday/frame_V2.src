package com.metarnet.core.common.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SimpleDateFormatSerializer;
import com.metarnet.core.common.adapter.AAAAAdapter;
import com.metarnet.core.common.controller.editor.*;
import com.metarnet.core.common.exception.UIException;
import com.ucloud.paas.proxy.aaaa.AAAAService;
import com.ucloud.paas.proxy.aaaa.entity.UserEntity;
import com.ucloud.paas.proxy.aaaa.util.PaasAAAAException;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Created with IntelliJ IDEA. User: metarnet Date: 13-2-26 Time: 下午7:54
 * Controller基类,提供公共方法
 */
public class BaseController {
    protected Logger logger = Logger.getLogger("BaseController");


    protected static final SerializeConfig ser = new SerializeConfig();



//    protected static final JsonConfig config = new JsonConfig();
    /**
     * 工作台定义的当前用户变量名
     */
    public static final String globalUniqueID = "globalUniqueID";

    /**
     * session中当前用户变量对象
     */
    public static final String globalUniqueUser = "globalUniqueUser";

    static {
        /*config.setIgnoreDefaultExcludes(false);
        config.registerJsonValueProcessor(Date.class, new JsonTimestampToStringUtil());
        config.registerJsonValueProcessor(Timestamp.class, new JsonTimestampToStringUtil());*/
        //	config.registerJsonValueProcessor(Timestamp.class, new JsDateJsonValueProcessor());
        //	config.registerJsonValueProcessor(Date.class, new JsDateJsonValueProcessor());

        ser.put(Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
        ser.put(Timestamp.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
    }

    @InitBinder
    protected void initBinder(ServletRequestDataBinder binder) throws UIException {
        binder.registerCustomEditor(Date.class, new DateEdit());
        binder.registerCustomEditor(int.class, new IntEdit());
        binder.registerCustomEditor(long.class, new LongEdit());
        binder.registerCustomEditor(Timestamp.class, new TimeStampEdit());
    }

    /**
     * 返回当前登录人
     *
     * @param request
     * @return
     * @throws UIException
     */
    public UserEntity getUserEntity(HttpServletRequest request) throws UIException {
        if (null != request.getSession().getAttribute(globalUniqueUser)&&!"".equals(request.getSession().getAttribute(globalUniqueUser))) {
            return (UserEntity) request.getSession().getAttribute(globalUniqueUser);
        }

        String globalUniqueID = (String) request.getSession().getAttribute("globalUniqueID");
        if (globalUniqueID == null || "".equals(globalUniqueID)) {
            globalUniqueID = request.getParameter("globalUniqueID");
        }
        try {
            UserEntity userEntity = AAAAAdapter.getInstence().findUserBySessionID(globalUniqueID);
            request.getSession().setAttribute(globalUniqueUser, userEntity);
            request.getSession().setAttribute("globalUniqueID", globalUniqueID);
            return userEntity;
        } catch (PaasAAAAException e) {
            //e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

//	Integer userId = getUserId(request);
//	logger.debug("userId is '" + userId + "'");
//	if (userId != null && userId != 0) {
//	    try {
//		UserEntity userEntity = AAAAAdapter.getInstence().findUserbyUserID(userId);
//		request.getSession().setAttribute(globalUniqueUser, userEntity);
//		return userEntity;
//	    } catch (PaasAAAAException e) {
////		throw new UIException(null, userId, e);
//	    }
//	}
        return null;
//	throw new UIException(null, userId, new Exception("没有获取到当前用户,userId=" + userId));
    }

    /**
     * Controller结束前处理 ,处理逻辑 1.将json输出给log4j 2.response写入json 3.记录到PAAS
     *
     * @param response
     * @param request
     * @param json     传给前台的json对象
     * @param refBizId 业务流水号
     * @throws java.io.IOException
     */
    protected void endHandle(HttpServletRequest request, HttpServletResponse response, JSON json, String refBizId)
            throws UIException {
        endHandle(request, response, json, refBizId, true);
    }

    protected void endHandle(HttpServletRequest request, HttpServletResponse response, String json, String refBizId)
            throws UIException {
        endHandle(request, response, json, refBizId, true);
    }
    /**
     * Controller结束前处理 ,处理逻辑 1.将json输出给log4j 2.response写入json 3.记录到PAAS
     *
     * @param response
     * @param request
     * @param json     传给前台的json对象
     * @param refBizId 业务流水号
     * @throws java.io.IOException
     */
    protected void endHandle4activiti(HttpServletRequest request, HttpServletResponse response, JSON json, String refBizId)
            throws UIException {
        endHandle4activiti(request, response, json, refBizId, true);
    }

    protected void endHandle4activiti(HttpServletRequest request, HttpServletResponse response, String json, String refBizId)
            throws UIException {
        endHandle4activiti(request, response, json, refBizId, true);
    }

    /**
     * Controller结束前处理 ,不记录日志 1.将json输出给log4j 2.response写入json
     *
     * @param response
     * @param request
     * @param json     传给前台的json对象
     * @param refBizId 业务流水号
     * @throws java.io.IOException
     */
    protected void endNotLogHandle(HttpServletRequest request, HttpServletResponse response, JSON json, String refBizId)
            throws UIException {
        endHandle(request, response, json, refBizId, false);
    }

    /**
     * Controller结束前处理 ,处理逻辑 1.将json输出给log4j 2.response写入json 3.记录到PAAS
     *
     * @param response
     * @param request
     * @param json     传给前台的json对象
     * @param refBizId 业务流水号
     * @throws java.io.IOException
     */
    private void endHandle(HttpServletRequest request, HttpServletResponse response, JSON json, String refBizId,
                           boolean write) throws UIException {

        endHandle(request, response, json == null ? StringUtils.EMPTY : json.toString(), refBizId, write);

    }
    /**
     * Controller结束前处理 ,处理逻辑 1.将json输出给log4j 2.response写入json 3.记录到PAAS
     *
     * @param response
     * @param request
     * @param json     传给前台的json对象
     * @param refBizId 业务流水号
     * @throws java.io.IOException
     */
    private void endHandle4activiti(HttpServletRequest request, HttpServletResponse response, JSON json, String refBizId,
                           boolean write) throws UIException {

        endHandle4activiti(request, response, json == null ? StringUtils.EMPTY : json.toString(), refBizId, write);

    }

    /**
     * Controller结束前处理 ,处理逻辑 1.将json输出给log4j 2.response写入json 3.记录到PAAS
     *
     * @param response
     * @param request
     * @param result   传给前台的数据
     * @param refBizId 业务流水号
     * @throws java.io.IOException
     */
    public void endHandle(HttpServletRequest request, HttpServletResponse response, String result, String refBizId,
                          boolean write) throws UIException {
        try {
            Logger.getLogger(this.getClass()).debug(result);
//            response.setContentType("application/json;charset=utf-8");
            response.setContentType("text/html;charset=utf-8");
            response.getWriter().print(result);
            response.getWriter().close();
            response.getWriter().close();
        /*if (write) {
        LogAdapter.getInstence().writeOperLog(refBizId, getUserEntity(request), this.getClass().getName(),
			Thread.currentThread().getStackTrace()[3].getMethodName(), LogAdapter.SUCCESS);
	    }*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Controller结束前处理 ,处理逻辑 1.将json输出给log4j 2.response写入json 3.记录到PAAS
     *
     * @param response
     * @param request
     * @param result   传给前台的数据
     * @param refBizId 业务流水号
     * @throws java.io.IOException
     */
    public void endHandle4activiti(HttpServletRequest request, HttpServletResponse response, String result, String refBizId,
                          boolean write) throws UIException {
        try {
            Logger.getLogger(this.getClass()).debug(result);
            response.setContentType("application/json;charset=utf-8");
//            response.setContentType("text/html;charset=utf-8");
            response.getWriter().print(result);
            response.getWriter().close();
            response.getWriter().close();
        /*if (write) {
        LogAdapter.getInstence().writeOperLog(refBizId, getUserEntity(request), this.getClass().getName(),
			Thread.currentThread().getStackTrace()[3].getMethodName(), LogAdapter.SUCCESS);
	    }*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 返回当前人ID
     *
     * @param request
     * @return
     */
    protected Integer getUserId(HttpServletRequest request) {
        try {
            String globalUniqueID = (String) request.getSession().getAttribute("globalUniqueID");
            AAAAService aaaaService = new AAAAService();
            UserEntity userEntity = aaaaService.findUserBySessionID(globalUniqueID);
            if (userEntity != null) {
                return userEntity.getUserId().intValue();
            } else {
                return null;
            }
//	    logger.debug("globalUniqueID is '" + globalUniqueID + "'");
//		HttpClient client = new HttpClient();
//		GetMethod get = new GetMethod(Constants.SESSIONURL+"?sessionId="+globalUniqueID+"&appId="+Constants.MODEL_NAME);
//		get.getParams().setContentCharset("utf-8");
////		get.getParams().setParameter("sessionId",globalUniqueID);
//		client.executeMethod(get);
//		String returnContent = get.getResponseBodyAsString();
//	    /*SessionManagement sm = new SessionManagement(globalUniqueID);
//	    SSOUserInfo ue = (SSOUserInfo) sm.getAttribute("SSOUserInfo");*/
//	    return Integer.parseInt(returnContent);
        } catch (Exception e) {
            return Integer.parseInt(request.getSession().getAttribute(globalUniqueID).toString());
        }
    }

    /**
     * 获取accountId
     *
     * @param request
     * @return
     * @throws UIException
     */
    protected String getAccountId(HttpServletRequest request) throws UIException {
        return getUserEntity(request).getUserName();
    }

    /**
     * 移除空的子节点，将节点子节点属性改为“TreeNode”,调度树使用
     *
     * @param jsonObject
     * @param treeNodes
     */
    protected void removeEmptyTreeNodes(JSONObject jsonObject, JSONArray treeNodes) {
        if (treeNodes.size() > 0) {
            for (int i = 0; i < treeNodes.size(); i++) {
                JSONObject jsonTemp = treeNodes.getJSONObject(i);
                if (jsonTemp.keySet().size() > 0) {
                    JSONArray tempTreeNodes = (JSONArray) jsonTemp.get("treeNode");
                    this.removeEmptyTreeNodes(jsonTemp, tempTreeNodes);
                }
            }
            jsonObject.put("TreeNode", treeNodes);
            jsonObject.remove("treeNode");
        } else {
            jsonObject.remove("treeNode");
        }
    }

    /**
     * 得到当前人能看到部门或人的根部门
     *
     * @param cloudOrgId
     * @return
     */
    protected String getUserRangeRootDeptId(Integer cloudOrgId) {
        // 默认显示全国的
        String rootId = "1";
        try {
//	    OrgStructure orgStructure = AAAAAdapter.getInstence().findCompanysByOrgId(cloudOrgId);
//	    if (orgStructure.getCountyCompany() != null) {
//		// 显示本班组的
//		rootId = orgStructure.getCountyCompany().getOrgId().toString();
//	    } else if (orgStructure.getCityCompany() != null) {
//		// 显示本地市的
//		rootId = orgStructure.getCityCompany().getOrgId().toString();
//	    } else if (orgStructure.getProvinceCompany() != null) {// 所属省公司
//		// 显示本省的
//		rootId = orgStructure.getProvinceCompany().getOrgId().toString();
//	    }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rootId;
    }

    /**
     * 根据request获取UserRangeRootDeptId
     *
     * @param request
     * @return
     */
    public String getUserRangeRootDeptId(HttpServletRequest request) {
        String userRangeRootDeptId = null;
        try {
            UserEntity currentUser = this.getUserEntity(request);
            if (currentUser != null) {
                return getUserRangeRootDeptId(Integer.valueOf(currentUser.getOrgID().toString()));
            }
        } catch (UIException e) {
            return null;
        }
        return userRangeRootDeptId;
    }
}
