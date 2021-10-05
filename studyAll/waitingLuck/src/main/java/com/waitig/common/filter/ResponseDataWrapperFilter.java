package com.waitig.common.filter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@WebFilter(filterName = "数据统一封装过滤器", urlPatterns = "/*")
public class ResponseDataWrapperFilter implements Filter {

    // 不走过滤器名单
    private static final Set<String> ALLOWED_PATHS = Collections.unmodifiableSet(new HashSet<>(
            Arrays.asList("/swagger-ui.html")));
    // 返回结果不进行JSON格式转化名单
    private static final Set<String> NOJSON_PATHS = Collections.unmodifiableSet(new HashSet<>(
            Arrays.asList("/swagger-ui.html")));

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
        httpServletResponse.addHeader("Access-Control-Allow-Origin", "*");
        if (httpServletRequest.getMethod().equalsIgnoreCase("OPTIONS")) {
            httpServletResponse.addHeader("Access-Control-Allow-Headers",
                    "X-Requested-With,Content-Type,Token,StaffId");
            httpServletResponse.addHeader("Access-Control-Allow-Methods",
                    "GET, POST, PUT, DELETE");
            httpServletResponse.addHeader("Access-Control-Max-Age", "6000");
            filterChain.doFilter(httpServletRequest, httpServletResponse);
            return;
        }
        if (ALLOWED_PATHS.contains(httpServletRequest.getRequestURI())) {
            filterChain.doFilter(httpServletRequest, httpServletResponse);
            return;
        } else {
            DataResponseWrapper responseWrapper = new DataResponseWrapper((HttpServletResponse) servletResponse);
            Map<String, Object> resultJo = new HashMap<>();
            try {
                filterChain.doFilter(servletRequest, responseWrapper);
                if (!("application/json;charset=UTF-8".equals(responseWrapper.getContentType()) ||
                        "text/plain;charset=UTF-8".equals(responseWrapper.getContentType()) ||
                        responseWrapper.getContentType() == null || "".equals(responseWrapper.getContentType()))) {
                    httpServletResponse.setContentType(responseWrapper.getContentType());
                    httpServletResponse.getOutputStream().write(responseWrapper.getDataStream());
                    return;
                }
                String responseContent = new String(responseWrapper.getDataStream(), "UTF-8");
                resultJo.put("code", 0);
                resultJo.put("msg", "");
                if (responseContent != null && !responseContent.trim().equals("")) {
                    if (responseContent.startsWith("[") && responseContent.endsWith("]") && !NOJSON_PATHS.contains(httpServletRequest.getRequestURI())) {
                        resultJo.put("data", JSONArray.fromObject(responseContent));
                    } else if (responseContent.startsWith("{") && responseContent.endsWith("}") && !NOJSON_PATHS.contains(httpServletRequest.getRequestURI())) {
                        resultJo.put("data", JSONObject.fromObject(responseContent));
                    } else {
                        resultJo.put("data", responseContent);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                resultJo.put("code", 1);
                resultJo.put("msg", e.getLocalizedMessage());
                resultJo.put("data", null);
            }

            PrintWriter out = null;
            try {
                servletResponse.setCharacterEncoding("UTF-8");
                servletResponse.setContentType("application/json; charset=utf-8");
                Gson gson = new GsonBuilder().serializeNulls()   //当字段值为空或null时，依然对该字段进行转换
                        .disableHtmlEscaping()  //防止特殊字符出现乱码
                        .create();
                String result = gson.toJson(resultJo);
                servletResponse.setContentLength(result.getBytes("UTF-8").length);
                out = servletResponse.getWriter();
                out.append(result);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (out != null) {
                    out.close();
                }
            }
        }


    }

    @Override
    public void destroy() {
    }

}