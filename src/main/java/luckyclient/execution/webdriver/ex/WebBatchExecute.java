package luckyclient.execution.webdriver.ex;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import org.openqa.selenium.WebDriver;

import luckyclient.execution.httpinterface.TestControl;
import luckyclient.execution.webdriver.WebDriverInitialization;
import luckyclient.remote.api.GetServerApi;
import luckyclient.remote.api.serverOperation;
import luckyclient.remote.entity.ProjectCase;
import luckyclient.remote.entity.ProjectCaseParams;
import luckyclient.remote.entity.ProjectCaseSteps;
import luckyclient.remote.entity.TaskExecute;
import luckyclient.utils.LogUtil;

/**
 * =================================================================
 * ����һ�������Ƶ��������������������κ�δ��������ǰ���¶Գ����������޸ĺ�������ҵ��;��Ҳ�������Գ�������޸ĺ����κ���ʽ�κ�Ŀ�ĵ��ٷ�����
 * Ϊ���������ߵ��Ͷ��ɹ���LuckyFrame�ؼ���Ȩ��Ϣ�Ͻ��۸�
 * ���κ����ʻ�ӭ��ϵ�������ۡ� QQ:1573584944  seagull1985
 * =================================================================
 * 
 * @author�� seagull
 * @date 2017��12��1�� ����9:29:40
 * 
 */
public class WebBatchExecute{
	
	public static void batchCaseExecuteForTast(String projectname,String taskid,String batchcase) throws IOException{
		//��¼��־�����ݿ�
		serverOperation.exetype = 0;   
		TestControl.TASKID = taskid;
		int drivertype = serverOperation.querydrivertype(taskid);
		WebDriver wd = null;
		try {
			wd = WebDriverInitialization.setWebDriverForTask(drivertype);
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			LogUtil.APP.error("��ʼ��WebDriver�����쳣��", e1);
		}
		serverOperation caselog = new serverOperation();
		TaskExecute task=GetServerApi.cgetTaskbyid(Integer.valueOf(taskid));
		List<ProjectCaseParams> pcplist=GetServerApi.cgetParamsByProjectid(task.getProjectId().toString());
		 //ִ��ȫ���ǳɹ�״̬����
		if(batchcase.indexOf("ALLFAIL")>-1){   
			List<Integer> caseIdList = caselog.getCaseListForUnSucByTaskId(taskid);
			for(int i=0;i<caseIdList.size();i++){
			   ProjectCase testcase = GetServerApi.cGetCaseByCaseId(caseIdList.get(i));
			   List<ProjectCaseSteps> steps=GetServerApi.getStepsbycaseid(testcase.getCaseId());
			   //ɾ���ɵ���־
			   serverOperation.deleteTaskCaseLog(testcase.getCaseId(), taskid);    
			   try {
				WebCaseExecution.caseExcution(testcase, steps, taskid,wd,caselog,pcplist);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				LogUtil.APP.error("�û�ִ�й������׳��쳣��", e);
			 }
			}			
		}else{                                           //����ִ������
			String[] temp=batchcase.split("\\#");
			for(int i=0;i<temp.length;i++){
				ProjectCase testcase = GetServerApi.cGetCaseByCaseId(Integer.valueOf(temp[i]));
				List<ProjectCaseSteps> steps=GetServerApi.getStepsbycaseid(testcase.getCaseId());
				//ɾ���ɵ���־
				serverOperation.deleteTaskCaseLog(testcase.getCaseId(), taskid);
				try {
					WebCaseExecution.caseExcution(testcase, steps,taskid,wd,caselog,pcplist);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					LogUtil.APP.error("�û�ִ�й������׳��쳣��", e);
				}
			}
		}
		serverOperation.updateTaskExecuteData(taskid, 0,2);
        //�ر������
        wd.quit();
	}
	
}