package com.ecmp.core.api;

import com.ecmp.core.entity.AbstractEntity;
import com.ecmp.core.entity.RelationEntity;
import com.ecmp.core.entity.RelationParam;
import com.ecmp.vo.OperateResult;
import io.swagger.annotations.ApiOperation;

import javax.ws.rs.*;
import java.util.List;

/**
 * <strong>实现功能:</strong>
 * <p>分配关系业务实体的服务接口</p>
 *
 * @param <T> AbstractEntity的子类
 * @param <P> AbstractEntity的子类
 * @param <C> AbstractEntity的子类
 * @author 王锦光(wangj)
 * @version 1.0.1 2017-06-14 14:15
 */
public interface IBaseRelationService<T extends AbstractEntity<String> & RelationEntity<P, C>,
        P extends AbstractEntity<String>, C extends AbstractEntity<String>> {
    /**
     * 通过父实体Id获取子实体清单
     *
     * @param parentId 父实体Id
     * @return 子实体清单
     */
    @GET
    @Path("getChildrenFromParentId")
    @ApiOperation(value = "通过角色获取功能项清单", notes = "通过角色Id获取已分配的功能项清单")
    List<C> getChildrenFromParentId(@QueryParam("parentId") String parentId);

    /**
     * 创建分配关系
     *
     * @param parentId 父实体Id
     * @param childIds 子实体Id清单
     * @return 操作结果
     */
    @POST
    @Path("insertRelations")
    @ApiOperation(value = "创建分配关系", notes = "通过父实体Id和子实体Id清单创建分配关系")
    OperateResult insertRelations(@QueryParam("parentId") String parentId, @QueryParam("childIds") List<String> childIds);

    /**
     * 创建分配关系
     *
     * @param relationParam 分配关系参数
     * @return 操作结果
     */
    @POST
    @Path("insertRelationsByParam")
    @ApiOperation(value = "创建分配关系", notes = "通过分配关系参数创建分配关系")
    OperateResult insertRelationsByParam(RelationParam relationParam);

    /**
     * 移除分配关系
     *
     * @param parentId 父实体Id
     * @param childIds 子实体Id清单
     * @return 操作结果
     */
    @DELETE
    @Path("removeRelations")
    @ApiOperation(value = "移除分配关系", notes = "通过父实体Id和子实体Id清单移除分配关系")
    OperateResult removeRelations(@QueryParam("parentId") String parentId, @QueryParam("childIds") List<String> childIds);

    /**
     * 移除分配关系
     *
     * @param relationParam 分配关系参数
     * @return 操作结果
     */
    @DELETE
    @Path("removeRelationsByParam")
    @ApiOperation(value = "移除分配关系", notes = "通过分配关系参数移除分配关系")
    OperateResult removeRelationsByParam(RelationParam relationParam);

    /**
     * 获取未分配的子实体清单
     *
     * @param parentId 父实体Id
     * @return 子实体清单
     */
    @GET
    @Path("getUnassigned")
    @ApiOperation(value = "获取未分配的功能项", notes = "获取用户有权限且未分配的功能项清单")
    List<C> getUnassignedChildren(@QueryParam("parentId") String parentId);

    /**
     * 通过子实体Id获取父实体清单
     *
     * @param childId 子实体Id
     * @return 父实体清单
     */
    @GET
    @Path("getParentsFromChildId")
    @ApiOperation(value = "通过子实体Id获取父实体清单", notes = "通过子实体Id获取父实体清单")
    List<P> getParentsFromChildId(@QueryParam("childId") String childId);

    /**
     * 通过父实体清单创建分配关系
     *
     * @param childId   子实体Id
     * @param parentIds 父实体Id清单
     * @return 操作结果
     */
    @POST
    @Path("insertRelationsByParents")
    @ApiOperation(value = "通过父实体清单创建分配关系", notes = "通过父实体清单创建分配关系")
    OperateResult insertRelationsByParents(@QueryParam("childId") String childId, @QueryParam("parentIds") List<String> parentIds);

    /**
     * 通过父实体清单移除分配关系
     *
     * @param childId   子实体Id
     * @param parentIds 父实体Id清单
     * @return 操作结果
     */
    @DELETE
    @Path("removeRelationsByParents")
    @ApiOperation(value = "通过父实体清单移除分配关系", notes = "通过父实体清单移除分配关系")
    OperateResult removeRelationsByParents(@QueryParam("childId") String childId, @QueryParam("parentIds") List<String> parentIds);

    /**
     * 通过父实体Id获取分配关系清单
     *
     * @param parentId 父实体Id
     * @return 分配关系清单
     */
    @GET
    @Path("getRelationsByParentId")
    @ApiOperation(value = "通过父实体Id获取分配关系清单", notes = "通过父实体Id获取分配关系清单")
    List<T> getRelationsByParentId(@QueryParam("parentId") String parentId);
}
