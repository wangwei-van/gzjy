package com.gzjy.receive.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import com.github.pagehelper.PageInfo;
import com.gzjy.common.exception.BizException;
import com.gzjy.receive.mapper.ReceiveSampleItemMapper;
import com.gzjy.receive.mapper.ReceiveSampleMapper;
import com.gzjy.receive.mapper.ReportExtendMapper;
import com.gzjy.receive.model.ReceiveSample;
import com.gzjy.receive.model.ReceiveSampleItem;
import com.gzjy.receive.model.ReceiveSampleTask;
import com.gzjy.receive.model.ReportExtend;
import com.gzjy.user.UserService;
import com.gzjy.user.model.User;

@Service
public class ReportService {
	private static Logger logger = LoggerFactory.getLogger(ReportService.class);
	@Autowired
	private ProcessEngine processEngine;
	@Autowired
	UserService userService;
	@Autowired
	ReceiveSampleMapper receiveSampleMapper;
	@Autowired
	ReportExtendMapper reportExtendMapper;
	@Autowired
	ReceiveSampleService receiveSampleService;
	@Autowired
	ReceiveSampleItemMapper receiveSampleItemMapper;
	/**
	 * 编辑报告
	 * @param receiveSampleId
	 */
	@Transactional
	public void editReceiveSample(ReceiveSample receiveSample) {
		receiveSampleMapper.updateByPrimaryKeySelective(receiveSample);
	}

	
	
	/**
	 * 启动报告流程引擎
	 * @param receiveSampleId
	 */
	@Transactional
	public void deploymentProcess(String receiveSampleId) {
		RuntimeService runtimeService = processEngine.getRuntimeService();
		User user = userService.getCurrentUser();
		Map<String, Object> variables = new HashMap<String, Object>();
		// 设定流程标记tag=1
		variables.put("tag", 1);
		// 启动流程引擎时首先要指定编辑人，默认是当前用户
		variables.put("editPersion", user.getId());
		String processId = runtimeService.startProcessInstanceByKey("ReportProcess", variables).getId();
		// 流程启动成功之后将返回的流程ID回填到合同receive_sample表中
		ReceiveSample receiveSample = new ReceiveSample();
		receiveSample.setReceiveSampleId(receiveSampleId);		
		receiveSample.setReportProcessId(processId);
		receiveSampleMapper.updateByPrimaryKeySelective(receiveSample);
	}

	/**
	 * 根据用户ID获取当前用户任务
	 * @param isHandle(0表示查詢未完成任務，1表示查詢已完成任務)
	 * @return ArrayList<ContractTask>
	 */	
	public ArrayList<ReceiveSampleTask> getContractTaskByUserId(String isHandle) {
		String userId = userService.getCurrentUser().getId();
		ArrayList<ReceiveSampleTask> taskList = new ArrayList<ReceiveSampleTask>();
		if ("0".equals(isHandle)) {
			logger.info("查询未完成任务");
			TaskService taskService = processEngine.getTaskService();
			List<Task> tasks = taskService.createTaskQuery().taskAssignee(userId).list();
			for (Task task : tasks) {
				logger.info("ID:" + task.getId() + ",任务名称:" + task.getName() + ",执行人:" + task.getAssignee() + ",开始时间:"
							+ task.getCreateTime());
				ReceiveSampleTask contractTask = new ReceiveSampleTask();
				contractTask.setId(task.getId());
				contractTask.setName(task.getName());
				contractTask.setAssignee(task.getAssignee());
				contractTask.setCreateTime(task.getCreateTime());
				contractTask.setProcessInstanceId(task.getProcessInstanceId());
				contractTask.setExecutionId(task.getExecutionId());
				taskList.add(contractTask);
			}
		} else {
			HistoryService historyService = processEngine.getHistoryService();
			List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery().finished()
					.taskAssignee(userId).list();
			logger.info("历史任务:" + tasks.size() + " userId:" + userId);
			for (HistoricTaskInstance task : tasks) {
				logger.info("ID:" + task.getId() + ",任务名称:" + task.getName() + ",执行人:" + task.getAssignee() + ",开始时间:"
						+ task.getCreateTime());
				ReceiveSampleTask contractTask = new ReceiveSampleTask();
				contractTask.setId(task.getId());
				contractTask.setName(task.getName());
				contractTask.setAssignee(task.getAssignee());
				contractTask.setCreateTime(task.getCreateTime());
				contractTask.setProcessInstanceId(task.getProcessInstanceId());
				contractTask.setExecutionId(task.getExecutionId());
				taskList.add(contractTask);
			}
		}
		return taskList;
	}

	
	/**
	 * 执行报告中的编辑任务
	 * @param taskId  任务编号
	 */
	@Transactional
	public void doEditTask(String taskId, String receiveSampleId, String examinePersonId, String comment) {
		TaskService taskService = processEngine.getTaskService();
		RuntimeService runtimeService = processEngine.getRuntimeService();
		Task task = (Task) taskService.createTaskQuery().taskId(taskId).list().get(0);
		//执行任务过程中动态指定审核人
		runtimeService.setVariable(task.getExecutionId(), "examinePersonId", examinePersonId);
		//执行任务过程中需要设置标记位方便流程引擎区分
		runtimeService.setVariable(task.getExecutionId(), "tag", 2);
		ReceiveSample receiveSample = new ReceiveSample();
		receiveSample.setReceiveSampleId(receiveSampleId);
		//设置合同状态为待审核
		receiveSample.setReportStatus(1);
		receiveSampleMapper.updateByPrimaryKeySelective(receiveSample);
//		ReceiveSample receiveSample = receiveSampleMapper.selectByPrimaryKey(receiveSampleId);
//		taskService.addComment(taskId,null, comment);
		taskService.complete(taskId);	
		
	}
	
