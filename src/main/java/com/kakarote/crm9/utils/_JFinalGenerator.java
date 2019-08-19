package com.kakarote.crm9.utils;

import com.jfinal.kit.PathKit;
import com.jfinal.plugin.activerecord.dialect.MysqlDialect;
import com.jfinal.plugin.activerecord.generator.Generator;
import com.jfinal.plugin.druid.DruidPlugin;
import com.kakarote.crm9.common.config.JfinalConfig;

import javax.sql.DataSource;

/**
 * @author wyq
 */
public class _JFinalGenerator {
    public static DataSource getDataSource() {
        DruidPlugin druidPlugin = JfinalConfig.createDruidPlugin();
        druidPlugin.start();
        return druidPlugin.getDataSource();
    }

    public static void main(String[] args) {
        // base model 所使用的包名
        String baseModelPackageName = "com.erpsnow.erp";
        // base model 文件保存路径
        String baseModelOutputDir = PathKit.getWebRootPath() + "/src/main/test/erp/base";

        // model 所使用的包名 (MappingKit 默认使用的包名)
        String modelPackageName = "com.kakarote.crm9.test";
        // model 文件保存路径 (MappingKit 与 DataDictionary 文件默认保存路径)
        String modelOutputDir = baseModelOutputDir + "/..";

        // 创建生成器
        Generator generator = new Generator(getDataSource(), baseModelPackageName, baseModelOutputDir, modelPackageName, modelOutputDir);
        // 设置数据库方言
        generator.setDialect(new MysqlDialect());
        // 设置是否生成链式 setter 方法
        generator.setGenerateChainSetter(false);

        // 设置是否在 Model 中生成 dao 对象
        generator.setGenerateDaoInModel(true);
        // 设置是否生成字典文件
        generator.setGenerateDataDictionary(false);
        // 设置需要被移除的表名前缀用于生成modelName。例如表名 "osc_user"，移除前缀 "osc_"后生成的model名为 "User"而非 OscUser
        generator.setRemovedTableNamePrefixes("72crm_");
        // 生成
        generator.generate();
    }
}
