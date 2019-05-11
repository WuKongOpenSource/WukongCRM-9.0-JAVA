package com.kakarote.crm9.common.config.render;

import com.jfinal.render.Render;
import com.jfinal.render.RenderException;
import com.jfinal.render.RenderManager;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 自定义异常渲染 根据前端请求的是HTML、Json来做不同的渲染；
 */
public class ErrorRender extends Render {

    private static final String HTML_CONTENT_TYPE = "text/html; charset=" + getEncoding();

    private static final String JSON_CONTENT_TYPE = "application/json; charset=" + getEncoding();

    private static String HTML_CONTENT = "<html><head><title>TITLE</title></head><body bgcolor='white'><center><h1>CONTENT</h1></center><hr>www.5kcrm.com</body></html>";

    private static final String HTML404 = HTML_CONTENT.replace("TITLE", "404 Not Found").replace("CONTENT", "404 Not Found");

    private static final String HTML500 = HTML_CONTENT.replace("TITLE", "500 Internal Server Error").replace("CONTENT", "500 Internal Server Error");

    private static final String HTML401 = HTML_CONTENT.replace("TITLE", "401 Unauthorized").replace("CONTENT", "401 Unauthorized");

    private static final String HTML403 = HTML_CONTENT.replace("TITLE", "403 Forbidden").replace("CONTENT", "403 Forbidden");

    private static final String JSON404 = "{\"code\": 404, \"msg\": \"404 请求失败\"}";
    private static final String JSON500 = "{\"code\": 500, \"msg\": \"500 操作失败\"}";
    private static final String JSON401 = "{\"code\": 401, \"msg\": \"401 未授权\"}";
    private static final String JSON403 = "{\"code\": 403, \"msg\": \"403 权限不足\"}";

    private int errorCode;

    protected ErrorRender(int errorCode, String view) {
        this.errorCode = errorCode;
        this.view = view;
    }

    @Override
    public void render() {
        String view = getView();
        if (view != null) {
            response.setStatus(getErrorCode());
            RenderManager.me().getRenderFactory().getRender(view).setContext(request, response).render();
            return;
        }
        PrintWriter writer;
        try {
            String wrBody;
            if (isAjax(request)) { // ajax请求，这里是json请求；如果是XML，自己定义！
                wrBody = getErrorJson();
                response.setStatus(getErrorCode()); // ajax请求响应必须是200才能正常接收
                response.setContentType(JSON_CONTENT_TYPE);
            } else {
                wrBody = getErrorHtml();
                response.setStatus(getErrorCode());
                response.setContentType(HTML_CONTENT_TYPE);
            }
            writer = response.getWriter();
            writer.write(wrBody);
            writer.flush();
        } catch (IOException e) {
            throw new RenderException(e);
        }
    }

    private String getErrorHtml() {
        int errorCode = getErrorCode();
        if (errorCode == 404) {
            return HTML404;
        } else if (errorCode == 500) {
            return HTML500;
        } else if (errorCode == 401) {
            return HTML401;
        } else if (errorCode == 403) {
            return HTML403;
        } else {
            return "<html><head><title>" + errorCode + " Error</title></head><body bgcolor='white'><center><h1>" + errorCode
                    + " Error</h1></center><hr>悟空软件</body></html>";
        }
    }

    private String getErrorJson() {
        int errorCode = getErrorCode();
        if (errorCode == 404) {
            return JSON404;
        } else if (errorCode == 500) {
            return JSON500;
        } else if (errorCode == 401) {
            return JSON401;
        } else if (errorCode == 403) {
            return JSON403;
        } else {
            return String.format("{code: %d, msg: '%d操作失败'}", errorCode, errorCode);
        }
    }

    private int getErrorCode() {
        return (errorCode != 0) ? errorCode : 100;
    }

    private boolean isAjax(HttpServletRequest request) {
        return request.getHeader("Admin-Token") != null;
    }
}