	/**
	 * 执行报告中的审核任务
	 * @param taskId  任务编号
	 */
	@Transactional
	public void doExamineTask(String taskId, String receiveSampleId,String approvePersonId, boolean pass, String comment) {
		TaskService taskService = processEngine.getTaskService();
		RuntimeService runtimeService = processEngine.getRuntimeService();
		Task task = (Task) taskService.createTaskQuery().taskId(taskId).list().get(0);
		//执行任务过程中可能同意可能拒绝,故需要设置标记位方便流程引擎区分
		if (pass) {
			runtimeService.setVariable(task.getExecutionId(), "tag", 3);
			//执行任务过程中动态指定批准人
			runtimeService.setVariable(task.getExecutionId(), "approvePersonId", approvePersonId);
		}else {
			runtimeService.setVariable(task.getExecutionId(), "tag", 1);
		}
		//设置合同状态为待批准
		ReceiveSample receiveSample = new ReceiveSample();
		receiveSample.setReceiveSampleId(receiveSampleId);				
		if(pass) {
			receiveSample.setReportStatus(2);
		}else {
			receiveSample.setReportStatus(0);
		}		
		receiveSampleMapper.updateByPrimaryKeySelective(receiveSample);
//		taskService.addComment(taskId, null, comment);
		taskService.complete(taskId);		
	}
	
	
	/**
	 * 执行报告中的批准任务
	 * @param taskId  任务编号
	 */
	@Transactional
	public void doApproveTask(String taskId, String receiveSampleId, boolean pass,String comment) {
		TaskService taskService = processEngine.getTaskService();
		RuntimeService runtimeService = processEngine.getRuntimeService();
		Task task = (Task) taskService.createTaskQuery().taskId(taskId).list().get(0);
		//执行任务过程中可能同意可能拒绝,故需要设置标记位方便流程引擎区分
		if (pass) {
			runtimeService.setVariable(task.getExecutionId(), "tag", 4);			
		}else {
			runtimeService.setVariable(task.getExecutionId(), "tag", 2);
		}
		ReceiveSample receiveSample = new ReceiveSample();
		receiveSample.setReceiveSampleId(receiveSampleId);	
		if(pass) {
			receiveSample.setReportStatus(3);
		}else {
			receiveSample.setReportStatus(1);
		}	
		receiveSampleMapper.updateByPrimaryKeySelective(receiveSample);
//		taskService.addComment(taskId, null, comment);
		taskService.complete(taskId);
	}
	
	
	/**
	 * 执行报告中的批准任务
	 * @param taskId  任务编号
	 *//*
	@Transactional
	public void doPrintTask(String taskId, String receiveSampleId,boolean pass) {
		TaskService taskService = processEngine.getTaskService();
		RuntimeService runtimeService = processEngine.getRuntimeService();
		Task task = (Task) taskService.createTaskQuery().taskId(taskId).list().get(0);
		//执行任务过程中可能同意可能拒绝,故需要设置标记位方便流程引擎区分
		if (pass) {
			runtimeService.setVariable(task.getExecutionId(), "tag", 5);			
		}else {
			runtimeService.setVariable(task.getExecutionId(), "tag", 3);
		}
		taskService.complete(taskId);
	}*/
	
