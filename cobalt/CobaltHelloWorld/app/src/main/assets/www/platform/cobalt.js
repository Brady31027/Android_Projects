/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Cobaltians
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
var cobalt = {
    version: '0.5.1',
    events: {}, //objects of events defined by the user
    debug: false,
    debugInBrowser: false,
    debugInDiv: false,

    callbacks: {},//array of all callbacks by callbackID
    lastCallbackId: 0,

    /*    cobalt.init(options)
     see doc for options
     */
    init: function (options) {

        cobalt.utils.init();

        if (options) {
            this.debug = ( options.debug === true );
            this.debugInBrowser = ( options.debugInBrowser === true );
            this.debugInDiv = ( options.debugInDiv === true );
            this.plugins.pluginsOptions = options.plugins || {};

            if (cobalt.debugInDiv) {
                this.createLogDiv();
            }
            if (options.events) {
                this.events = options.events
            }
            cobalt.storage.enable();

            cobalt.utils.extend(cobalt.datePicker, options.datePicker);
            if (cobalt.datePicker.enabled) {
                cobalt.datePicker.init();
            }

        } else {
            cobalt.storage.enable();
        }

        if (cobalt.adapter && cobalt.adapter.init) {
            cobalt.adapter.init();
        }

        cobalt.plugins.init();

        //send cobalt is ready event to native
        cobalt.send({'type': 'cobaltIsReady', version: this.version})
    },
    addEventListener: function (eventName, handlerFunction) {
        if (typeof eventName === "string" && typeof handlerFunction === "function") {
            this.events[eventName] = handlerFunction;
        }
    },
    removeEventListener: function (eventName) {
        if (typeof eventName === "string" && this.events[eventName]) {
            this.events[eventName] = undefined;
        }
    },
    /*    cobalt.log(stuff,...)
     all arguments can be a string or an object. object will be json-ised and separated with a space.
     cobalt.log('toto')
     cobalt.log('a',5,{"hip":"hop"})
     */
    log: function () {
        var logString = cobalt.argumentsToString(arguments);
        if (cobalt.debug) {
            if (cobalt.debugInBrowser && window.console) {
                console.log(logString);
            } else {
                cobalt.send({type: "log", value: logString})
            }
            cobalt.divLog(logString)
        }
    },
    divLog: function () {
        if (cobalt.debugInDiv) {
            cobalt.createLogDiv();
            var logdiv = cobalt.utils.$('#cobalt_logdiv');
            if (logdiv) {
                var logString = "<br/>" + cobalt.argumentsToString(arguments);
                try {
                    cobalt.utils.append(logdiv, logString);
                } catch (e) {
                    cobalt.utils.append(logdiv, "<b>cobalt.log failed on something.</b>");
                }
            }
        }
    },
    argumentsToString: function () {
        var stringItems = [];
        //ensure arguments[0] exists?
        cobalt.utils.each(arguments[0], function (i, elem) {
            stringItems.push(cobalt.utils.logToString(elem))
        });
        return stringItems.join(' ');
    },
    /* internal, create log div if needed */
    createLogDiv: function () {
        if (!cobalt.utils.$('#cobalt_logdiv')) {
            //create usefull log div:
            cobalt.utils.append(document.body, '<div id="cobalt_logdiv" style="width:100%; text-align: left; height: 100px; border:1px solid blue; overflow: scroll; background:#eee;"></div>')
        }
    },
    //Sends an object to native side.
    //See doc for guidelines.
    send: function (obj, callback) {
        if (!typeof obj === "object") return;
        if (callback) {
            obj.callback = cobalt.registerCallback(callback);
        }
        if (cobalt.debugInBrowser) {
            cobalt.log('sending', obj);
        }
        if (cobalt.adapter) {
            cobalt.adapter.send(obj, callback);
        }
    },
    registerCallback: function (callback) {
        var callbackId;
        if (callback) {
            if (typeof callback === "function") {
                callbackId = "" + (cobalt.lastCallbackId++);
                cobalt.callbacks[callbackId] = callback;
                cobalt.callbacks.latest = callback;
            } else if (typeof callback === "string") {
                callbackId = callback;
            }
        }
        return callbackId;
    },
    //Sends an event to native side.
    //See doc for guidelines.
    sendEvent: function (eventName, params, callback) {
        if (eventName) {
            var obj = {
                type: "event",
                event: eventName,
                data: params
            };
            cobalt.send(obj, callback);
        }
    },
    //Sends a callback to native side.
    //See doc for guidelines.
    sendCallback: function (callback, data) {
        if (typeof callback === "string" && callback.length > 0) {
            cobalt.divLog("calling callback with callback id = ", callback);
            cobalt.send({type: "callback", callback: callback, data: data})
        }
    },
    //Navigate to an other page or do some special navigation actions
    navigate: {
        //cobalt.navigate.push({ page : "next.html", controller:"myController", animated:false });
        push: function (options) {
            if (options && (options.page || options.controller)) {
                cobalt.send({
                    type: "navigation",
                    action: "push",
                    data: {
                        page: options.page,
                        controller: options.controller,
                        animated: (options.animated !== false), //default to true
                        data: options.data,
                        bars: options.bars
                    }
                });
                if (cobalt.debugInBrowser && window.event && window.event.altKey) {
                    setTimeout(function () {
                        window.open(options.page, '_blank');
                    }, 0);
                }
            }
        },
        //cobalt.navigate.pop();
        pop: function (data) {
            cobalt.send({"type": "navigation", "action": "pop", data: {data: data}});

            if (cobalt.debugInBrowser && window.event && window.event.altKey) {
                window.close();
            }
        },
        //cobalt.navigate.popTo({ page : "next.html", controller:"myController" });
        popTo: function (options) {
            if (options && (options.page || options.controller)) {
                cobalt.send({
                    type: "navigation",
                    action: "pop",
                    data: {
                        page: options.page,
                        controller: options.controller,
                        data: options.data
                    }
                });

                if (cobalt.debugInBrowser && window.event && window.event.altKey) {
                    window.close();
                }
            }
        },
        //cobalt.navigate.replace({ page : "next.html", controller:"myController", animated:false });
        replace: function (options) {
            if (options && (options.page || options.controller)) {
                cobalt.send({
                    type: "navigation",
                    action: "replace",
                    data: {
                        page: options.page,
                        controller: options.controller,
                        animated: (options.animated !== false), //default to true
                        clearHistory: (options.clearHistory === true), //default to false
                        data: options.data,
                        bars: options.bars
                    }
                });
                if (cobalt.debugInBrowser && window.event && window.event.altKey) {
                    location.href = options.page;
                }
            }
        },
        modal: function (options) {
            if (options && (options.page || options.controller)) {
                cobalt.adapter.navigateToModal(options);

                if (cobalt.debugInBrowser && window.event && window.event.altKey) {
                    setTimeout(function () {
                        window.open(options.page, '_blank');
                    }, 0);
                }
            }
        },
        dismiss: function (data) {
            cobalt.adapter.dismissFromModal(data);

            if (cobalt.debugInBrowser && window.event && window.event.altKey) {
                window.close();
            }
        }
    },
    /* sends a toast request to native */
    toast: function (text) {
        cobalt.send({type: "ui", control: "toast", data: {message: cobalt.utils.logToString(text)}});
    },

    /*  Raise a native alert with options
     */
    alert: function (options) {

        var obj = {};

        if (options && (options.message || options.title )) {
            if (typeof options == "string") {
                options = {message: options};
            }

            cobalt.utils.extend(obj, {
                title: options.title,
                message: options.message,
                //ensure buttons is an array of strings or default to one Ok button
                buttons: (options.buttons && cobalt.utils.isArray(options.buttons) && options.buttons.length) ? options.buttons : ['Ok'],
                //only supported on Android
                cancelable: (options.cancelable) ? true : false
            });

            var callback = ( typeof options.callback === "string" || typeof options.callback === "function" ) ? options.callback : undefined;

            cobalt.send({
                type: "ui", control: "alert", data: obj
            }, callback);

            if (cobalt.debugInBrowser) {
                var btns_str = "";
                cobalt.utils.each(obj.buttons, function (index, button) {
                    btns_str += "\t" + index + " - " + button + "\n";
                });
                var index = parseInt(window.prompt(
                    "Title : " + obj.title + "\n"
                    + "Message : " + obj.message + "\n"
                    + "Choices : \n" + btns_str, 0), 10);

                switch (typeof callback) {
                    case "function":
                        callback({index: (index == NaN) ? undefined : index});
                        break;
                    case "string":
                        var str_call = callback + "({index : " + index + "})";
                        try {
                            eval(str_call);
                        } catch (e) {
                            cobalt.log('failed to call ', str_call);
                        }
                        break;
                }
            }

        }
    },
    pullToRefresh: {
        /*
         set texts of Pull-to-Refresh.
         //see doc for guidelines.
         //cobalt.pullToRefresh.setTexts("Pull to refresh", "Refreshing...");
         //cobalt.pullToRefresh.setTexts(undefined, undefined);
         */
        setTexts: function (pullToRefreshText, refreshingText) {
            if (typeof pullToRefreshText != "string") pullToRefreshText = undefined;
            if (typeof refreshingText != "string") pullToRefreshText = undefined;

            cobalt.send({
                type: "ui",
                control: "pullToRefresh",
                data: {
                    action: "setTexts",
                    texts: {
                        pullToRefresh: pullToRefreshText,
                        refreshing: refreshingText
                    }
                }
            });
        }
    },
    /*
     show a web page as an layer.
     //see doc for guidelines.
     //cobalt.webLayer("show","tests_12_webAlertContent.html",1.2);
     //cobalt.webLayer("dismiss");
     //in next example, foobar object will be sent in onWebLayerDismissed :
     //cobalt.webLayer("dismiss",{ foo : "bar"});
     */
    webLayer: function (action, data, fadeDuration) {
        switch (action) {
            case "dismiss":
                cobalt.send({type: "webLayer", action: "dismiss", data: data});
                break;
            case "show":
                if (data) {
                    cobalt.send({type: "webLayer", action: "show", data: {page: data, fadeDuration: fadeDuration}})
                }
                break;
        }
    },
    /*
     open an url in the device browser.
     //cobalt.openExternalUrl("http://cobaltians.com")
     */
    openExternalUrl: function (url) {
        if (url) {
            cobalt.send({
                type: "intent",
                action: "openExternalUrl",
                data: {
                    url: url
                }
            });
        }
    },
    /* internal, called from native */
    execute: function (json) {
        cobalt.divLog("received", json);
        /*parse data if string, die silently if parsing error */
        if (json && typeof json == "string") {
            try {
                json = JSON.parse(json);
            } catch (e) {
                cobalt.divLog("can't parse string to JSON");
            }
        }
        try {
            switch (json && json.type) {
                case "plugin":
                    cobalt.plugins.handleEvent(json);
                    break;
                case "event":
                    cobalt.adapter.handleEvent(json);
                    break;
                case "callback":
                    cobalt.adapter.handleCallback(json);
                    break;
                case "ui":
                    switch (json.control) {
                        case "bars":
                            cobalt.nativeBars.handleEvent(json.data);
                            break;
                    }
                    break;
                default:
                    cobalt.adapter.handleUnknown(json)
            }
        } catch (e) {
            cobalt.log('cobalt.execute failed : ' + e)
        }
    },
    //internal function to try calling callbackID if it's representing a string or a function.
    tryToCallCallback: function (json) {
        cobalt.divLog('trying to call web callback');
        var callbackfunction = null;
        if (cobalt.utils.isNumber(json.callback) && typeof cobalt.callbacks[json.callback] === "function") {
            //if it's a number, a real JS callback should exist in cobalt.callbacks
            callbackfunction = cobalt.callbacks[json.callback]

        } else if (typeof json.callback === "string") {
            //if it's a string, check if function exists
            callbackfunction = eval(json.callback);
        }
        if (typeof callbackfunction === "function") {
            try {
                callbackfunction(json.data)
            } catch (e) {
                cobalt.log('Failed calling callback : ' + e)
            }
        } else {
            cobalt.adapter.handleUnknown(json);
        }
    },
    defaultBehaviors: {
        handleEvent: function (json) {
            cobalt.log("received event", json.event);
            if (cobalt.events && typeof cobalt.events[json.event] === "function") {
                cobalt.events[json.event](json.data, json.callback);
            } else {
                switch (json.event) {
                    case "onBackButtonPressed":
                        cobalt.log('sending OK for a native back');
                        cobalt.sendCallback(json.callback, {value: true});
                        break;
                    default :
                        cobalt.adapter.handleUnknown(json);
                        break;
                }
            }
        },
        handleCallback: function (json) {
            switch (json.callback) {
                default:
                    cobalt.tryToCallCallback(json);
                    break;
            }
        },
        handleUnknown: function (json) {
            cobalt.log('received unhandled message ', json);
        },
        navigateToModal: function (options) {
            cobalt.send({
                "type": "navigation", "action": "modal", data: {
                    page: options.page,
                    controller: options.controller,
                    data: options.data,
                    bars: options.bars
                }
            });
        },
        dismissFromModal: function (data) {
            cobalt.send({"type": "navigation", "action": "dismiss", data: {data: data}});
        },
        initStorage: function () {
            return cobalt.storage.enable()
        }
    },
    utils: {
        $: function (selector) {
            if (typeof selector === "string") {
                if (selector[0] == "#") {
                    return document.getElementById(selector.replace('#', ''));
                }
                else {
                    return document.querySelectorAll(selector)
                }
            } else {
                return selector;
            }
        },
        toString: Object.prototype.toString,
        logToString: function (stuff) {
            switch (typeof  stuff) {
                case "string":
                    break;
                case "function":
                    stuff = ("" + stuff.call).replace('native', 'web'); //to avoid panic ;)
                    break;
                default:
                    try {
                        stuff = JSON.stringify(stuff)
                    } catch (e) {
                        stuff = "" + stuff;
                    }
            }
            return stuff;
        },

        class2type: {},
        attr: function (node, attr, value) {
            node = cobalt.utils.$(node);
            if (value) {
                if (node && node.setAttribute) {
                    node.setAttribute(attr, value)
                }
            } else {
                return ( node && node.getAttribute ) ? node.getAttribute(attr) : undefined;
            }
        },
        text: function (node, text) {
            node = cobalt.utils.$(node);
            if (typeof node == "object") {
                if (text) {
                    node.innerText = text;
                } else {
                    return node.innerText
                }
            }
        },
        html: function (node, html) {
            node = cobalt.utils.$(node);
            if (typeof node == "object") {
                if (html != undefined) {
                    node.innerHTML = html;
                } else {
                    return node.innerHTML;
                }
            }
        },
        HTMLEncode: function (value) {
            var element = document.createElement('div');
            cobalt.utils.text(element, value || '');
            return cobalt.utils.html(element);
        },
        HTMLDecode: function (value) {
            var element = document.createElement('div');
            cobalt.utils.html(element, value || '');
            return cobalt.utils.text(element);
        },
        likeArray: function (obj) {
            return typeof obj.length == 'number'
        },
        each: function (elements, callback) {
            var i, key;
            if (cobalt.utils.likeArray(elements)) {
                for (i = 0; i < elements.length; i++)
                    if (callback.call(elements[i], i, elements[i]) === false) return
            } else {
                for (key in elements)
                    if (callback.call(elements[key], key, elements[key]) === false) return
            }
        },
        extend: function (obj, source) {
            if (!obj) obj = {};
            if (source) {
                for (var prop in source) {
                    obj[prop] = source[prop];
                }
            }
            return obj;
        },
        append: function (node, html) {
            node = cobalt.utils.$(node);
            if (typeof node == "object") {
                node.innerHTML = node.innerHTML + html;
            }
        },
        css: function (node, obj) {
            node = cobalt.utils.$(node);
            if (typeof node === "object" && node.style) {
                if (typeof obj == "object") {
                    for (var prop in obj) {
                        node.style[prop] = (typeof obj[prop] == "string") ? obj[prop] : obj[prop] + "px";
                    }
                } else {
                    var style = window.getComputedStyle(node);
                    if (style) {
                        return style[obj] ? style[obj].replace('px', '') : undefined;
                    }
                }
            }
        },
        addClass: function (node, css_class) {
            node = cobalt.utils.$(node);
            if (typeof node == "object" && css_class) {
                if (node.classList) {
                    node.classList.add(css_class);
                } else {
                    node.setAttribute("class", node.getAttribute("class") + " " + css_class);
                }
            }
        },
        removeClass: function (node, css_class) {
            node = cobalt.utils.$(node);
            if (typeof node == "object" && css_class) {
                if (node.classList) {
                    node.classList.remove(css_class);
                } else {
                    node.setAttribute("class", node.getAttribute("class").replace(css_class, ''));
                }
            }
        },
        escape: encodeURIComponent,
        serialize: function (params, obj, traditional, scope) {
            var type, array = cobalt.utils.isArray(obj), hash = cobalt.utils.isPlainObject(obj);
            cobalt.utils.each(obj, function (key, value) {
                type = cobalt.utils.type(value);
                if (scope) key = traditional ? scope :
                scope + '[' + (hash || type == 'object' || type == 'array' ? key : '') + ']';
                // handle data in serializeArray() format
                if (!scope && array) params.add(value.name, value.value);
                // recurse into nested objects
                else if (type == "array" || (!traditional && type == "object"))
                    cobalt.utils.serialize(params, value, traditional, key);
                else params.add(key, value)
            })
        },
        param: function (obj, traditional) {
            var params = [];
            params.add = function (k, v) {
                this.push(cobalt.utils.escape(k) + '=' + cobalt.utils.escape(v))
            };
            cobalt.utils.serialize(params, obj, traditional);
            return params.join('&').replace(/%20/g, '+')
        },
        isArray: function (obj) {
            if (!Array.isArray) {
                return Object.prototype.toString.call(obj) === '[object Array]';
            } else {
                return Array.isArray(obj);
            }
        },
        isNumber: function (n) {
            return !isNaN(parseFloat(n)) && isFinite(n);
        },
        isWindow: function (obj) {
            return obj != null && obj == obj.window
        },
        isObject: function (obj) {
            return this.type(obj) == "object"
        },
        isPlainObject: function (obj) {
            return this.isObject(obj) && !this.isWindow(obj) && Object.getPrototypeOf(obj) == Object.prototype;
        },
        type: function (obj) {
            return obj == null ?
                String(obj) :
            this.class2type[cobalt.utils.toString.call(obj)] || "object";
        },
        init: function () {
            this.each("Boolean Number String Function Array Date RegExp Object Error".split(" "), function (i, name) {
                cobalt.utils.class2type["[object " + name + "]"] = name.toLowerCase()
            })
        }
    },
    nativeBars: {
        handler: undefined,
        setEventListener: function (handler) {
            cobalt.nativeBars.handler = handler;
        },
        handleEvent: function (data) {
            cobalt.log(data.action, data.name, data.data);
            if (data.name && cobalt.nativeBars.handler) {
                cobalt.nativeBars.handler(data.name, data.action, data.data);
            } else {
                cobalt.log('no handler defined. use setEventListener');
            }
        },
        send: function (data) {
            if (data) {
                cobalt.send({type: "ui", control: "bars", data: data});
            }
        },
        setBarsVisible: function (visible) {
            if (visible && (typeof visible.top != "undefined" || typeof visible.bottom != "undefined")) {
                cobalt.nativeBars.send({action: "setBarsVisible", visible: visible});
            } else {
                cobalt.log('setBarsVisible : nothing to set.')
            }
        },
        setBarContent: function (content) {
            if (content && (
                    typeof content.backgroundColor != "undefined"
                    || typeof content.bottom != "undefined"
                    || typeof content.androidIcon != "undefined"
                    || typeof content.title != "undefined"
                )) {
                cobalt.nativeBars.send({action: "setBarContent", content: content});
            } else {
                cobalt.log('setBarContent : nothing to set.')
            }
        },
        setActionContent: function (name, content) {
                    if (name && content && (
                            typeof content.androidIcon != "undefined"
                            || typeof content.iosIcon != "undefined"
                            || typeof content.icon != "undefined"
                            || typeof content.color != "undefined"
                            || typeof content.title != "undefined"
                        )) {
                        cobalt.nativeBars.send({action: "setActionContent", name:name, content: content});
                    } else {
                        cobalt.log('setActionContent : nothing to set.')
                    }
                },
        setActionParam: function (action, name, param, value) {
            if (param) {
                if (name) {
                    var obj = {action: action, name: name};
                    obj[param] = value;
                    cobalt.nativeBars.send(obj);
                } else {
                    cobalt.log(action, ': no action name provided.')
                }
            }
        },
        setActionVisible: function (name, visible) {
            this.setActionParam("setActionVisible", name, "visible", visible);
        },
        setActionEnabled: function (name, enabled) {
            this.setActionParam("setActionEnabled", name, "enabled", enabled);
        },
        setActionBadge: function (name, badge) {
            this.setActionParam("setActionBadge", name, "badge", '' + badge);
        },
        setBars: function (newBars) {
            if (cobalt.utils.isObject(newBars)) {
                cobalt.nativeBars.send({action: "setBars", bars: newBars});
            } else {
                cobalt.log('setBars: no bars provided.')
            }
        }
    },
    datePicker: {
        //USER OPTIONS
        enabled: true,
        texts: {
            validate: "Ok",
            cancel: "Cancel",
            clear: "Clear"
        },
        //default format is "yyyy-mm-dd".
        format: function (value) {
            return value;
        },
        placeholderStyles: "width:100%; color:#AAA;",
        //internal
        init: function () {
            var inputs = cobalt.utils.$('input[type=date]');

            cobalt.utils.each(inputs, function () {
                var input = this;
                var id = cobalt.utils.attr(input, 'id');
                if (!id) {
                    id = 'CobaltGeneratedId_' + Math.random().toString(36).substring(7);
                    cobalt.utils.attr(input, 'id', id);
                }
                cobalt.datePicker.updateFromValue.apply(input);
            });

            if (cobalt.adapter && cobalt.adapter.datePicker && cobalt.adapter.datePicker.init) {
                cobalt.adapter.datePicker.init(inputs);
            }
        },
        updateFromValue: function () {
            var id = cobalt.utils.attr(this, 'id');
            cobalt.log("updating storage value of date #", id);
            if (this.value) {
                cobalt.utils.addClass(this, 'not_empty');
            } else {
                cobalt.utils.removeClass(this, 'not_empty');
            }
            cobalt.log('current value is', this.value);
            var values = this.value.split('-');
            if (values.length == 3) {
                var d = {
                    year: parseInt(values[0], 10),
                    month: parseInt(values[1], 10),
                    day: parseInt(values[2], 10)
                };
                cobalt.log('setting storage date ', 'CobaltDatePickerValue_' + id, d);
                cobalt.storage.set('CobaltDatePickerValue_' + id, d)

            } else {
                cobalt.log('removing date');
                cobalt.storage.remove('CobaltDatePickerValue_' + id)
            }
            return false;
        },
        enhanceFieldValue: function () {
            //cobalt.log('updating date format')
            var date = cobalt.storage.get('CobaltDatePickerValue_' + cobalt.utils.attr(this, 'id'));
            if (date) {
                cobalt.log('format date=', date);
                this.value = cobalt.datePicker.format(cobalt.datePicker.stringifyDate(date))
            }
        },
        stringifyDate: function (date) {
            if (date && date.year !== undefined) {
                return date.year + '-' + cobalt.datePicker.zerofill(date.month, 2) + '-' + cobalt.datePicker.zerofill(date.day, 2)
            }
            return "";
        },
        zerofill: function (number, padding) {
            return new String(new Array(padding + 1).join("0") + number).slice(-padding)
        },
        val: function (input) {
            if (input[0] && input[0].value !== undefined) {
                input = input[0];
            }
            if (cobalt.adapter && cobalt.adapter.datePicker && cobalt.adapter.datePicker.val) {
                cobalt.log('returning cobalt adapter datePicker value');
                return cobalt.adapter.datePicker.val(input);
            } else {
                cobalt.log('returning cobalt default datePicker value');
                return input.value || undefined;
            }
        }
    },
    storage: {
        /*    localStorage helper

         cobalt.storage.set('town','Lannion');
         cobalt.storage.get('town');
         //returns 'Lannion'

         cobalt.storage.set('age',12);
         cobalt.storage.get('age');
         //returns 12 (number)

         cobalt.storage.set('user',{name:'toto',age:6});
         cobalt.storage.get('user');
         //returns {name:'toto',age:6} (object)

         */
        storage: false,
        enable: function () {
            var storage,
                fail,
                uid;
            try {
                uid = new Date().toString();
                (storage = window.localStorage).setItem(uid, uid);
                fail = storage.getItem(uid) != uid;
                storage.removeItem(uid);
                fail && (storage = false);
            } catch (e) {
            }

            if (!storage) {
                return false;
            } else {
                this.storage = storage;
                return true;
            }
        },
        clear: function () {
            if (this.storage) {
                this.storage.clear();
            }
        },
        set: function (uid, value) {
            if (this.storage) {
                var obj = {
                    t: typeof value,
                    v: value
                };
                if (obj.v instanceof Date) {
                    obj.t = "date";
                }
                return this.storage.setItem(uid, JSON.stringify(obj));
            }
        },
        get: function (uid) {
            if (this.storage) {
                var val = this.storage.getItem(uid, 'json');
                val = JSON.parse(val);
                if (val) {
                    switch (val.t) {
                        case "date":
                            return new Date(val.v);
                        default :
                            return val.v;
                    }
                }
                return;
            }
        },
        remove: function (uid) {
            if (this.storage) {
                return this.storage.removeItem(uid)
            }
        }
    },
    plugins: {
        /*
         all plugins must
         - have a "init" function.
         - define a "name" proprety that will identify them.
         they can
         - have a "handleEvent" function that will receive all event {type:"plugin", name:thePluginName}
         - add options to the init call to receive them when the plugin will be inited.
         */
        pluginsOptions: {},
        enabledPlugins: {},

        //add a plugin to the plugin list.
        register: function (plugin) {
            if (plugin && typeof plugin.name === "string" && typeof plugin.init === "function") {
                cobalt.plugins.enabledPlugins[plugin.name] = plugin;
            }
        },
        init: function () {
            for (var pluginName in cobalt.plugins.enabledPlugins) {
                if (cobalt.plugins.enabledPlugins[pluginName]) {
                    //init each plugin with options set at the init step.
                    var options = cobalt.plugins.pluginsOptions[pluginName];
                    cobalt.plugins.enabledPlugins[pluginName].init(options);
                }
            }
        },
        handleEvent: function (event) {
            //try to call plugin "handleEvent" function (if any).
            if (typeof event.name === "string") {
                if (cobalt.plugins.enabledPlugins[event.name]
                    && typeof cobalt.plugins.enabledPlugins[event.name].handleEvent === "function") {

                    try {
                        cobalt.plugins.enabledPlugins[event.name].handleEvent(event);
                    } catch (e) {
                        cobalt.log('failed calling "' + event.name + '" plugin handleEvent function. ', e)
                    }
                } else {
                    cobalt.log('plugin "' + event.name + '" not found or no handleEvent function in this plugin.')
                }
            } else {
                cobalt.log('unknown plugin event', event)
            }

        }
    }

};cobalt.android_adapter = {
    //
    //ANDROID ADAPTER
    //
    init: function () {
        cobalt.platform = { is : "Android" };
    },
    //send native stuff
    send: function (obj) {
        if (obj && !cobalt.debugInBrowser) {
            cobalt.divLog('sending', obj);
            try {
                Android.onCobaltMessage(JSON.stringify(obj));
            } catch (e) {
                cobalt.log('ERROR : cant connect to native')
            }

        }
    },
    //modal stuffs. really basic on ios, more complex on android.
    navigateToModal: function (options) {
        cobalt.send({
            "type": "navigation",
            "action": "modal",
            data: {
				page: options.page, 
				controller: options.controller,
				data : options.data,
				bars : options.bars
			}
        }, 'cobalt.adapter.storeModalInformations');
    },
    dismissFromModal: function (data) {
        var dismissInformations = cobalt.storage.get("dismissInformations");
        if (dismissInformations && dismissInformations.page && dismissInformations.controller) {
            cobalt.send({
                "type": "navigation",
                "action": "dismiss",
                data: {
					page: dismissInformations.page, 
					controller: dismissInformations.controller,
					data : data
				}
            });
            cobalt.storage.remove("dismissInformations");
        } else {
            cobalt.log("WANRING : dismissInformations are not available in storage")
        }

    },
    storeModalInformations: function (params) {
        cobalt.divLog("storing informations for the dismiss :", params);
        cobalt.storage.set("dismissInformations", params);

    },
    //localStorage stuff
    initStorage: function () {
        //on android, try to bind window.localStorage to Android LocalStorage
        try {
            window.localStorage = LocalStorage;
        } catch (e) {
            cobalt.log("LocalStorage WARNING : can't find android class LocalStorage. switching to raw localStorage")
        }
        return cobalt.storage.enable();
    },
    //datePicker stuff
    datePicker: {
        init: function (inputs) {

            cobalt.utils.each(inputs, function () {
                var input = this;
                var id = cobalt.utils.attr(input, 'id');

                cobalt.log('datePicker setted with value=' + input.value);
                cobalt.utils.attr(input, 'type', 'text');
                cobalt.datePicker.enhanceFieldValue.apply(input);

                input.addEventListener('focus', function () {
                    cobalt.log('show formPicker date for date #', id);
                    input.blur();
                    var previousDate = cobalt.storage.get('CobaltDatePickerValue_' + id);
                    if (!previousDate) {
                        var d = new Date();
                        previousDate = {
                            year: d.getFullYear(),
                            day: d.getDate(),
                            month: d.getMonth() + 1
                        }
                    }
                    cobalt.send({
                        type: "ui", control: "picker", data: {
                            type: "date", date: previousDate,
                            texts: cobalt.datePicker.texts
                        }
                    }, function (newDate) {
                        if (newDate && newDate.year) {
                            input.value = newDate.year + '-' + newDate.month + '-' + newDate.day;
                            cobalt.log('setting storage date ', newDate);
                            cobalt.storage.set('CobaltDatePickerValue_' + id, newDate);
                            cobalt.datePicker.enhanceFieldValue.apply(input);
                        } else {
                            cobalt.log('removing storage date');
                            input.value = "";
                            cobalt.storage.remove('CobaltDatePickerValue_' + id)
                        }
                    });
                    return false;

                }, false);

            });
        },
        val: function (input) {
            var date = cobalt.storage.get('CobaltDatePickerValue_' + cobalt.utils.attr(input, 'id'));
            if (date) {
                var str_date = cobalt.datePicker.stringifyDate(date);
                cobalt.log('returning storage date ', str_date);
                return str_date;
            }
            return undefined;
        }
    },

    //default behaviours
    handleEvent: cobalt.defaultBehaviors.handleEvent,
    handleCallback: cobalt.defaultBehaviors.handleCallback,
    handleUnknown: cobalt.defaultBehaviors.handleUnknown
};
cobalt.adapter = cobalt.android_adapter;