package com.code.maker.template;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.code.maker.meta.Meta;
import com.code.maker.meta.enums.FileGenerateTypeEnum;
import com.code.maker.meta.enums.FileTypeEnum;
import com.code.maker.template.enums.FileFilterRangeEnum;
import com.code.maker.template.enums.FileFilterRuleEnum;
import com.code.maker.template.model.*;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 模板制作工具
 */
public class TemplateMaker {

    public static long makeTemplate(TemplateMakerConfig templateMakerConfig) {
        Long id = templateMakerConfig.getId();
        Meta meta = templateMakerConfig.getMeta();
        String originProjectPath = templateMakerConfig.getOriginProjectPath();
        TemplateMakerFileConfig templateMakerFileConfig = templateMakerConfig.getFileConfig();
        TemplateMakerModelConfig templateMakerModelConfig = templateMakerConfig.getModelConfig();
        TemplateMakerOutputConfig templateMakerOutputConfig = templateMakerConfig.getOutputConfig();

        return makeTemplate(meta, originProjectPath, templateMakerFileConfig, templateMakerModelConfig, templateMakerOutputConfig, id);
    }

    /**
     * 制作模板
     *
     * @param newMeta
     * @param originProjectPath
     * @param templateMakerFileConfig
     * @param templateMakerModelConfig
     * @param templateMakerOutputConfig
     * @param id
     * @return
     */
    public static long makeTemplate(Meta newMeta, String originProjectPath, TemplateMakerFileConfig templateMakerFileConfig, TemplateMakerModelConfig templateMakerModelConfig, TemplateMakerOutputConfig templateMakerOutputConfig, Long id) {
        // 没有 id 则生成
        if (id == null) {
            id = IdUtil.getSnowflakeNextId();
        }

        // 业务逻辑、、、

        // 复制目录
        String projectPath = System.getProperty("user.dir");
        String tempDirPath = projectPath + File.separator + ".temp";
        String templatePath = tempDirPath + File.separator + id;
        if (!FileUtil.exist(templatePath)) {
            FileUtil.mkdir(templatePath);
            FileUtil.copy(originProjectPath, templatePath, true);
        }

        // 一、输入信息
        // 要挖坑的项目根目录
        String sourceRootPath = FileUtil.loopFiles(new File(templatePath), 1, null)
                .stream()
                .filter(File::isDirectory)
                .findFirst()
                .orElseThrow(RuntimeException::new)
                .getAbsolutePath();
        // win系统需要对路径转义
        sourceRootPath = sourceRootPath.replaceAll("\\\\", "/");

        // 二、生成文件模板
        List<Meta.FileConfigDTO.FileInfo> newFileInfoList = makeFileTemplates(templateMakerFileConfig, templateMakerModelConfig, sourceRootPath);

        // 获取模型配置信息
        List<Meta.ModelConfigDTO.ModelInfo> newModelInfoList = getModelInfoList(templateMakerModelConfig);

        // 三、生成配置文件（对象 -> json文件）
        String metaOutputPath = templatePath + "/meta.json";

        // 已有 meta 文件，则不是第一次制作，则在原meta文件基础上修改
        if (FileUtil.exist(metaOutputPath)) {
            // meta.json -> Meta对象 -> meta.json
            newMeta = JSONUtil.toBean(FileUtil.readUtf8String(metaOutputPath), Meta.class);
            // 1. 追加配置参数
            List<Meta.FileConfigDTO.FileInfo> fileInfoList = newMeta.getFileConfig().getFiles();
            fileInfoList.addAll(newFileInfoList);

            List<Meta.ModelConfigDTO.ModelInfo> modelInfoList = newMeta.getModelConfig().getModels();
            modelInfoList.addAll(newModelInfoList);

            // 配置去重
            newMeta.getFileConfig().setFiles(distincFiles(fileInfoList));
            newMeta.getModelConfig().setModels(distincModels(modelInfoList));

        } else {

            // fileConfig配置
            Meta.FileConfigDTO fileConfig = new Meta.FileConfigDTO();
            fileConfig.setSourceRootPath(sourceRootPath);

            List<Meta.FileConfigDTO.FileInfo> fileInfoList = new ArrayList<>();
            fileConfig.setFiles(fileInfoList);

            fileInfoList.addAll(newFileInfoList);

            newMeta.setFileConfig(fileConfig);

            // modelConfig配置
            Meta.ModelConfigDTO modelConfig = new Meta.ModelConfigDTO();

            List<Meta.ModelConfigDTO.ModelInfo> modelInfoList = new ArrayList<>();
            modelConfig.setModels(modelInfoList);
            modelInfoList.addAll(newModelInfoList);

            newMeta.setModelConfig(modelConfig);
        }

        // 2. 额外的输出配置
        if (templateMakerOutputConfig != null) {
            // 外层文件 和 组内文件同名 外层的去重
            if (templateMakerOutputConfig.isRemoveGroupFilesFromRoot()) {
                List<Meta.FileConfigDTO.FileInfo> fileInfoList = newMeta.getFileConfig().getFiles();
                newMeta.getFileConfig().setFiles(TemplateMakerUtils.removeGroupFilesFormRoot(fileInfoList));
            }
        }

        // 3. 输出元信息文件
        FileUtil.writeUtf8String(JSONUtil.toJsonPrettyStr(newMeta), metaOutputPath);

        return id;
    }

