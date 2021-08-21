package org.smartframework.cloud.starter.mybatis.plus.test.prepare.dynamicdatasource.biz;

import com.baomidou.dynamic.datasource.annotation.DS;
import org.smartframework.cloud.starter.mybatis.plus.common.biz.BaseBiz;
import org.smartframework.cloud.starter.mybatis.plus.test.prepare.dynamicdatasource.entity.RoleInfoEntity;
import org.smartframework.cloud.starter.mybatis.plus.test.prepare.dynamicdatasource.constants.DatasourceNames;
import org.smartframework.cloud.starter.mybatis.plus.test.prepare.dynamicdatasource.mapper.RoleInfoBaseMapper;
import org.springframework.stereotype.Repository;

@Repository
@DS(DatasourceNames.AUTH)
public class RoleInfoOmsBiz extends BaseBiz<RoleInfoBaseMapper, RoleInfoEntity> {


}