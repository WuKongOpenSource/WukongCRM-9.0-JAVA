package com.kakarote.crm9.erp.crm.common;

import com.jfinal.config.Routes;
import com.kakarote.crm9.erp.crm.controller.*;

public class CrmRouter extends Routes {
    @Override
    public void config() {
        addInterceptor(new CrmInterceptor());
        add("/CrmProduct", CrmProductController.class);
        add("/CrmProductCategory", CrmProductCategoryController.class);
        add("/CrmLeads", CrmLeadsController.class);
        add("/CrmCustomer", CrmCustomerController.class);
        add("/CrmBusiness", CrmBusinessController.class);
        add("/CrmContacts", CrmContactsController.class);
        add("/CrmContract", CrmContractController.class);
        add("/CrmReceivables", CrmReceivablesController.class);
        add("/CrmRecord",CrmRecordController.class);
        add("/Crm/ReceivablesPlan",CrmReceivablesPlanController.class);
        add("/Crm/Instrument",InstrumentController.class);
        add("/CrmBackLog",CrmBackLogController.class);
    }
}