    /**
     * 获取模型配置
     *
     * @param templateMakerModelConfig
     * @return
     */
    private static List<Meta.ModelConfigDTO.ModelInfo> getModelInfoList(TemplateMakerModelConfig templateMakerModelConfig) {
        // 本次新增的模型列表
        List<Meta.ModelConfigDTO.ModelInfo> newModelInfoList = new ArrayList<>();
        // 非空校验
        if (templateMakerModelConfig == null) {
            return newModelInfoList;
        }

        List<TemplateMakerModelConfig.ModelInfoConfig> models = templateMakerModelConfig.getModels();
        if (CollUtil.isEmpty(models)) {
            return newModelInfoList;
        }
        // 将配置类的变量，转为配置文件能接受的对象
        List<Meta.ModelConfigDTO.ModelInfo> inputModelInfoList = models.stream()
                .map(modelInfoConfig -> {
                    Meta.ModelConfigDTO.ModelInfo modelInfo = new Meta.ModelConfigDTO.ModelInfo();
                    BeanUtil.copyProperties(modelInfoConfig, modelInfo);
                    return modelInfo;
                }).collect(Collectors.toList());

        // 如果模型分组
        TemplateMakerModelConfig.ModelGroupConfig modelGroupConfig = templateMakerModelConfig.getModelGroupConfig();
        if (modelGroupConfig != null) {
            // 复制变量
            Meta.ModelConfigDTO.ModelInfo groupModelInfo = new Meta.ModelConfigDTO.ModelInfo();
            BeanUtil.copyProperties(modelGroupConfig, groupModelInfo);

            // 模型全部放到一个分组
            groupModelInfo.setModels(inputModelInfoList);
            newModelInfoList.add(groupModelInfo);
        } else {
            // 不分组，添加所有的模型信息列表
            newModelInfoList.addAll(inputModelInfoList);
        }
        return newModelInfoList;
    }

