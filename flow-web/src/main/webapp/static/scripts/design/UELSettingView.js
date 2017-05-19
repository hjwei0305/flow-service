/**
 * Created by fly on 2017/4/18.
 */
EUI.UELSettingView = EUI.extend(EUI.CustomUI, {
    title: null,
    afterConfirm: null,
    businessModelId: null,
    properties: null,

    initComponent: function () {
        this.window = EUI.Window({
            width: 710,
            height: 500,
            padding: 10,
            title: this.title,
            buttons: this.getButtons(),
            layout: "border",
            items: [{
                region: "north",
                height: 40,
                items: [{
                    xtype: "TextField",
                    name: "name",
                    title: "表达式名称",
                    allowBlank: false
                }]
            },
                this.initLeft(), this.initCenter()
            ]
        })
        ;
        this.logicUelCmp = EUI.getCmp("logicUel");
        this.groovyUelCmp = EUI.getCmp("groovyUel");
        this.addEvents();
        this.getProperties();
    },
    getButtons: function () {
        var g = this;
        return [{
            title: "保存配置",
            selected: true,
            handler: function () {
                var data = {
                    logicUel: g.logicUelCmp.getValue(),
                    groovyUel: g.groovyUelCmp.getValue()
                };
                g.afterConfirm && g.afterConfirm.call(this, data);
                g.window.close();
            }
        }, {
            title: "取消",
            handler: function () {
                g.window.close();
            }
        }];
    },
    initLeft: function () {
        return {
            region: "west",
            width: 165,
            html: "<div class='property-box'></div>"
        };
    },
    initCenter: function () {
        var g = this;
        return {
            region: "center",
            items: [{
                xtype: "Container",
                height: 100,
                id: "calculate",
                html: this.initCalculateBtns()
            }, {
                xtype: "TextArea",
                width: 489,
                height: 140,
                id: "logicUel",
                style: {
                    "margin-left": "10px"
                },
                name: "logicUel",
                afterValidate: function (value) {
                    if (!g.properties) {
                        return;
                    }
                    for (var key in g.properties) {
                        var reg = new RegExp(key, "g");
                        value = value.replace(reg, g.properties[key]);
                    }
                    g.groovyUelCmp.setValue(value);
                }
            }, {
                xtype: "TextArea",
                width: 489,
                height: 140,
                name: "groovyUel",
                style: {
                    "margin-left": "10px"
                },
                id: "groovyUel",
                readonly: true
            }
            ]
        };
    },
    initCalculateBtns: function () {
        var html = "";
        for (var i = 0; i < _flowUelBtn.length; i++) {
            var item = _flowUelBtn[i];
            html += "<div class='calculate-btn' uel='" + item.uel + "' operator='" + item.operator + "'>" + item.name + " " + item.operator + "</div>";
        }
        return html;
    }
    ,
    addEvents: function () {
        var g = this;
        $(".calculate-btn").bind("click", function () {
            var operator = " " + $(this).attr("operator") + " ";
            var uel = " " + $(this).attr("uel") + " ";
            var value = g.logicUelCmp.getValue() + operator;
            g.logicUelCmp.setValue(value);
            value = g.groovyUelCmp.getValue() + uel;
            g.groovyUelCmp.setValue(value);
        });
        $(".property-item").live("click", function () {
            var text = $(this).text();
            var key = $(this).attr("key");
            var value = g.logicUelCmp.getValue() + " " + text + " ";
            g.logicUelCmp.setValue(value);
            value = g.groovyUelCmp.getValue() + " " + key + " ";
            g.groovyUelCmp.setValue(value);
        });
    }
    ,
    getProperties: function () {
        var g = this;
        EUI.Store({
            url: _ctxPath + "/design/getProperties",
            params: {
                businessModelId: this.businessModelId
            },
            success: function (result) {
                if (!result.success) {
                    EUI.ProcessStatus(result);
                    return;
                }
                g.showProperties(result.data);
            },
            failure: function (result) {
                EUI.ProcessStatus(result);
            }
        });
    }
    ,
    showProperties: function (data) {
        var html = "";
        for (var key in data) {
            html += "<div class='property-item' key='" + key + "'>" + data[key] + "</div>";
        }
        $(".property-box").append(html);
    }
})
;