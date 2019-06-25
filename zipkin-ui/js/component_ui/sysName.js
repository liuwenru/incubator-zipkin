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

import {component} from 'flightjs';
import Cookies from 'js-cookie';
import $ from 'jquery';
import queryString from 'query-string';

export default component(function sysNames(){
    // 更新调用后台接口后刷新页面下拉框
    this.updateSysNameDropdown=function(ev,data){
        $('#sysName').empty();
        this.$node.append($($.parseHTML('<option value="all">所有</option>')));
        $.each(data.names, (i, item) => {
            $('<option>').val(item.sysguid).text(item.name).appendTo('#sysName');
        });
    };
    this.onChange = function() {
        Cookies.set('last-sysName', this.$node.val());
        this.trigger('#containerName','uiChangeContainerName',{names:"aaaa",lastSystemName:this.$node.val()});
        //this.triggerChange(this.$node.val());

    };
    this.after('initialize', function() {
        //this.trigger("uiChangeSysName");
        this.on('change',this.onChange);
        this.on(document,'sysnamess', this.updateSysNameDropdown);
    });
});