    /**
     * 生成多个文件
     *
     * @param templateMakerFileConfig
     * @param templateMakerModelConfig
     * @param sourceRootPath
     * @return
     */
    private static List<Meta.FileConfigDTO.FileInfo> makeFileTemplates(TemplateMakerFileConfig templateMakerFileConfig, TemplateMakerModelConfig templateMakerModelConfig, String sourceRootPath) {
        List<Meta.FileConfigDTO.FileInfo> newFileInfoList = new ArrayList<>();

        // 非空校验
        if (templateMakerFileConfig == null) {
            return newFileInfoList;
        }

        List<TemplateMakerFileConfig.FileInfoConfig> fileInfoConfigList = templateMakerFileConfig.getFiles();
        if (CollUtil.isEmpty(fileInfoConfigList)) {
            return newFileInfoList;
        }

        // 遍历输入文件
        for (TemplateMakerFileConfig.FileInfoConfig fileInfoConfig : fileInfoConfigList) {
            String inputFilePath = fileInfoConfig.getPath();
            String inputFileAbsolutePath = sourceRootPath + File.separator + inputFilePath;
            // 传入绝对路径
            // 得到过滤后的文件列表
            List<File> fileList = FileFilter.doFilter(inputFileAbsolutePath, fileInfoConfig.getFilterConfigList());
            // 不处理已经生成的 FTL 模板
            fileList = fileList.stream()
                    .filter(file -> !file.getAbsolutePath().endsWith(".ftl"))
                    .collect(Collectors.toList());
            // 得到要输出文件
            for (File file : fileList) {
                Meta.FileConfigDTO.FileInfo fileInfo = makeFileTemplate(templateMakerModelConfig, sourceRootPath, file, fileInfoConfig);
                newFileInfoList.add(fileInfo);
            }
        }

        // 如果文件分组
        TemplateMakerFileConfig.FileGroupConfig fileGroupConfig = templateMakerFileConfig.getFileGroupConfig();
        if (fileGroupConfig != null) {
            String condition = fileGroupConfig.getCondition();
            String groupKey = fileGroupConfig.getGroupKey();
            String groupName = fileGroupConfig.getGroupName();

            // 新增分组配置
            Meta.FileConfigDTO.FileInfo groupFileInfo = new Meta.FileConfigDTO.FileInfo();
            groupFileInfo.setType(FileTypeEnum.GROUP.getValue());
            groupFileInfo.setCondition(condition);
            groupFileInfo.setGroupKey(groupKey);
            groupFileInfo.setGroupName(groupName);
            // 文件全部放到一个分组
            groupFileInfo.setFiles(newFileInfoList);
            newFileInfoList = new ArrayList<>();
            newFileInfoList.add(groupFileInfo);
        }
        return newFileInfoList;
    }

