package com.kakarote.crm9;

import com.jfinal.server.undertow.UndertowConfig;
import com.jfinal.server.undertow.UndertowServer;
import com.kakarote.crm9.common.config.JfinalConfig;
import com.kakarote.crm9.common.constant.BaseConstant;

public class Application {
    public static void main(String[] args) {
        UndertowConfig config=new UndertowConfig(JfinalConfig.class,"config/undertow.txt");
        config.setResourcePath("src/main/webapp,"+ BaseConstant.UPLOAD_PATH);
        config.setServerName(BaseConstant.NAME);
        UndertowServer.create(config).start();
    }
}
