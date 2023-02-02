package cn.itcast.mp.generator;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.InjectionConfig;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.builder.ConfigBuilder;
import com.baomidou.mybatisplus.generator.config.converts.MySqlTypeConvert;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.config.rules.DbColumnType;
import com.baomidou.mybatisplus.generator.config.rules.FileType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * MyBatis-Plus 代码生成类
 */
public class CodeGeneratorArticle {

	private static final String PACKAGE_NAME = "com.heima";
	private static final String MODEL_NAME = "article";

	private static final String DB_URL  = "jdbc:mysql://192.168.200.130:3306/leadnews_article?useUnicode=true&useSSL=false&characterEncoding=utf8";
	private static final String DATA_SOURCE_USER_NAME  = "root";
	private static final String DATA_SOURCE_PASSWORD  = "root";


	private static final String[] TABLE_NAMES = new String[]{
			"ap_article","ap_article_config","ap_article_content"
	};

	private static final Boolean swagger2 = false;

	//工程路径
	//独立工程
	// private static final String projectPath = System.getProperty("user.dir");
	//嵌套工程
	private static final String projectPath = System.getProperty("user.dir")+
			File.separator+"heima-leadnews-service"+File.separator+"heima-leadnews-article";


	public static void main(String[] args) {
		// 代码生成器
		AutoGenerator mpg = new MyAutoGenerator();
		// 选择 freemarker 引擎
		mpg.setTemplateEngine(new FreemarkerTemplateEngine());
		// 全局配置
		GlobalConfig gc = new GlobalConfig();
		gc.setFileOverride(true);
		gc.setOutputDir(projectPath + "/src/main/java");
		gc.setAuthor("itheima");
		gc.setOpen(false);
		gc.setSwagger2(false);
		gc.setServiceName("%sService");
        gc.setBaseResultMap(true);
        gc.setBaseColumnList(true);
		gc.setSwagger2(swagger2);

		mpg.setGlobalConfig(gc);

		// 数据库配置
		DataSourceConfig dsc = new DataSourceConfig();
		dsc.setDbType(DbType.MYSQL);
		dsc.setUrl(DB_URL);
		dsc.setDriverName("com.mysql.jdbc.Driver");
		dsc.setUsername(DATA_SOURCE_USER_NAME);
		dsc.setPassword(DATA_SOURCE_PASSWORD);

		dsc.setTypeConvert(new MySqlTypeConvert(){
			@Override
			public DbColumnType processTypeConvert(GlobalConfig globalConfig, String fieldType) {
				System.out.println("转换类型：" + fieldType);
				//tinyint转换成Boolean
				if ( fieldType.toLowerCase().contains("tinyint") ) {
					return DbColumnType.INTEGER;
				}
				//将数据库中datetime转换成date
				if ( fieldType.toLowerCase().contains("datetime") ) {
					return DbColumnType.DATE;
				}
				return (DbColumnType) super.processTypeConvert(globalConfig, fieldType);
			}
		});

		mpg.setDataSource(dsc);

		// 包配置
		PackageConfig pc = new PackageConfig();
		pc.setModuleName(MODEL_NAME);
		pc.setParent(PACKAGE_NAME);

		pc.setServiceImpl("service.impl");
		pc.setXml("mapper");
		pc.setEntity("pojos");

		mpg.setPackageInfo(pc);


        // 自定义配置
        InjectionConfig cfg = new InjectionConfig() {
            @Override
            public void initMap() {
              //do nothings
            }
        };
        //修改mapper.xml路径
        List<FileOutConfig> focList = new ArrayList<>();
        focList.add(new FileOutConfig("/templates/mapper.xml.ftl") {
            @Override
            public String outputFile(TableInfo tableInfo) {
                // 自定义输入文件名称
                return projectPath + "/src/main/resources/mapper/" +
                        tableInfo.getEntityName() + "Mapper" + StringPool.DOT_XML;
            }
        });

        //原来路径不生成xml文件
		cfg.setFileCreate(new IFileCreate() {
			@Override
			public boolean isCreate(ConfigBuilder configBuilder, FileType fileType, String filePath) {
				// 判断自定义文件夹是否需要创建
				if(FileType.XML == fileType){
					return false;
				}
				checkDir(filePath);
				return true;
			}
		});
        cfg.setFileOutConfigList(focList);
        mpg.setCfg(cfg);

		// 设置模板
		TemplateConfig tc = new TemplateConfig();
		mpg.setTemplate(tc);

		// 策略配置
		StrategyConfig strategy = new StrategyConfig();
		strategy.setNaming(NamingStrategy.underline_to_camel);
		strategy.setColumnNaming(NamingStrategy.underline_to_camel);
		strategy.setEntityLombokModel(true);
		strategy.setRestControllerStyle(true);
		strategy.setInclude(TABLE_NAMES);
		strategy.setControllerMappingHyphenStyle(true);
		strategy.setTablePrefix(pc.getModuleName() + "_");
		// Boolean类型字段是否移除is前缀处理
		strategy.setEntityBooleanColumnRemoveIsPrefix(false);
		strategy.setRestControllerStyle(true);

		mpg.setStrategy(strategy);


		mpg.execute();
	}

}
