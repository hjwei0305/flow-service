/**
 * Created by fly on 2017/4/18.
 */
EUI.FlowNodeSettingView = EUI.extend(EUI.CustomUI, {
    title: null,
    initComponent: function () {
        EUI.Window({
            title: this.title + " 配置",
            buttons: this.getButtons(),
            items: [{
                xtype: "TabPanel",
                items: this.getTabItems()
            }]
        });
    },
    getButtons: function () {
        return [{
            title: "保存",
            handler: function () {
            }
        }, {
            title: "保存",
            handler: function () {
            }
        }];
    },
    getTabItems: function () {
        return [{
            title: "常规",
            items: [{
                xtype:"FormPanel"
            }]
        }];
    }
});