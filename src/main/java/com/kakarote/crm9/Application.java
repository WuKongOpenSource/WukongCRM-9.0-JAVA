package com.kakarote.crm9;

import com.jfinal.server.undertow.UndertowServer;
import com.kakarote.crm9.common.config.JfinalConfig;
import com.kakarote.crm9.common.constant.BaseConstant;

public class Application {
    public static void main(String[] args) {
        UndertowServer
                .create(JfinalConfig.class,"config/undertow.txt")
                .setResourcePath("src/main/webapp,"+ BaseConstant.UPLOAD_PATH)
                .start();
    }
}
