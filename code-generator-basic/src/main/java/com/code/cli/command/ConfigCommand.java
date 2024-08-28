package com.code.cli.command;

import cn.hutool.core.util.ReflectUtil;
import com.code.model.MainTemplateConfig;
import picocli.CommandLine;

import java.lang.reflect.Field;

@CommandLine.Command(name = "config", mixinStandardHelpOptions = true)
public class ConfigCommand implements Runnable{
    /**
     * 反射：程序动态运行时，获取某些类的信息（类没执行时，是无法知道它的信息的（执行的方法等等），所有需要动态获取）
     * 甚至可以动态创建新的类，执行新的类的方法
     */
    @Override
    public void run() {
        Field[] fields = ReflectUtil.getFields(MainTemplateConfig.class);
        for (Field field : fields) {
            System.out.println("字段类型：" + field.getType());
            System.out.println("字段名称：" + field.getName());
        }
    }
}
