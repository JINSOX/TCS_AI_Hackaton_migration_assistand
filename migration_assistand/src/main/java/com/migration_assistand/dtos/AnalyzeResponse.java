package com.migration_assistand.dtos;

public class AnalyzeResponse {
    private String projectPath;
    private boolean hasPatch;
    private String patchPath;
    private String patchContent;
    private int rewriteExitCode;
    private String rewriteLog;

    // getters / setters
    public String getProjectPath() { return projectPath; }
    public void setProjectPath(String projectPath) { this.projectPath = projectPath; }
    public boolean isHasPatch() { return hasPatch; }
    public void setHasPatch(boolean hasPatch) { this.hasPatch = hasPatch; }
    public String getPatchPath() { return patchPath; }
    public void setPatchPath(String patchPath) { this.patchPath = patchPath; }
    public String getPatchContent() { return patchContent; }
    public void setPatchContent(String patchContent) { this.patchContent = patchContent; }
    public int getRewriteExitCode() { return rewriteExitCode; }
    public void setRewriteExitCode(int rewriteExitCode) { this.rewriteExitCode = rewriteExitCode; }
    public String getRewriteLog() { return rewriteLog; }
    public void setRewriteLog(String rewriteLog) { this.rewriteLog = rewriteLog; }
}