    private static Meta.FileConfigDTO.FileInfo makeFileTemplate(TemplateMakerModelConfig templateMakerModelConfig, String sourceRootPath,
                                                                File inputFile, TemplateMakerFileConfig.FileInfoConfig fileInfoConfig) {
        String fileInputAbsolutePath = inputFile.getAbsolutePath();
        // 注意win系统，要对路径转义
        fileInputAbsolutePath = fileInputAbsolutePath.replaceAll("\\\\", "/");

        // 要挖坑的文件(一定要相对路径)
        String fileInputPath = fileInputAbsolutePath.replace(sourceRootPath + "/", "");
        String fileOutputPath = fileInputPath + ".ftl";

        // 二、使用字符串替换，生成模板文件
        String fileOutputAbsolutePath = fileInputAbsolutePath + ".ftl";

        String fileContent;
        // 如果已有模板文件，表示已经不是第一次制作，则在原有的基础上挖坑
        boolean hasTemplateFile = FileUtil.exist(fileOutputAbsolutePath);
        if (hasTemplateFile) {
            fileContent = FileUtil.readUtf8String(fileOutputAbsolutePath);
        } else {
            fileContent = FileUtil.readUtf8String(fileInputAbsolutePath);
        }

        // 支持多个模型：对于同一文件内容，遍历模型，进行多轮替换
        TemplateMakerModelConfig.ModelGroupConfig modelGroupConfig = templateMakerModelConfig.getModelGroupConfig();
        // 最新替换后的内容
        String newFileContent = fileContent;
        String replacement;
        for (TemplateMakerModelConfig.ModelInfoConfig modelInfoConfig : templateMakerModelConfig.getModels()) {
            String fieldName = modelInfoConfig.getFieldName();
            // 不分组
            if (modelGroupConfig == null) {
                replacement = String.format("${%s}", fieldName);
            } else {
                // 分组
                String groupKey = modelGroupConfig.getGroupKey();
                replacement = String.format("${%s.%s}", groupKey, fieldName);
            }
            // 多次替换
            newFileContent = StrUtil.replace(newFileContent, modelInfoConfig.getReplaceText(), replacement);
        }

        // 文件配置信息
        Meta.FileConfigDTO.FileInfo fileInfo = new Meta.FileConfigDTO.FileInfo();
        fileInfo.setInputPath(fileOutputPath);
        fileInfo.setOutputPath(fileInputPath);
        fileInfo.setCondition(fileInfoConfig.getCondition());
        fileInfo.setType(FileTypeEnum.FILE.getValue());
        fileInfo.setGenerateType(FileGenerateTypeEnum.DYNAMIC.getValue());

        boolean contentEquals = newFileContent.equals(fileContent);
        // 之前不存在模板文件，且这次替换没有修改文件的内容，才是静态生成
        if (!hasTemplateFile) {
            if (contentEquals) {
                fileInfo.setInputPath(fileInputPath);
                fileInfo.setGenerateType(FileGenerateTypeEnum.STATIC.getValue());
            } else {
                // 输出模板文件(动态生成，才需要生成模板文件)
                FileUtil.writeUtf8String(newFileContent, fileOutputAbsolutePath);
            }
        } else if (!contentEquals) {
            // 已经有模板文件，且增加了新坑
            FileUtil.writeUtf8String(newFileContent, fileOutputAbsolutePath);
        }
        return fileInfo;
    }

