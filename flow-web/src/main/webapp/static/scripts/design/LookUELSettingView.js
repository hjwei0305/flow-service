/**
 * Created by fly on 2017/4/18.
 */
EUI.LookUELSettingView = EUI.extend(EUI.CustomUI, {
    data: null,
    businessModelId: null,

    initComponent: function () {
        this.window = EUI.Window({
            width: 520,
            height: 280,
            padding: 10,
            title: this.data.name || this.title,
            items: this.initCenter()
        })
        ;
        this.logicUelCmp = EUI.getCmp("logicUel");
        this.groovyUelCmp = EUI.getCmp("groovyUel");
        if (this.data && !Object.isEmpty(this.data)) {
            this.loadData();
        }
    },
    initCenter: function () {
        var g = this;
        return [{
            xtype: "TextArea",
            width: 489,
            height: 120,
            id: "logicUel",
            readonly: true,
            style: {
                "margin-left": "10px"
            },
            name: "logicUel"
        }, {
            xtype: "TextArea",
            width: 489,
            height: 120,
            name: "groovyUel",
            style: {
                "margin-left": "10px"
            },
            id: "groovyUel",
            readonly: true
        }];
    },
    loadData: function () {
        this.logicUelCmp.setValue(this.data.logicUel);
        this.groovyUelCmp.setValue(this.data.groovyUel);
    }
})
;