	/**
	 * 根据receiveSampleId查询检验项列表
	 * @param receiveSampleId
	 * @return List<ReceiveSampleItem>
	 */
	public List<ReceiveSampleItem> getReceiveSampleItemList(String receiveSampleId) {
		 return receiveSampleItemMapper.selectByReceiveSampleId(receiveSampleId);
	}
	
	/**
	 * 修改ReceiveSampleItem
	 * @param receiveSampleId
	 * @return List<ReceiveSampleItem>
	 */
	public void updateReceiveSampleItem(ReceiveSampleItem receiveSampleItem) {
		 receiveSampleItemMapper.updateByPrimaryKeySelective(receiveSampleItem);
	}
	
	/**
	 * 修改ReceiveSample
	 * @param receiveSampleId
	 * @return List<ReceiveSampleItem>
	 */
	public void updateReceiveSample(ReceiveSample receiveSample) {
		 receiveSampleMapper.updateByPrimaryKeySelective(receiveSample);
	}
	
	
	public void insertReportExtend(ReportExtend record) {
		reportExtendMapper.insert(record);
	}
	
	public void updateReportExtend(ReportExtend record) {
		reportExtendMapper.updateByPrimaryKeySelective(record);
	}
	
	public void deleteReportExtend(String id) {
		reportExtendMapper.deleteByPrimaryKey(id);
	}
	
	/**
	 * 多条件查询报告
	 */
	public PageInfo<ReceiveSample> getReportByCondition(String id, String reportId, String entrustedUnit, String inspectedUnit,
			 String sampleName, String executeStandard, String productionUnit, String sampleType,
			 String checkType, Integer reportStatus, String order, int status, Integer pageNum,
			 Integer pageSize, String startTime, String endTime) 
	{
		Map<String, Object> filter = new HashMap<String, Object>();
		if (StringUtils.isBlank(startTime)) {
			startTime = null;
		}
		if (StringUtils.isBlank(endTime)) {
			endTime = null;
		}
		if (!StringUtils.isBlank(reportId)) {
			filter.put("report_id", reportId);
		}
		if (!StringUtils.isBlank(inspectedUnit)) {
			filter.put("inspected_unit", inspectedUnit);
		}
		if (!StringUtils.isBlank(sampleName)) {
			filter.put("sample_name", sampleName);
		}
		if (!StringUtils.isBlank(executeStandard)) {
			filter.put("execute_standard", executeStandard);
		}
		if (!StringUtils.isBlank(productionUnit)) {
			filter.put("production_unit", productionUnit);
		}
		if (!StringUtils.isBlank(id)) {
			filter.put("receive_sample_id", id);
		}
		if (!StringUtils.isBlank(entrustedUnit)) {
			filter.put("entrusted_unit", entrustedUnit);
		}
		if (!StringUtils.isBlank(sampleType)) {
			filter.put("sample_type", sampleType);
		}
		if (!StringUtils.isBlank(checkType)) {
			filter.put("check_type", checkType);
		}
		if (reportStatus != null && reportStatus != 5) { 
			filter.put("report_status", reportStatus);
			User u = userService.getCurrentUser();
			boolean superUser = u.getRole().isSuperAdmin();
			if (!superUser) {
				String name = u.getName();
				if (reportStatus == 0) {
					filter.put("draw_user", name);
				}
				if (reportStatus == 1) {
					filter.put("examine_user", name);
				}
				if (reportStatus == 2) {
					filter.put("approval_user", name);
				}
			}
		}
		if (StringUtils.isBlank(order)) {
			order = "created_at desc";
		}
		if (status != 5) {
			filter.put("status", status);
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date start = null;
		Date end = null;
		try {
			start = startTime == null ? null : sdf.parse(startTime);
			end = endTime == null ? null : sdf.parse(endTime);
		} catch (Exception e) {
			e.printStackTrace();
			throw new BizException("输入的时间格式不合法！");
		}
		return receiveSampleService.select(pageNum, pageSize, order, filter, start, end);
	}
}