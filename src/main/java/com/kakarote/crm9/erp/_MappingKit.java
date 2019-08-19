package com.kakarote.crm9.erp;

import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.kakarote.crm9.erp.admin.entity.*;
import com.kakarote.crm9.erp.crm.entity.*;
import com.kakarote.crm9.erp.oa.entity.*;
import com.kakarote.crm9.erp.work.entity.*;

public class _MappingKit {

	public static void mapping(ActiveRecordPlugin arp) {
		arp.addMapping("72crm_admin_dept", "dept_id", AdminDept.class);
		arp.addMapping("72crm_admin_examine", "examine_id", AdminExamine.class);
		arp.addMapping("72crm_admin_config", "setting_id", AdminConfig.class);
		arp.addMapping("72crm_admin_examine_record", "record_id", AdminExamineRecord.class);
		arp.addMapping("72crm_admin_examine_step", "step_id", AdminExamineStep.class);
		arp.addMapping("72crm_admin_examine_log", "log_id", AdminExamineLog.class);
		arp.addMapping("72crm_admin_field", "field_id", AdminField.class);
		arp.addMapping("72crm_admin_field_sort", "field_id", AdminFieldSort.class);
		arp.addMapping("72crm_admin_field_style", "id", AdminFieldStyle.class);
		arp.addMapping("72crm_admin_fieldv", "id", AdminFieldv.class);
		arp.addMapping("72crm_admin_file", "file_id", AdminFile.class);
		arp.addMapping("72crm_admin_menu", "menu_id", AdminMenu.class);
		arp.addMapping("72crm_admin_record", "record_id", AdminRecord.class);
		arp.addMapping("72crm_admin_role", "role_id", AdminRole.class);
		arp.addMapping("72crm_admin_role_menu", "id", AdminRoleMenu.class);
		arp.addMapping("72crm_admin_scene", "scene_id", AdminScene.class);
		arp.addMapping("72crm_admin_scene_default", "default_id", AdminSceneDefault.class);
		arp.addMapping("72crm_admin_user", "user_id", AdminUser.class);
		arp.addMapping("72crm_admin_user_role", "id", AdminUserRole.class);
		arp.addMapping("72crm_crm_business", "business_id", CrmBusiness.class);
		arp.addMapping("72crm_crm_achievement", "achievement_id", CrmAchievement.class);
		arp.addMapping("72crm_crm_action_record", "id", CrmActionRecord.class);
		arp.addMapping("72crm_crm_business_product", "r_id", CrmBusinessProduct.class);
		arp.addMapping("72crm_crm_business_status", "status_id", CrmBusinessStatus.class);
		arp.addMapping("72crm_crm_business_type", "type_id", CrmBusinessType.class);
		arp.addMapping("72crm_crm_contacts", "contacts_id", CrmContacts.class);
		arp.addMapping("72crm_crm_contacts_business", "id", CrmContactsBusiness.class);
		arp.addMapping("72crm_crm_contract", "contract_id", CrmContract.class);
		arp.addMapping("72crm_crm_contract_product", "r_id", CrmContractProduct.class);
		arp.addMapping("72crm_crm_customer", "customer_id", CrmCustomer.class);
		arp.addMapping("72crm_crm_customer_stats", "id", CrmCustomerStats.class);
		arp.addMapping("72crm_crm_leads", "leads_id", CrmLeads.class);
		arp.addMapping("72crm_crm_owner_record", "record_id", CrmOwnerRecord.class);
		arp.addMapping("72crm_crm_product", "product_id", CrmProduct.class);
		arp.addMapping("72crm_crm_product_category", "category_id", CrmProductCategory.class);
		arp.addMapping("72crm_crm_receivables", "receivables_id", CrmReceivables.class);
		arp.addMapping("72crm_crm_receivables_plan", "plan_id", CrmReceivablesPlan.class);
		arp.addMapping("72crm_oa_action_record", "log_id", OaActionRecord.class);
		arp.addMapping("72crm_oa_announcement", "announcement_id", OaAnnouncement.class);
		arp.addMapping("72crm_oa_event", "event_id", OaEvent.class);
		arp.addMapping("72crm_oa_event_relation", "eventrelation_id", OaEventRelation.class);
		arp.addMapping("72crm_oa_examine_category", "category_id", OaExamineCategory.class);
		arp.addMapping("72crm_oa_examine_relation", "r_id", OaExamineRelation.class);
		arp.addMapping("72crm_oa_examine_travel", "travel_id", OaExamineTravel.class);
		arp.addMapping("72crm_oa_examine", "examine_id", OaExamine.class);
		arp.addMapping("72crm_oa_examine_log", "log_id", OaExamineLog.class);
		arp.addMapping("72crm_oa_examine_record", "record_id", OaExamineRecord.class);
		arp.addMapping("72crm_oa_examine_step", "step_id", OaExamineStep.class);
		arp.addMapping("72crm_oa_log", "log_id", OaLog.class);
		arp.addMapping("72crm_oa_log_relation", "r_id", OaLogRelation.class);
		arp.addMapping("72crm_task", "task_id", Task.class);
		arp.addMapping("72crm_task_comment", "comment_id", TaskComment.class);
		arp.addMapping("72crm_task_relation", "r_id", TaskRelation.class);
		arp.addMapping("72crm_work", "work_id", Work.class);
		arp.addMapping("72crm_work_task_class", "class_id", WorkTaskClass.class);
		arp.addMapping("72crm_work_task_label", "label_id", WorkTaskLabel.class);
		arp.addMapping("72crm_work_task_log", "log_id", WorkTaskLog.class);
		arp.addMapping("72crm_work_user", "id", WorkUser.class);
		arp.addMapping("72crm_crm_business_change", "change_id", CrmBusinessChange.class);
	}
}

