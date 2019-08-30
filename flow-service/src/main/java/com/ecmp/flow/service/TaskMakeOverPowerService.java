package com.ecmp.flow.service;

import com.ecmp.context.ContextUtil;
import com.ecmp.core.dao.BaseEntityDao;
import com.ecmp.core.search.Search;
import com.ecmp.core.search.SearchFilter;
import com.ecmp.core.service.BaseEntityService;
import com.ecmp.enums.UserAuthorityPolicy;
import com.ecmp.flow.api.ITaskMakeOverPowerService;
import com.ecmp.flow.dao.TaskMakeOverPowerDao;
import com.ecmp.flow.entity.TaskMakeOverPower;
import com.ecmp.vo.OperateResultWithData;
import com.ecmp.vo.ResponseData;
import com.ecmp.vo.SessionUser;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.ecmp.core.search.SearchFilter.Operator.GE;
import static com.ecmp.core.search.SearchFilter.Operator.LE;

@Service
public class TaskMakeOverPowerService extends BaseEntityService<TaskMakeOverPower> implements ITaskMakeOverPowerService {

    @Autowired
    private TaskMakeOverPowerDao taskMakeOverPowerDao;

    protected BaseEntityDao<TaskMakeOverPower> getDao() {
        return this.taskMakeOverPowerDao;
    }


    /**
     * 查询自己的转授权单据
     */
    @Override
    public ResponseData findAllByUser() {
        String userId = ContextUtil.getUserId();
        List<TaskMakeOverPower> list;
        SessionUser sessionUser = ContextUtil.getSessionUser();
        UserAuthorityPolicy authorityPolicy = sessionUser.getAuthorityPolicy();
        if (authorityPolicy.equals(UserAuthorityPolicy.TenantAdmin)) {
            list = taskMakeOverPowerDao.findAll();
        }else{
            list = taskMakeOverPowerDao.findListByProperty("userId",userId);
        }
        return ResponseData.operationSuccessWithData(list);
    }


    /**
     * 保存转授权单据
     * @param entity 实体
     * @return
     */
    @Override
    public OperateResultWithData<TaskMakeOverPower> setUserAndsave(TaskMakeOverPower entity){
        entity.setUserId(ContextUtil.getUserId());
        entity.setUserAccount(ContextUtil.getUserAccount());
        entity.setUserName(ContextUtil.getUserName());
        //规则检查
        ResponseData  responseData = checkOk(entity);
        if(!responseData.getSuccess()){
            return  OperateResultWithData.operationFailure(responseData.getMessage());
        }
        return super.save(entity);
    }


    @Override
    public ResponseData updateOpenStatusById(String id) {
        TaskMakeOverPower bean =   taskMakeOverPowerDao.findOne(id);
        if(bean.getOpenStatus()==true){
            bean.setOpenStatus(false);
        }else{
            //生效规则检查
            ResponseData  responseData = checkOk(bean);
            if(!responseData.getSuccess()){
                return  OperateResultWithData.operationFailure(responseData.getMessage());
            }
            bean.setOpenStatus(true);
        }
        taskMakeOverPowerDao.save(bean);
        return ResponseData.operationSuccessWithData(bean);
    }


    /**
     * 检查逻辑是否符合
     * @return
     */
    public ResponseData checkOk(TaskMakeOverPower bean){
        //检查该用户建立的授权信息是否重复
        ResponseData  responseData =  checkUserRepetition(bean);
        if(!responseData.getSuccess()){
            return responseData;
        }
        //检查该用户是否在该时间段已经被转授权
        responseData =  checkPowerUserRepetition(bean);
        return  responseData;
    }


