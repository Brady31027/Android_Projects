webpackJsonp([2],{

/***/ 442:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
Object.defineProperty(__webpack_exports__, "__esModule", { value: true });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "AddShoppingItemPageModule", function() { return AddShoppingItemPageModule; });
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1_ionic_angular__ = __webpack_require__(132);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__add_shopping_item__ = __webpack_require__(445);
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};



var AddShoppingItemPageModule = (function () {
    function AddShoppingItemPageModule() {
    }
    AddShoppingItemPageModule = __decorate([
        Object(__WEBPACK_IMPORTED_MODULE_0__angular_core__["I" /* NgModule */])({
            declarations: [
                __WEBPACK_IMPORTED_MODULE_2__add_shopping_item__["a" /* AddShoppingItemPage */],
            ],
            imports: [
                __WEBPACK_IMPORTED_MODULE_1_ionic_angular__["d" /* IonicPageModule */].forChild(__WEBPACK_IMPORTED_MODULE_2__add_shopping_item__["a" /* AddShoppingItemPage */]),
            ],
        })
    ], AddShoppingItemPageModule);
    return AddShoppingItemPageModule;
}());

//# sourceMappingURL=add-shopping-item.module.js.map

/***/ }),

/***/ 445:
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "a", function() { return AddShoppingItemPage; });
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__angular_core__ = __webpack_require__(0);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1_ionic_angular__ = __webpack_require__(132);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__services_shopping_list_shopping_list_service__ = __webpack_require__(275);
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};



/**
 * Generated class for the AddShoppingItemPage page.
 *
 * See https://ionicframework.com/docs/components/#navigation for more info on
 * Ionic pages and navigation.
 */
var AddShoppingItemPage = (function () {
    function AddShoppingItemPage(navCtrl, navParams, shopping) {
        this.navCtrl = navCtrl;
        this.navParams = navParams;
        this.shopping = shopping;
        this.item = {
            name: "",
            quantity: undefined,
            price: undefined,
        };
    }
    AddShoppingItemPage.prototype.ionViewDidLoad = function () {
        console.log('ionViewDidLoad AddShoppingItemPage');
    };
    AddShoppingItemPage.prototype.addItem = function (item) {
        var _this = this;
        this.shopping.addItem(item).then(function (ref) {
            //console.log(ref.key);
            _this.navCtrl.setRoot('HomePage', { key: ref.key });
        });
    };
    AddShoppingItemPage = __decorate([
        Object(__WEBPACK_IMPORTED_MODULE_0__angular_core__["m" /* Component */])({
            selector: 'page-add-shopping-item',template:/*ion-inline-start:"/Users/brady/Desktop/android_ws/Android_Projects/cordova/ionic/firebase_spending_calc/src/pages/add-shopping-item/add-shopping-item.html"*/'<!--\n  Generated template for the AddShoppingItemPage page.\n\n  See http://ionicframework.com/docs/components/#navigation for more info on\n  Ionic pages and navigation.\n-->\n<ion-header>\n\n  <ion-navbar>\n    <ion-title>Add Item</ion-title>\n  </ion-navbar>\n\n</ion-header>\n\n<ion-content padding>\n\n  <ion-item>\n    <ion-label>Name</ion-label>\n    <ion-input [(ngModel)]="item.name" placeholder="iPad Pro"></ion-input>\n  </ion-item>\n\n  <ion-item>\n    <ion-label>Quantity</ion-label>\n    <ion-input [(ngModel)]="item.quantity" type="number" placeholder="1"></ion-input>\n  </ion-item>\n\n  <ion-item>\n    <ion-label>Price</ion-label>\n    <ion-input [(ngModel)]="item.price" type="number" placeholder="700"></ion-input>\n  </ion-item>\n\n  <button ion-button block clear (click)="addItem(item)">Add</button>\n  \n</ion-content>\n'/*ion-inline-end:"/Users/brady/Desktop/android_ws/Android_Projects/cordova/ionic/firebase_spending_calc/src/pages/add-shopping-item/add-shopping-item.html"*/,
        }),
        __metadata("design:paramtypes", [__WEBPACK_IMPORTED_MODULE_1_ionic_angular__["e" /* NavController */],
            __WEBPACK_IMPORTED_MODULE_1_ionic_angular__["f" /* NavParams */],
            __WEBPACK_IMPORTED_MODULE_2__services_shopping_list_shopping_list_service__["a" /* ShoppingListService */]])
    ], AddShoppingItemPage);
    return AddShoppingItemPage;
}());

//# sourceMappingURL=add-shopping-item.js.map

/***/ })

});
//# sourceMappingURL=2.js.map