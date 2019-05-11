package com.kakarote.crm9.common.config.render;

import com.jfinal.render.Render;
import com.jfinal.render.RenderFactory;

public class ErpRenderFactory extends RenderFactory {
    @Override
    public Render getErrorRender(int errorCode, String view) {
        return new ErrorRender(errorCode, view);
    }

    @Override
    public Render getErrorRender(int errorCode) {
        return new ErrorRender(errorCode, constants.getErrorView(errorCode));
    }
}
