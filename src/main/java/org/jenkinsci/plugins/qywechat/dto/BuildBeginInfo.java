package org.jenkinsci.plugins.qywechat.dto;

import hudson.EnvVars;
import org.jenkinsci.plugins.qywechat.NotificationUtil;
import org.jenkinsci.plugins.qywechat.model.NotificationConfig;
import hudson.model.AbstractBuild;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 开始构建的通知信息
 * @author jiaju
 */
public class BuildBeginInfo {

    /**
     * 请求参数
     */
    private Map params = new HashMap<String, Object>();

    /**
     * 预计时间，毫秒
     */
    private Long durationTime = 0L;

    /**
     * 本次构建控制台地址
     */
    private String consoleUrl;

    /**
     * 工程名称
     */
    private String projectName;

    /**
     * 环境名称
     */
    private String topicName = "";

    /**
     * 构建分支
     */
    private String branch;

    public BuildBeginInfo(String projectName, AbstractBuild<?, ?> build, EnvVars envVars, NotificationConfig config){
        // 构建分支
        this.branch = envVars.get("gitlabBranch");
        //预计时间
        if(build.getProject().getEstimatedDuration()>0){
            this.durationTime = build.getProject().getEstimatedDuration();
        }
        //控制台地址
        StringBuilder urlBuilder = new StringBuilder();
        String jenkinsUrl = NotificationUtil.getJenkinsUrl();
        if(StringUtils.isNotEmpty(jenkinsUrl)){
            String buildUrl = build.getUrl();
            urlBuilder.append(jenkinsUrl);
            if(!jenkinsUrl.endsWith("/")){
                urlBuilder.append("/");
            }
            urlBuilder.append(buildUrl);
            if(!buildUrl.endsWith("/")){
                urlBuilder.append("/");
            }
            urlBuilder.append("console");
        }
        this.consoleUrl = urlBuilder.toString();
        //工程名称
        this.projectName = projectName;
        //环境名称
        if(config.topicName!=null){
            topicName = config.topicName;
        }
    }

    public String toJSONString(){
        //耗时预计
        String durationTimeStr = "无";
        if(durationTime>0){
            Long l = durationTime / (1000 * 60);
            durationTimeStr = l + "分钟";
        }

        //组装内容
        StringBuilder content = new StringBuilder();
        if(StringUtils.isNotEmpty(topicName)){
            content.append(this.topicName);
        }
        content.append("<font color=\"info\">【" + this.projectName + "】</font>开始构建\n");
        content.append(" >构建分支：<font color=\"comment\">" + this.branch + "</font>\n");
        content.append(" >预计用时：<font color=\"comment\">" +  durationTimeStr + "</font>\n");
        if(StringUtils.isNotEmpty(this.consoleUrl)){
            content.append(" >[查看控制台](" + this.consoleUrl + ")");
        }

        Map markdown = new HashMap<String, Object>();
        markdown.put("content", content.toString());

        Map data = new HashMap<String, Object>();
        data.put("msgtype", "markdown");
        data.put("markdown", markdown);

        String req = JSONObject.fromObject(data).toString();
        return req;
    }



}