    public static void main(String[] args) {
        // 1. 基本信息
        String name = "acm-template-generator";
        String description = "ACM 示例模板生成器";

        // 构造配置参数的对象
        Meta meta = new Meta();
        meta.setName(name);
        meta.setDescription(description);

        // 指定原始项目目录
        String projectPath = System.getProperty("user.dir");
        String originProjectPath = new File(projectPath).getParent() + File.separator + "springboot-init-master";

        // 要挖坑的文件
        String fileInputPath1 = "src/main/java/com/yupi/springbootinit/common";
        String fileInputPath2 = "src/main/resources/application.yml";
        List<String> inputFilePathList = Arrays.asList(fileInputPath1, fileInputPath2);

        // 输入的模型参数信息
        // 输入的模型参数信息（第二次）
        Meta.ModelConfigDTO.ModelInfo modelInfo = new Meta.ModelConfigDTO.ModelInfo();
        modelInfo.setFieldName("className");
        modelInfo.setType("String");

        // 替换变量
//        String searchStr = "Sum: ";
        String  searchStr = "BaseResponse";

        // 文件过滤配置
        TemplateMakerFileConfig.FileInfoConfig fileInfoConfig1 = new TemplateMakerFileConfig.FileInfoConfig();
        fileInfoConfig1.setPath(fileInputPath1);
        List<FileFilterConfig> fileFilterConfigList = new ArrayList<>();
        FileFilterConfig fileFilterConfig = FileFilterConfig.builder()
                .range(FileFilterRangeEnum.FILE_NAME.getValue())
                .rule(FileFilterRuleEnum.CONTAINS.getValue())
                .value("Base")
                .build();
        fileFilterConfigList.add(fileFilterConfig);
        fileInfoConfig1.setFilterConfigList(fileFilterConfigList);

        TemplateMakerFileConfig.FileInfoConfig fileInfoConfig2 = new TemplateMakerFileConfig.FileInfoConfig();
        fileInfoConfig2.setPath(fileInputPath2);

        List<TemplateMakerFileConfig.FileInfoConfig> fileInfoConfigList = Arrays.asList(fileInfoConfig1, fileInfoConfig2);

        TemplateMakerFileConfig templateMakerFileConfig = new TemplateMakerFileConfig();
        templateMakerFileConfig.setFiles(fileInfoConfigList);

        // 分组配置
        TemplateMakerFileConfig.FileGroupConfig fileGroupConfig = new TemplateMakerFileConfig.FileGroupConfig();
        fileGroupConfig.setCondition("outputText");
        fileGroupConfig.setGroupKey("testa");
        fileGroupConfig.setGroupName("测试分组");
        templateMakerFileConfig.setFileGroupConfig(fileGroupConfig);

        // 模型参数配置
        TemplateMakerModelConfig templateMakerModelConfig = new TemplateMakerModelConfig();

// - 模型组配置
        TemplateMakerModelConfig.ModelGroupConfig modelGroupConfig = new TemplateMakerModelConfig.ModelGroupConfig();
        modelGroupConfig.setGroupKey("mysql");
        modelGroupConfig.setGroupName("数据库配置");
        templateMakerModelConfig.setModelGroupConfig(modelGroupConfig);

// - 模型配置
        TemplateMakerModelConfig.ModelInfoConfig modelInfoConfig1 = new TemplateMakerModelConfig.ModelInfoConfig();
        modelInfoConfig1.setFieldName("url");
        modelInfoConfig1.setType("String");
        modelInfoConfig1.setDefaultValue("jdbc:mysql://localhost:3306/my_db");
        modelInfoConfig1.setReplaceText("jdbc:mysql://localhost:3306/my_db");

        TemplateMakerModelConfig.ModelInfoConfig modelInfoConfig2 = new TemplateMakerModelConfig.ModelInfoConfig();
        modelInfoConfig2.setFieldName("username");
        modelInfoConfig2.setType("String");
        modelInfoConfig2.setDefaultValue("root");
        modelInfoConfig2.setReplaceText("root");

        List<TemplateMakerModelConfig.ModelInfoConfig> modelInfoConfigList = Arrays.asList(modelInfoConfig1, modelInfoConfig2);
        templateMakerModelConfig.setModels(modelInfoConfigList);


        long id = TemplateMaker.makeTemplate(meta, originProjectPath, templateMakerFileConfig, templateMakerModelConfig, null, 1834374419939172352L);
        System.out.println(id);
    }

    /**
     * 文件去重
     * 
     * @param fileInfoList
     * @return
     */
    private static List<Meta.FileConfigDTO.FileInfo> distincFiles(List<Meta.FileConfigDTO.FileInfo> fileInfoList) {
        // 1. 将所有文件配置fileInfo 分为有分组和无分组 （分治）

        // 筛选出有分组的fileInfo
        Map<String, List<Meta.FileConfigDTO.FileInfo>> groupKeyFileInfoListMap = fileInfoList.stream()
                .filter(fileInfo -> StrUtil.isNotBlank(fileInfo.getGroupKey()))
                .collect(
                        Collectors.groupingBy(Meta.FileConfigDTO.FileInfo::getGroupKey)
                );

        // 2. 对于有分组的文件配置，如果有相同分组，同分组的文件进行合并，不同分组的可同时保留

        // 保存每个组合并后的对象map
        Map<String, Meta.FileConfigDTO.FileInfo> groupKeyMergedFileInfoMap = new HashMap<>();
        // 同组合并
        for (Map.Entry<String, List<Meta.FileConfigDTO.FileInfo>> entry : groupKeyFileInfoListMap.entrySet()) {
            List<Meta.FileConfigDTO.FileInfo> tempFileInfoList = entry.getValue();

            List<Meta.FileConfigDTO.FileInfo> newFileInfoList = new ArrayList<>(tempFileInfoList.stream()
                    .flatMap(fileInfo -> fileInfo.getFiles().stream())
                    .collect(
                            Collectors.toMap(Meta.FileConfigDTO.FileInfo::getOutputPath, o -> o, (e, r) -> r)
                    ).values());

            // 使用新的 group 配置（覆盖）
            Meta.FileConfigDTO.FileInfo newFileInfo = CollUtil.getLast(tempFileInfoList);
            newFileInfo.setFiles(newFileInfoList);
            // 合并后配置的map
            String groupKey = entry.getKey();
            groupKeyMergedFileInfoMap.put(groupKey, newFileInfo);
        }
        // 3. 创建新的文件配置列表（结果列表），将合并后的分组添加到结果列表
        ArrayList<Meta.FileConfigDTO.FileInfo> resultList = new ArrayList<>(groupKeyMergedFileInfoMap.values());
        // 4. 再将无分组的文件配置列表添加到结果列表
        resultList.addAll(new ArrayList<>(fileInfoList.stream()
                .filter(fileInfo -> StrUtil.isBlank(fileInfo.getGroupKey()))
                .collect(
                        Collectors.toMap(Meta.FileConfigDTO.FileInfo::getOutputPath, o -> o, (e, r) -> r)
                ).values()));
        return resultList;
    }
    
