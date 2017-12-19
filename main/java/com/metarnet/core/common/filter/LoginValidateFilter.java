package com.metarnet.core.common.filter;

import com.metarnet.core.common.adapter.AAAAAdapter;
import com.ucloud.paas.proxy.aaaa.entity.UserEntity;
import com.ucloud.paas.proxy.aaaa.util.PaasAAAAException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Administrator on 2015/7/17.
 */
public class LoginValidateFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
//        if (null == request.getSession().getAttribute("globalUniqueUser")) {
//            response.sendRedirect(request.getContextPath() + "/index.jsp");
//        }
//        String globalUniqueID = request.getParameter("globalUniqueID");
//        if (StringUtils.isNotBlank(globalUniqueID)) {
//            request.getSession().setAttribute("globalUniqueID", globalUniqueID);
//            request.getSession().setAttribute("globalUniqueUser", null);
//        }
        // 不过滤的uri
        String[] notFilter = new String[]{"index.jsp" , "forward.jsp" , "ProcessDef" , "driverController.do" ,
                "flowNodeSettingSyncController.do" , "getNodeSetting.do" , "getMyCompletedTasks.do" ,
                "updateBusiInfoByRoot" , "addLog.do" ,"flowButtonList.jsp","*.jsp","flowButtonQuery.do",
                "findNodesByDiagram.do","demoTaskSubmit.jsp","flowNodeSettingController.do","demoCompleteTask.jsp",
                "workFlowController.do","demoWorkflow.jsp","demoTask.jsp","demoController.do","demoForm.jsp",
                "frame_proxy.jsp","frame_son.jsp"

        };
        String[] notFilterMethod = new String[]{ };
        // 请求的url
        String url = request.getRequestURI();
        response.setHeader("Access-Control-Allow-Origin" , "*");
        response.setHeader("Access-Control-Allow-Methods" , "GET,POST");
        boolean doFilter = check(notFilter, url);
        String method = request.getParameter("method");
        if(url.endsWith("error.jsp")){
            doFilter = false;
        }
        if (StringUtils.isNotBlank(method) && "userlogin".equals(method)) {
            doFilter = false;
        }
        if (StringUtils.isNotBlank(method)) {
            for (String s : notFilterMethod) {
                if (s.equals(method)) {
                    doFilter = false;
                    break;
                }
            }
        }
        if (doFilter) {
            Object obj = request.getSession().getAttribute("globalUniqueUser");
            String globalUniqueID = request.getParameter("globalUniqueID");

            if (null == obj) {
                if (StringUtils.isNotBlank(globalUniqueID)) {
                    try {
                        UserEntity userSession = AAAAAdapter.getInstence().findUserBySessionID(globalUniqueID);
                        request.getSession().setAttribute("globalUniqueUser", userSession);
                        request.getSession().setAttribute("globalUniqueID", globalUniqueID);
                    } catch (PaasAAAAException e) {
                        e.printStackTrace();
                    }
                    filterChain.doFilter(request, response);
                } else {
//                    System.out.println(url);
//                    response.sendRedirect(request.getContextPath() + "/index.jsp");
                    response.sendRedirect(request.getContextPath().replace("UFP_MANAGE" , "uf").replace("UFP_TASK" , "uf"));
                }
            } else {
                if (StringUtils.isNotBlank(globalUniqueID) && !globalUniqueID.equals(request.getSession().getAttribute("globalUniqueID"))) {
                    try {
                        request.getSession().setAttribute("globalUniqueUser", AAAAAdapter.getInstence().findUserBySessionID(globalUniqueID));
                        request.getSession().setAttribute("globalUniqueID", globalUniqueID);
                    } catch (PaasAAAAException e) {
                        e.printStackTrace();
                    }
                }
                filterChain.doFilter(request, response);
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {

    }

    /**
     * @param notFilter 不拦截的url
     * @param url       ：请求的url
     * @return false：不拦截
     * true：拦截
     */
    public boolean check(String[] notFilter, String url) {
        //url以css和js结尾的不进行拦截
        if (url.endsWith(".css") || url.endsWith(".js") || url.endsWith(".png") || url.endsWith(".gif")|| url.endsWith(".eot")) {
            return false;
        }
        //含有notFilter中的任何一个则不进行拦截
        for (String s : notFilter) {
            if (url.contains(s)) {
                return false;
            }
        }
        return true;
    }
}
