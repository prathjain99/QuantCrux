package com.quantcrux.dto;

import com.quantcrux.model.FileFormat;
import com.quantcrux.model.ReportType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public class ReportRequest {
    
    private UUID portfolioId;
    private UUID strategyId;
    
    @NotNull
    private ReportType reportType;
    
    @NotBlank
    private String reportName;
    
    private String description;
    
    private LocalDate periodStart;
    private LocalDate periodEnd;
    
    @NotNull
    private FileFormat fileFormat;
    
    private String templateConfig;
    private String filters;
    
    // Report-specific options
    private String benchmarkSymbol = "SPY";
    private Boolean includeCharts = true;
    private Boolean includeCorrelations = false;
    private Boolean includeAttribution = false;
    private Boolean includeBenchmarkComparison = true;
    
    // Constructors
    public ReportRequest() {}
    
    public ReportRequest(ReportType reportType, String reportName, FileFormat fileFormat) {
        this.reportType = reportType;
        this.reportName = reportName;
        this.fileFormat = fileFormat;
    }
    
    // Getters and Setters
    public UUID getPortfolioId() { return portfolioId; }
    public void setPortfolioId(UUID portfolioId) { this.portfolioId = portfolioId; }
    
    public UUID getStrategyId() { return strategyId; }
    public void setStrategyId(UUID strategyId) { this.strategyId = strategyId; }
    
    public ReportType getReportType() { return reportType; }
    public void setReportType(ReportType reportType) { this.reportType = reportType; }
    
    public String getReportName() { return reportName; }
    public void setReportName(String reportName) { this.reportName = reportName; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public LocalDate getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }
    
    public LocalDate getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }
    
    public FileFormat getFileFormat() { return fileFormat; }
    public void setFileFormat(FileFormat fileFormat) { this.fileFormat = fileFormat; }
    
    public String getTemplateConfig() { return templateConfig; }
    public void setTemplateConfig(String templateConfig) { this.templateConfig = templateConfig; }
    
    public String getFilters() { return filters; }
    public void setFilters(String filters) { this.filters = filters; }
    
    public String getBenchmarkSymbol() { return benchmarkSymbol; }
    public void setBenchmarkSymbol(String benchmarkSymbol) { this.benchmarkSymbol = benchmarkSymbol; }
    
    public Boolean getIncludeCharts() { return includeCharts; }
    public void setIncludeCharts(Boolean includeCharts) { this.includeCharts = includeCharts; }
    
    public Boolean getIncludeCorrelations() { return includeCorrelations; }
    public void setIncludeCorrelations(Boolean includeCorrelations) { this.includeCorrelations = includeCorrelations; }
    
    public Boolean getIncludeAttribution() { return includeAttribution; }
    public void setIncludeAttribution(Boolean includeAttribution) { this.includeAttribution = includeAttribution; }
    
    public Boolean getIncludeBenchmarkComparison() { return includeBenchmarkComparison; }
    public void setIncludeBenchmarkComparison(Boolean includeBenchmarkComparison) { this.includeBenchmarkComparison = includeBenchmarkComparison; }
}