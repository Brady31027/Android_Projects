import { Injectable } from "@angular/core";
import { ToastController } from "ionic-angular/components/toast/toast-controller";

@Injectable()
export class ToastService {
    constructor(private toastCtrl: ToastController) {
        
    }
    
    show(message: string, duration: number= 3000) {
     return this.toastCtrl.create({
         message,
         duration
     }).present();   
    }
}