    /**
     * 模型去重
     * 
     * @param modelInfoList
     * @return
     */
    private static List<Meta.ModelConfigDTO.ModelInfo> distincModels(List<Meta.ModelConfigDTO.ModelInfo> modelInfoList) {
        // 1. 将所有模型配置modelInfo 分为有分组和无分组 （分治）

        // 筛选出有分组的modelInfo
        Map<String, List<Meta.ModelConfigDTO.ModelInfo>> groupKeyModelInfoListMap = modelInfoList.stream()
                .filter(modelInfo -> StrUtil.isNotBlank(modelInfo.getGroupKey()))
                .collect(
                        Collectors.groupingBy(Meta.ModelConfigDTO.ModelInfo::getGroupKey)
                );

        // 2. 对于有分组的模型配置，如果有相同分组，同分组的模型进行合并，不同分组的可同时保留

        // 保存每个组合并后的对象map
        Map<String, Meta.ModelConfigDTO.ModelInfo> groupKeyMergedModelInfoMap = new HashMap<>();
        // 同组合并
        for (Map.Entry<String, List<Meta.ModelConfigDTO.ModelInfo>> entry : groupKeyModelInfoListMap.entrySet()) {
            List<Meta.ModelConfigDTO.ModelInfo> tempModelInfoList = entry.getValue();

            List<Meta.ModelConfigDTO.ModelInfo> newModelInfoList = new ArrayList<>(tempModelInfoList.stream()
                    .flatMap(modelInfo -> modelInfo.getModels().stream())
                    .collect(
                            Collectors.toMap(Meta.ModelConfigDTO.ModelInfo::getFieldName, o -> o, (e, r) -> r)
                    ).values());

            // 使用新的 group 配置（覆盖）
            Meta.ModelConfigDTO.ModelInfo newModelInfo = CollUtil.getLast(tempModelInfoList);
            newModelInfo.setModels(newModelInfoList);
            // 合并后配置的map
            String groupKey = entry.getKey();
            groupKeyMergedModelInfoMap.put(groupKey, newModelInfo);
        }
        // 3. 创建新的模型配置列表（结果列表），将合并后的分组添加到结果列表
        ArrayList<Meta.ModelConfigDTO.ModelInfo> resultList = new ArrayList<>(groupKeyMergedModelInfoMap.values());
        // 4. 再将无分组的模型配置列表添加到结果列表
        resultList.addAll(new ArrayList<>(modelInfoList.stream()
                .filter(modelInfo -> StrUtil.isBlank(modelInfo.getGroupKey()))
                .collect(
                        Collectors.toMap(Meta.ModelConfigDTO.ModelInfo::getFieldName, o -> o, (e, r) -> r)
                ).values()));
        return resultList;
    }
}
