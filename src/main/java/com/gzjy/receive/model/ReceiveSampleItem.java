package com.gzjy.receive.model;

import java.io.Serializable;
import java.util.Date;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.format.annotation.DateTimeFormat;

import com.gzjy.common.Add;
import com.gzjy.common.Update;

public class ReceiveSampleItem implements Serializable{
    /**
     * 
     */
    private static final long serialVersionUID = 1549980287356846170L;
    private ReceiveSample sample;
    //接样检验项目的id
    @NotEmpty(message="检验项目的id不能为空", groups={Update.class})
    private String id;
    //抽样编号
    @NotNull(message="抽样编号不能为空", groups={Add.class})
    private String receiveSampleId;
  //抽样编号
    @NotNull(message="报告编号不能为空", groups={Add.class})
    private String reportId;
    //项目名称
    @NotNull(message="项目名称不能为空", groups={Add.class})
    private String name;
    //检测方法
    @NotNull(message="检测方法", groups={Add.class})
    private String method;
    //单位
    private String unit;
    //标准值
    private String standardValue;
    //检出限
    private String detectionLimit;
    //定量限
    private String quantitationLimit;
    //设备
    private String device;
    //价格
    private Double defaultPrice;
    //检验室
    private String testRoom;
    //分包
    private String subpackage;
    //结果
    private String itemResult;
   //实测值
    private String measuredValue;
    //检验员
    private String testUser;
    //检验项状态(0:待分配，1：带检测，2：检测完成，5：查询所有的状态结果)
    private Integer status;
    //更新时间
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updatedAt;
    

    public ReceiveSample getSample() {
        return sample;
    }

    public void setSample(ReceiveSample sample) {
        this.sample = sample;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getReceiveSampleId() {
        return receiveSampleId;
    }

    public void setReceiveSampleId(String receiveSampleId) {
        this.receiveSampleId = receiveSampleId == null ? null : receiveSampleId.trim();
    }
    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId == null ? null : reportId.trim();
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method == null ? null : method.trim();
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit == null ? null : unit.trim();
    }

    public String getStandardValue() {
        return standardValue;
    }

    public void setStandardValue(String standardValue) {
        this.standardValue = standardValue == null ? null : standardValue.trim();
    }

    public String getDetectionLimit() {
        return detectionLimit;
    }

    public void setDetectionLimit(String detectionLimit) {
        this.detectionLimit = detectionLimit == null ? null : detectionLimit.trim();
    }

    public String getQuantitationLimit() {
        return quantitationLimit;
    }

    public void setQuantitationLimit(String quantitationLimit) {
        this.quantitationLimit = quantitationLimit == null ? null : quantitationLimit.trim();
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device == null ? null : device.trim();
    }

    public Double getDefaultPrice() {
        return defaultPrice;
    }

    public void setDefaultPrice(Double defaultPrice) {
        this.defaultPrice = defaultPrice;
    }

    public String getTestRoom() {
        return testRoom;
    }

    public void setTestRoom(String testRoom) {
        this.testRoom = testRoom == null ? null : testRoom.trim();
    }

    public String getSubpackage() {
        return subpackage;
    }

    public void setSubpackage(String subpackage) {
        this.subpackage = subpackage == null ? null : subpackage.trim();
    }

    public String getItemResult() {
        return itemResult;
    }

    public void setItemResult(String itemResult) {
        this.itemResult = itemResult == null ? null : itemResult.trim();
    }

    public String getMeasuredValue() {
        return measuredValue;
    }

    public void setMeasuredValue(String measuredValue) {
        this.measuredValue = measuredValue == null ? null : measuredValue.trim();
    }

    public String getTestUser() {
        return testUser;
    }

    public void setTestUser(String testUser) {
        this.testUser = testUser == null ? null : testUser.trim();
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
    
}