    /**
     * 检查该用户是否被授权
     * @param bean
     * @return
     */
    public ResponseData checkPowerUserRepetition(TaskMakeOverPower bean){
        //查询该用户所有授权信息（不包含当前数据）
        Search search = new Search();
        search.addFilter(new SearchFilter("powerUserId", bean.getUserId()));
        search.addFilter(new SearchFilter("openStatus", true));
        if(StringUtils.isNotEmpty(bean.getId())){
            search.addFilter(new SearchFilter("id", bean.getId(), SearchFilter.Operator.NE));
        }
        List<TaskMakeOverPower> list = taskMakeOverPowerDao.findByFilters(search);
        Date startDate =  bean.getPowerStartDate();
        Date endDate = bean.getPowerEndDate();
        SimpleDateFormat dateFormat =new SimpleDateFormat("yyyy-MM-dd");
        if(list!=null&&list.size()>0){
            for(int i=0;i<list.size();i++){
                TaskMakeOverPower a =list.get(i);
                Date  start =  a.getPowerStartDate();
                Date  end = a.getPowerEndDate();
                if(((startDate.after(start)||startDate.equals(start))&&(startDate.before(end)||startDate.equals(end)))  //开始时间重叠
                        || ((endDate.after(start)||endDate.equals(start))&&(endDate.before(end)||endDate.equals(end)))  //结束事件重叠
                        || (startDate.before(start)&&endDate.after(end))//包含
                        ){
                    return ResponseData.operationFailure("["+dateFormat.format(start)+"]至["+dateFormat.format(end)+"],["+a.getUserName()+"]已经转授权给您，所以该时间段您不能再转授权！");
                }
            }
        }
        return  ResponseData.operationSuccess();
    }


    /**
     * 检查该用户建立的授权信息是否重复
      * @param bean
     * @return
     */
    public ResponseData checkUserRepetition(TaskMakeOverPower bean){
        //查询该用户所有授权信息（不包含当前数据）
        Search search = new Search();
        search.addFilter(new SearchFilter("userId", bean.getUserId()));
        search.addFilter(new SearchFilter("openStatus", true));
        if(StringUtils.isNotEmpty(bean.getId())){
            search.addFilter(new SearchFilter("id", bean.getId(), SearchFilter.Operator.NE));
        }
        List<TaskMakeOverPower> list = taskMakeOverPowerDao.findByFilters(search);
        Date startDate =  bean.getPowerStartDate();
        Date endDate = bean.getPowerEndDate();
        SimpleDateFormat dateFormat =new SimpleDateFormat("yyyy-MM-dd");
        if(list!=null&&list.size()>0){
            for(int i=0;i<list.size();i++){
                TaskMakeOverPower a =list.get(i);
                Date  start =  a.getPowerStartDate();
                Date  end = a.getPowerEndDate();
                if(((startDate.after(start)||startDate.equals(start))&&(startDate.before(end)||startDate.equals(end)))  //开始时间重叠
                        || ((endDate.after(start)||endDate.equals(start))&&(endDate.before(end)||endDate.equals(end)))  //结束事件重叠
                        || (startDate.before(start)&&endDate.after(end))//包含
                        ){
                    return ResponseData.operationFailure("["+dateFormat.format(start)+"]至["+dateFormat.format(end)+"],您已经建立了一份["+a.getPowerUserName()+"]的转授权，不能重复创建！");
                }
            }
        }
        return  ResponseData.operationSuccess();
    }


    /**
     * 根据被授权人ID查询满足要求的授权信息
     * @param powerUserId  被授权人ID
     * @return
     */
    public List<TaskMakeOverPower> findMeetUserByPowerId(String powerUserId){
        SimpleDateFormat simp   = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        try{
            date = simp.parse(simp.format(date));
        }catch (Exception e){
        }
        Search search = new Search();
        search.addFilter(new SearchFilter("powerUserId", powerUserId));
        search.addFilter(new SearchFilter("openStatus", true));
        search.addFilter(new SearchFilter("powerStartDate", date,LE,"Date"));
        search.addFilter(new SearchFilter("powerEndDate", date,GE,"Date"));
        return       taskMakeOverPowerDao.findByFilters(search);
    }





}
