package com.code.cli.command;

import cn.hutool.core.bean.BeanUtil;
import com.code.generator.MainGenerator;
import com.code.model.MainTemplateConfig;
import lombok.Data;
import picocli.CommandLine;


import java.util.concurrent.Callable;

@CommandLine.Command(name = "generate", mixinStandardHelpOptions = true)
@Data
public class GenerateCommand implements Callable {

    /**
     * 作者（字符串，填充值）
     */
    @CommandLine.Option(names = {"-a", "--author"}, description = "作者名称", arity="0..1", interactive = true, echo = true)
    private String author = "coder";

    /**
     * 输出信息
     */
    @CommandLine.Option(names = {"-o", "--outputText"}, description = "输出结果", arity = "0..1", interactive = true, echo = true)
    private String outputText = "输出结果";

    /**
     * 是否循环（开关）
     */
    @CommandLine.Option(names = {"-l", "--loop"}, description = "是否循环", arity = "0..1", interactive = true, echo = true)
    private boolean loop;


    @Override
    public Object call() throws Exception {
        MainTemplateConfig mainTemplateConfig = new MainTemplateConfig();
        BeanUtil.copyProperties(this, mainTemplateConfig);
        MainGenerator.doGenerate(mainTemplateConfig);
        return 0;
    }
}
