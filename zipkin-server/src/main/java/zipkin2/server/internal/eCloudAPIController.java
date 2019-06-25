/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package zipkin2.server.internal;


import com.alibaba.fastjson.JSON;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.HttpStatus;
import com.linecorp.armeria.common.MediaType;
import com.linecorp.armeria.server.annotation.Get;
import com.linecorp.armeria.server.annotation.Param;
import com.linecorp.armeria.server.annotation.ProducesJson;
import com.squareup.moshi.Json;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.util.ArrayList;
import java.util.List;

@ConditionalOnProperty(name = "zipkin.collector.ecloudapi.http.enabled", matchIfMissing = true)
public class eCloudAPIController {
  private static sysinfo[] syses;
  private static projectinfo[] projects;
  private static containerinfo[] containers;
  public eCloudAPIController() {
    projects=new projectinfo[10];
    for (int i=0;i<projects.length;i++){
      projects[i]=new projectinfo("项目"+i,String.valueOf(i));
    }
  }
  class sysinfo{
    String name;
    String sysguid;

    public sysinfo(String name, String sysguid) {
      this.name = name;
      this.sysguid = sysguid;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getSysguid() {
      return sysguid;
    }

    public void setSysguid(String sysguid) {
      this.sysguid = sysguid;
    }
  }

  class projectinfo{
    private String projectname;
    private String projectguid;
    public projectinfo(String projectname, String projectguid) {
      this.projectname = projectname;
      this.projectguid = projectguid;
    }
    public String getProjectname() {
      return projectname;
    }
    public void setProjectname(String projectname) {
      this.projectname = projectname;
    }
    public String getProjectguid() {
      return projectguid;
    }
    public void setProjectguid(String projectguid) {
      this.projectguid = projectguid;
    }
  }

  class containerinfo{
    private String containername;
    private String containerguid;

    public containerinfo(String containername, String containerguid) {
      this.containername = containername;
      this.containerguid = containerguid;
    }

    public String getContainername() {
      return containername;
    }

    public void setContainername(String containername) {
      this.containername = containername;
    }

    public String getContainerguid() {
      return containerguid;
    }

    public void setContainerguid(String containerguid) {
      this.containerguid = containerguid;
    }
  }


  // 获取所有的可以查看的项目
  @Get("/api/v1/projects")
  public HttpResponse getecloudprojects(){
    String projectsjson= JSON.toJSONString(projects);
    return HttpResponse.of(HttpStatus.OK,
      MediaType.PLAIN_TEXT_UTF_8,
      projectsjson);
  }

  // 根据给定的项目编号，查看对应的系统信息
  @Get("/api/v1/sysinfo/{projectguid}")
  public HttpResponse getecloudsysinfo(@Param("projectguid") String projectguid) {

    sysinfo tempsys1=new sysinfo("系统"+projectguid,projectguid);
    sysinfo tempsys2=new sysinfo("系统"+(projectguid+1),projectguid+1);
    ArrayList<sysinfo> sysinfos=new ArrayList<>();
    sysinfos.add(tempsys1);
    sysinfos.add(tempsys2);
    return HttpResponse.of(HttpStatus.OK,MediaType.PLAIN_TEXT_UTF_8,JSON.toJSONString(sysinfos));
  }
  // 根据给定系统guid获取
  @Get("/api/v1/containerinfo/{sysguid}")
  public HttpResponse getecloudsysnames(@Param("sysguid") String sysguid) {
    containerinfo tempcontainer1=new containerinfo("容器"+sysguid,sysguid);
    containerinfo tempcontainer2=new containerinfo("容器"+(sysguid+1),sysguid+1);
    List<containerinfo> containerinfoList=new ArrayList<containerinfo>();
    containerinfoList.add(tempcontainer1);
    containerinfoList.add(tempcontainer2);
    String jsonresponse= JSON.toJSONString(containerinfoList);
    return HttpResponse.of(HttpStatus.OK,MediaType.PLAIN_TEXT_UTF_8,jsonresponse);
  }

}



