package com.kakarote.crm9;

import com.kakarote.crm9.common.config.JfinalConfig;
import com.kakarote.crm9.common.constant.BaseConstant;
import com.jfinal.server.undertow.UndertowServer;

public class Application {
    public static void main(String[] args) {
        /**
         * jetty启动
         * JFinal.start("src/main/webapp", 80, "/", 5);
         */
        UndertowServer
                .create(JfinalConfig.class,"config/undertow.txt")
                .setResourcePath("src/main/webapp,"+ BaseConstant.UPLOAD_PATH)
                .start();
    }
}
